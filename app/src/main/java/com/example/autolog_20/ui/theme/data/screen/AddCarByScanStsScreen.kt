package com.example.autolog_20.ui.theme.data.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.STSRecognitionUiState
import com.example.autolog_20.ui.theme.data.model.viewmodel.STSViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarByScanStsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: STSViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return STSViewModel() as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showImageSourceSheet by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    // Состояние для отслеживания, показывать ли форму редактирования
    var showEditForm by remember { mutableStateOf(false) }

    var editedVin by remember { mutableStateOf("") }
    var editedBrand by remember { mutableStateOf("") }
    var editedModel by remember { mutableStateOf("") }
    var editedYear by remember { mutableStateOf(0) }
    var editedColor by remember { mutableStateOf("") }
    var editedNumberPlate by remember { mutableStateOf("") }

    // Разрешение на камеру
    val cameraPermission = Manifest.permission.CAMERA

    // Ланчер для камеры
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoFile?.let { file ->
                selectedImageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                recognizeSTS(selectedImageUri!!, viewModel, scope, context)
            }
        }
        showImageSourceSheet = false
    }

    // Ланчер для разрешения на камеру
    val cameraPermissionState = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val timeStamp = System.currentTimeMillis()
            val photoFile = File(context.cacheDir, "sts_$timeStamp.jpg")
            tempPhotoFile = photoFile
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoUri)
        } else {
            Toast.makeText(context, "Для сканирования СТС нужно разрешение на камеру", Toast.LENGTH_SHORT).show()
            showImageSourceSheet = false
        }
    }

    // Ланчер для галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            recognizeSTS(it, viewModel, scope, context)
        }
        showImageSourceSheet = false
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is STSRecognitionUiState.Success -> {
                val data = (uiState as STSRecognitionUiState.Success).data
                editedVin = data.vin
                editedBrand = data.brand
                editedModel = data.model
                editedYear = data.year
                editedColor = data.color
                editedNumberPlate = data.numberPlate
                showEditForm = true // Показываем форму редактирования при успешном распознавании
            }
            STSRecognitionUiState.Loading -> {
                showEditForm = true // Показываем форму с прогрессом
            }
            else -> {
                // Не показываем форму если нет данных
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавление по СТС") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!showEditForm && selectedImageUri == null) {
                InitialContent(
                    onSelectPhoto = { showImageSourceSheet = true }
                )
            }

            selectedImageUri?.let { uri ->
                if (showEditForm) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PhotoThumbnail(uri = uri, context = context)
                }
            }

            when (uiState) {
                STSRecognitionUiState.Loading -> {
                    if (showEditForm) {
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Распознавание текста...")
                    }
                }

                is STSRecognitionUiState.Success -> {
                    if (showEditForm) {
                        EditFormContent(
                            editedVin = editedVin,
                            editedBrand = editedBrand,
                            editedModel = editedModel,
                            editedYear = editedYear,
                            editedColor = editedColor,
                            editedNumberPlate = editedNumberPlate,
                            onVinChange = { editedVin = it.uppercase() },
                            onBrandChange = { editedBrand = it },
                            onModelChange = { editedModel = it },
                            onYearChange = { newYear -> editedYear = newYear },
                            onColorChange = { editedColor = it },
                            onNumberPlateChange = { editedNumberPlate = it.uppercase() },
                            onSelectNewPhoto = {
                                selectedImageUri = null
                                showEditForm = false
                                viewModel.reset()
                                showImageSourceSheet = true
                            },
                            onConfirm = {
                                if (editedVin.length == 17 &&
                                    editedBrand.isNotBlank() &&
                                    editedModel.isNotBlank() &&
                                    editedYear in 1900..2028 &&
                                    editedColor.isNotBlank() &&
                                    editedNumberPlate.isNotBlank()
                                ) {
                                    navController.navigate(
                                        "add_car_from_sts/$editedVin/$editedBrand/$editedModel/$editedYear/$editedColor/$editedNumberPlate"
                                    )
                                } else {
                                    val errors = mutableListOf<String>()
                                    if (editedVin.length != 17) errors.add("VIN")
                                    if (editedBrand.isBlank()) errors.add("марку")
                                    if (editedModel.isBlank()) errors.add("модель")
                                    if (editedYear !in 1900..2028) errors.add("год выпуска")
                                    if (editedColor.isBlank()) errors.add("цвет")
                                    if (editedNumberPlate.isBlank()) errors.add("номер")

                                    val message = when (errors.size) {
                                        1 -> "Заполните поле: ${errors[0]}"
                                        else -> "Заполните поля: ${errors.joinToString(", ")}"
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }

                is STSRecognitionUiState.Error -> {
                    if (showEditForm) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = (uiState as STSRecognitionUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.reset()
                                selectedImageUri = null
                                showEditForm = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Попробовать снова")
                        }
                    }
                }

                STSRecognitionUiState.Idle -> Unit
            }
        }
    }

    if (showImageSourceSheet) {
        ImageSourceBottomSheet(
            onDismiss = { showImageSourceSheet = false },
            onGalleryClick = { galleryLauncher.launch("image/*") },
            onCameraClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        cameraPermission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val timeStamp = System.currentTimeMillis()
                    val photoFile = File(context.cacheDir, "sts_$timeStamp.jpg")
                    tempPhotoFile = photoFile
                    val photoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    cameraLauncher.launch(photoUri)
                } else {
                    cameraPermissionState.launch(cameraPermission)
                }
                showImageSourceSheet = false
            }
        )
    }
}

@Composable
private fun InitialContent(
    onSelectPhoto: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Сканирование СТС",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Сфотографируйте или выберите фото свидетельства о регистрации ТС",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSelectPhoto,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выбрать фото СТС")
        }
    }
}

@Composable
private fun PhotoThumbnail(uri: Uri, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        val bitmap = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()

        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "СТС фото",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun EditFormContent(
    editedVin: String,
    editedBrand: String,
    editedModel: String,
    editedYear: Int,
    editedColor: String,
    editedNumberPlate: String,
    onVinChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onNumberPlateChange: (String) -> Unit,
    onSelectNewPhoto: () -> Unit,
    onConfirm: () -> Unit
) {
    Spacer(modifier = Modifier.height(24.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Распознанные данные",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = editedVin,
                onValueChange = onVinChange,
                label = { Text("VIN") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = editedBrand,
                onValueChange = onBrandChange,
                label = { Text("Марка") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = editedModel,
                onValueChange = onModelChange,
                label = { Text("Модель") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = if (editedYear > 0) editedYear.toString() else "",
                onValueChange = { onYearChange(it.toIntOrNull() ?: 0) },
                label = { Text("Год выпуска") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = editedColor,
                onValueChange = onColorChange,
                label = { Text("Цвет") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = editedNumberPlate,
                onValueChange = onNumberPlateChange,
                label = { Text("Номер автомобиля") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSelectNewPhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Выбрать другое фото")
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Далее")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageSourceBottomSheet(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Выберите источник",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onGalleryClick() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Выбрать из галереи",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onCameraClick() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Сделать фото",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    }
}

private fun recognizeSTS(
    uri: Uri,
    viewModel: STSViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    context: Context
) {
    scope.launch {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: ByteArray(0)
            inputStream?.close()

            val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
            val photoPart = MultipartBody.Part.createFormData(
                "photo",
                "sts_${System.currentTimeMillis()}.jpg",
                requestBody
            )

            viewModel.recognizeSTS(photoPart)
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}