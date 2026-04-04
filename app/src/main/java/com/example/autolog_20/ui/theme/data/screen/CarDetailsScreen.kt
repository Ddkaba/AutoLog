package com.example.autolog_20.ui.theme.data.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autolog_20.R
import com.example.autolog_20.ui.theme.TileExpenses
import com.example.autolog_20.ui.theme.TileMileage
import com.example.autolog_20.ui.theme.TileService
import com.example.autolog_20.ui.theme.TileTires
import com.example.autolog_20.ui.theme.TileTo
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.locale.TokenManager
import com.example.autolog_20.ui.theme.data.model.CarDetailResponse
import com.example.autolog_20.ui.theme.data.model.CarDetailsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.Manifest
import android.R.attr.subtitle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState

@Composable
fun CarHeader(car: CarDetailResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${car.brand} ${car.model}",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Гос. номер: ${car.numberPlate}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Год выпуска: ${car.yearOfManufacture}")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Цвет: ${car.color}")
                }
            }
        }
    }
}

@Composable
fun FeatureTile(
    title: String,
    subtitle: String,
    icon: Painter,
    color: androidx.compose.ui.graphics.Color,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    isWarning: Boolean = false,
    content: @Composable (() -> Unit) = {}
) {
    val backgroundColor = if (isWarning) Color(0x33FF5252) else color.copy(alpha = 0.12f)
    val borderColor = if (isWarning) Color.Red else color

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isWarning) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (content != {}) {
                    IconButton(onClick = { onExpandChange(!expanded) }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Свернуть" else "Развернуть",
                            tint = color
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun TireTypeDialog(
    onConfirm: (String) -> Unit
) {
    var selected by remember { mutableStateOf("summer") }

    AlertDialog(
        onDismissRequest = { /* нельзя закрыть без выбора */ },
        title = { Text("Укажите тип установленной резины") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selected == "summer",
                        onClick = { selected = "summer" }
                    )
                    Text("Летняя")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selected == "winter",
                        onClick = { selected = "winter" }
                    )
                    Text("Зимняя")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selected) }) {
                Text("Сохранить")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CarDetailsScreen(
    navController: NavController,
    numberPlate: String
) {
    val context = LocalContext.current.applicationContext
    val viewModel: CarDetailsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CarDetailsViewModel(RetrofitClient.api, numberPlate, context) as T
            }
        }
    )

    val carDetail by viewModel.carDetail.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showTireDialog by viewModel.showTireDialog.collectAsStateWithLifecycle()
    val tireRecommendation by viewModel.tireRecommendation.collectAsStateWithLifecycle()
    var expandedTires by remember { mutableStateOf(false) }

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted &&
            !TokenManager.shouldShowTireDialog()
        ) {
            Log.d("CarDetailsScreen", "Разрешения получены → запускаем проверку резины")
            viewModel.checkTireRecommendation()
        }
    }

    var expandedTo by remember { mutableStateOf(false) }
    var expandedExpenses by remember { mutableStateOf(false) }
    var expandedMileage by remember { mutableStateOf(false) }
    var showTireSelectionSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Автомобиль") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (showTireDialog) {
                TireTypeDialog(onConfirm = viewModel::setTireType)
            }

            // Шапка автомобиля
            carDetail?.let { CarHeader(car = it) } ?: run {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            Spacer(Modifier.height(16.dp))

            FeatureTile(
                title = "ТО",
                subtitle = "Ближайшее обслуживание",
                icon = rememberVectorPainter(Icons.Default.Build),
                color = TileTo,
                expanded = expandedTo,
                onExpandChange = { expandedTo = it },
                onClick = { navController.navigate("maintenance/${numberPlate}") }
            ) {
                Text(
                    text = "1 240 км",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            FeatureTile(
                title = "Покрышки",
                subtitle = when {
                    TokenManager.shouldShowTireDialog() -> "Укажите тип резины"
                    tireRecommendation == null -> "Проверка..."
                    tireRecommendation?.shouldChangeTo != tireRecommendation?.currentTires ->
                        "Рекомендуется смена! ${tireRecommendation?.recommendation}"
                    else -> "Всё в порядке"
                },
                icon = painterResource(id = R.drawable.ic_action_name),
                color = TileTires,
                expanded = expandedTires,
                onExpandChange = { expandedTires = it },
                onClick = {
                    showTireSelectionSheet = true
                    expandedTires = false
                },
                isWarning = TokenManager.shouldShowTireDialog() || (tireRecommendation?.shouldChangeTo != tireRecommendation?.currentTires)
            ) {
                val currentTires = TokenManager.getCurrentTires()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (currentTires) {
                            "summer"    -> "Сейчас установлена: Летняя резина"
                            "winter"    -> "Сейчас установлена: Зимняя резина"
                            "allseason" -> "Сейчас установлена: Всесезонная резина"
                            else        -> "Тип резины не указан"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentTires == null)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showTireSelectionSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showTireSelectionSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                ) {
                    TireTypeSelectionSheet(
                        currentType = TokenManager.getCurrentTires(),
                        onTypeSelected = { selectedType ->
                            TokenManager.setCurrentTires(selectedType)
                            showTireSelectionSheet = false
                            viewModel.checkTireRecommendation()
                        },
                        onDismiss = { showTireSelectionSheet = false }
                    )
                }
            }

            FeatureTile(
                title = "Расходы",
                subtitle = "За последний месяц",
                icon = rememberVectorPainter(Icons.Default.AttachMoney),
                color = TileExpenses,
                expanded = expandedExpenses,
                onExpandChange = { expandedExpenses = it },
                onClick = {
                    navController.navigate("expenses/${numberPlate}")
                }
            ) {
                Text(
                    text = "Итого: 14 200 ₽",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ТО • Топливо • Мойка",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            FeatureTile(
                title = "Пробег",
                subtitle = "За месяц",
                icon = rememberVectorPainter(Icons.Default.Speed),
                color = TileMileage,
                expanded = expandedMileage,
                onExpandChange = { expandedMileage = it },
                onClick = { navController.navigate("mileage/${numberPlate}") }
            ) {
                Text(
                    text = "1 240 км",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Средний расход: 8.2 л/100 км",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            FeatureTile(
                title = "Поиск автосервисов",
                subtitle = "Ближайшие СТО",
                icon = rememberVectorPainter(Icons.Default.LocationOn),
                color = TileService,
                expanded = false,
                onExpandChange = {},
                onClick = { navController.navigate("services/${numberPlate}") }
            )
        }
    }

    if (!locationPermissionsState.allPermissionsGranted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.LocationOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(24.dp))

            Text(
                "Для рекомендаций по замене резины нужен доступ к местоположению",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            if (locationPermissionsState.shouldShowRationale) {
                Text(
                    "Приложение использует ваше местоположение, чтобы определить температуру и сезон для рекомендаций по шинам.",
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text("Разрешить доступ")
                }
            } else {
                Text(
                    "Разрешение на местоположение было отклонено.\nПерейдите в настройки приложения → Разрешения → Местоположение → Разрешить всё время или только при использовании.",
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Открыть настройки")
                }
            }
        }
    }
}

@Composable
fun TireTypeSelectionSheet(
    currentType: String?,
    onTypeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val types = listOf(
        Triple("summer",    "Летняя",    Icons.Default.WbSunny),
        Triple("winter",    "Зимняя",    Icons.Default.AcUnit),
        Triple("allseason", "Всесезонная", Icons.Default.Autorenew)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (currentType == null) "Выберите тип резины" else "Изменить тип резины",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        types.forEach { (type, label, icon) ->
            val isSelected = currentType == type

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onTypeSelected(type) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                tonalElevation = if (isSelected) 4.dp else 0.dp,
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Выбрано",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = onDismiss) {
            Text("Отмена")
        }
    }
}




