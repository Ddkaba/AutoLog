package com.example.autolog_20.ui.theme.data.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.AddCarByVinUiState
import com.example.autolog_20.ui.theme.data.model.AddCarManualUiState
import com.example.autolog_20.ui.theme.data.model.viewmodel.AddCarManualViewModel
import com.example.autolog_20.ui.theme.data.model.viewmodel.DrivingSurveyViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarManualScreen(navController: NavController) {
    val viewModel: AddCarManualViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddCarManualViewModel(RetrofitClient.api) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val surveyViewModel: DrivingSurveyViewModel = viewModel()

    var vinInput by remember { mutableStateOf("") }
    var brandInput by remember { mutableStateOf("") }
    var modelInput by remember { mutableStateOf("") }
    var fuelTypeInput by remember { mutableStateOf("Бензин") }
    var mileageInput by remember { mutableStateOf("") }
    var yearInput by remember { mutableStateOf("") }
    var numberInput by remember { mutableStateOf("") }
    var colorInput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val fuelTypes = listOf("Бензин", "Дизель", "Гибрид")
    val fuelTypeMap = mapOf(
        "Бензин" to "Petrol",
        "Дизель" to "Diesel",
        "Гибрид" to "Hybrid"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавление авто") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        // Убираем verticalScroll из основного Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is AddCarManualUiState.Input,
                is AddCarManualUiState.Error,
                is AddCarManualUiState.Loading,
                is AddCarManualUiState.VinAlreadyExistsError -> {
                    // Добавляем скролл только для этого блока
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Введите данные автомобиля", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(24.dp))

                        // Поле для VIN
                        OutlinedTextField(
                            value = vinInput,
                            onValueChange = { vinInput = it.uppercase().filter { c -> c.isLetterOrDigit() } },
                            label = { Text("VIN (17 символов)") },
                            placeholder = { Text("WAUZZZF55LA007770") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Поле для марки
                        OutlinedTextField(
                            value = brandInput,
                            onValueChange = { brandInput = it },
                            label = { Text("Марка") },
                            placeholder = { Text("Toyota") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Поле для модели
                        OutlinedTextField(
                            value = modelInput,
                            onValueChange = { modelInput = it },
                            label = { Text("Модель") },
                            placeholder = { Text("Camry") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = fuelTypeInput,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Тип топлива") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                fuelTypes.forEach { fuel ->
                                    DropdownMenuItem(
                                        text = { Text(fuel) },
                                        onClick = {
                                            fuelTypeInput = fuel
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        when (state) {
                            is AddCarManualUiState.Error -> {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                            is AddCarManualUiState.VinAlreadyExistsError -> {
                                Spacer(Modifier.height(16.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = state.message,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "Найден существующий автомобиль:\n" +
                                                    "${state.existingCar.brand} ${state.existingCar.model}\n" +
                                                    "Год: ${state.existingCar.year_of_manufacture}\n" +
                                                    "Номер: ${state.existingCar.number_plate}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                viewModel.checkCarData(
                                    vin = vinInput,
                                    brand = brandInput,
                                    model = modelInput,
                                    fuelType = fuelTypeMap[fuelTypeInput] ?: "Petrol"
                                )
                            },
                            enabled = vinInput.length == 17 &&
                                    brandInput.isNotBlank() &&
                                    modelInput.isNotBlank() &&
                                    state !is AddCarManualUiState.Loading,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            if (state is AddCarManualUiState.Loading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Продолжить")
                            }
                        }
                    }
                }

                is AddCarManualUiState.Preview -> {
                    val preview = state.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Проверьте данные", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(24.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                DetailRow("Марка", preview.brand)
                                DetailRow("Модель", preview.model)
                                DetailRow("VIN", preview.vin)
                                DetailRow("Тип топлива", fuelTypeInput)
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.confirmCar() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Верно, продолжить")
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.resetToInput() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Изменить данные")
                        }
                    }
                }

                is AddCarManualUiState.EnterMileage -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Информация об автомобиле", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(24.dp))

                        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

                        OutlinedTextField(
                            value = mileageInput,
                            onValueChange = { mileageInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Пробег (км)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = yearInput,
                            onValueChange = { yearInput = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("Год выпуска") },
                            placeholder = { Text("2020") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = numberInput,
                            onValueChange = { numberInput = it.uppercase() },
                            label = { Text("Номер автомобиля") },
                            placeholder = { Text("A000AA78") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                val mileage = mileageInput.toIntOrNull()
                                val year = yearInput.toIntOrNull()
                                if (mileage != null && year != null && yearInput.length == 4 && numberInput.isNotBlank()) {
                                    viewModel.saveMileageAndContinue(mileage, year, numberInput)
                                }
                            },
                            enabled = mileageInput.isNotBlank() && yearInput.length == 4 && numberInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Продолжить")
                        }
                    }
                }

                is AddCarManualUiState.EnterColor -> {
                    var colorError by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Укажите цвет автомобиля",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Это поможет при идентификации автомобиля",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        val suggestedColors = listOf("Черный", "Белый", "Серебристый", "Серый",
                            "Красный", "Синий", "Зеленый", "Желтый")

                        Text(
                            text = "Популярные цвета:",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestedColors.forEach { color ->
                                FilterChip(
                                    selected = colorInput == color,
                                    onClick = {
                                        colorInput = color
                                        colorError = null
                                    },
                                    label = { Text(color) },
                                    modifier = Modifier
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Или введите свой вариант:",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = colorInput,
                            onValueChange = {
                                colorInput = it
                                colorError = null
                            },
                            label = { Text("Цвет автомобиля") },
                            placeholder = { Text("Например: Темно-синий металлик") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = colorError != null,
                            supportingText = {
                                if (colorError != null) {
                                    Text(colorError!!, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (colorInput.isBlank()) {
                                    colorError = "Пожалуйста, укажите цвет автомобиля"
                                } else {
                                    viewModel.saveColorAndContinue(colorInput)
                                }
                            },
                            enabled = colorInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Продолжить")
                        }
                    }
                }

                is AddCarManualUiState.DrivingSurveyState -> {
                    val surveyState by surveyViewModel.surveyState.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        if (surveyState == null) {
                            surveyViewModel.startSurvey(
                                state.survey.vinData,
                                state.survey.mileage,
                                state.survey.year,
                                state.survey.number,
                                state.survey.color
                            )
                        }
                    }

                    val currentSurvey = surveyState ?: state.survey

                    DrivingSurveyScreen(
                        survey = currentSurvey,
                        viewModel = surveyViewModel,
                        onFinish = { summary ->
                            viewModel.goToSummary(summary)
                        }
                    )
                }

                is AddCarManualUiState.SurveySummaryState -> {
                    val context = LocalContext.current
                    var isSaving by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Подтверждение данных", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(24.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Основная информация",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(12.dp))
                                DetailRow("Марка", state.summary.vinData.brand)
                                DetailRow("Модель", state.summary.vinData.model)
                                DetailRow("VIN", state.summary.vinData.vin)
                                DetailRow("Тип топлива", state.summary.fuelType)
                                DetailRow("Пробег", "${state.summary.mileage} км")
                                DetailRow("Год выпуска", "${state.summary.year}")
                                DetailRow("Номер", state.summary.number)
                                DetailRow("Цвет", state.summary.color)
                                DetailRow("Привод", state.summary.driveType ?: "Не указан")
                                DetailRow("Коробка передач", state.summary.transmission ?: "Не указана")
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                isSaving = true
                                viewModel.addCompleteCarWithData(
                                    surveySummary = state.summary,
                                    onSuccess = {
                                        isSaving = false
                                        viewModel.confirmAllData()
                                    },
                                    onError = { error ->
                                        isSaving = false
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isSaving,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text("Добавить автомобиль")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.resetToInput() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Изменить данные")
                        }
                    }
                }

                is AddCarManualUiState.Success -> {
                    LaunchedEffect(Unit) {
                        delay(1500)
                        navController.popBackStack()
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Автомобиль успешно добавлен!", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}