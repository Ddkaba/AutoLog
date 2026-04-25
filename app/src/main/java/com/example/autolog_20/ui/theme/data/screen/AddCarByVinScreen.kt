package com.example.autolog_20.ui.theme.data.screen

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.AddCarByVinUiState
import com.example.autolog_20.ui.theme.data.model.DrivingSurvey
import com.example.autolog_20.ui.theme.data.model.SurveySummary
import com.example.autolog_20.ui.theme.data.model.viewmodel.AddCarByVinViewModel
import com.example.autolog_20.ui.theme.data.model.viewmodel.DrivingSurveyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarByVinScreen(navController: NavController) {
    val viewModel: AddCarByVinViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddCarByVinViewModel(RetrofitClient.api) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val surveyViewModel: DrivingSurveyViewModel = viewModel()

    var vinInput by remember { mutableStateOf("") }
    var mileageInput by remember { mutableStateOf("") }
    var yearInput by remember { mutableStateOf("") }
    var carNumberInput by remember { mutableStateOf("") }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is AddCarByVinUiState.Input,
                is AddCarByVinUiState.Error,
                is AddCarByVinUiState.VinAlreadyExistsError,
                is AddCarByVinUiState.Loading -> {
                    Column {
                        Text("Введите VIN автомобиля", style = MaterialTheme.typography.titleLarge)

                        Spacer(Modifier.height(32.dp))

                        OutlinedTextField(
                            value = vinInput,
                            onValueChange = { vinInput = it.uppercase().filter { c -> c.isLetterOrDigit() } },
                            label = { Text("VIN (17 символов)") },
                            placeholder = { Text("WDB2110231A709926") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = state is AddCarByVinUiState.VinAlreadyExistsError || state is AddCarByVinUiState.Error
                        )

                        when (state) {
                            is AddCarByVinUiState.Error -> {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                            is AddCarByVinUiState.VinAlreadyExistsError -> {
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
                                            text = "Найден существующий автомобиль:",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            text = "${state.existingCar.brand} ${state.existingCar.model}\n",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.checkVin(vinInput) },
                            enabled = vinInput.length == 17 && state !is AddCarByVinUiState.Loading,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            if (state is AddCarByVinUiState.Loading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Проверить VIN")
                            }
                        }
                    }
                }

                is AddCarByVinUiState.Preview -> {
                    val preview = state.data
                    Text("Найден автомобиль", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(24.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            DetailRow("Марка", preview.brand)
                            DetailRow("Модель", preview.model)
                            DetailRow("VIN", preview.vin)
                            DetailRow("Тип топлива",
                                when(preview.fuel) {
                                    "Petrol" -> "Бензин"
                                    "Diesel" -> "Дизель"
                                    "Electric" -> "Электро"
                                    else -> preview.fuel
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(40.dp))

                    Text("Это правильный автомобиль?", style = MaterialTheme.typography.titleMedium)

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.confirmVin() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Да, верно")
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetToInput() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Нет, другой VIN")
                        }
                    }
                }

                is AddCarByVinUiState.EnterMileage -> {
                    fun isValidCarNumber(number: String): Boolean {
                        val regex = Regex("^[АВЕКМНОРСТУХABEKMHOPCTYX]\\d{3}[АВЕКМНОРСТУХABEKMHOPCTYX]{2}\\d{2,3}$")
                        return regex.matches(number.uppercase())
                    }

                    var isChecking by remember { mutableStateOf(false) }
                    var checkError by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Информация об автомобиле",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = mileageInput,
                            onValueChange = { mileageInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Пробег (км)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = mileageInput.isNotBlank() && (mileageInput.toIntOrNull() == null || (mileageInput.toIntOrNull()
                                ?: 0) <= 0),
                            supportingText = {
                                if (mileageInput.isNotBlank() && (mileageInput.toIntOrNull() == null || (mileageInput.toIntOrNull()
                                        ?: 0) <= 0)
                                ) {
                                    Text("Введите корректный пробег")
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                        OutlinedTextField(
                            value = yearInput,
                            onValueChange = { yearInput = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("Год выпуска (4 цифры)") },
                            placeholder = { Text("2020") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = yearInput.length == 4 && (yearInput.toIntOrNull() == null ||
                                    yearInput.toIntOrNull()!! < 1900 ||
                                    yearInput.toIntOrNull()!! > currentYear),
                            supportingText = {
                                if (yearInput.length == 4 && (yearInput.toIntOrNull() == null ||
                                            yearInput.toIntOrNull()!! < 1900 ||
                                            yearInput.toIntOrNull()!! > currentYear)) {
                                    Text("Введите корректный год (1900-$currentYear)")
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val isNumberValid = isValidCarNumber(carNumberInput)
                        OutlinedTextField(
                            value = carNumberInput,
                            onValueChange = {
                                carNumberInput = it.uppercase()
                                checkError = null
                            },
                            label = { Text("Номер автомобиля") },
                            placeholder = { Text("A000AA78") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = (carNumberInput.isNotBlank() && !isNumberValid) || checkError != null,
                            supportingText = {
                                when {
                                    checkError != null -> Text(checkError!!, color = MaterialTheme.colorScheme.error)
                                    carNumberInput.isNotBlank() && !isNumberValid -> Text("Пример правильного номера: А123БВ777")
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        val isMileageValid = mileageInput.isNotBlank() &&
                                (mileageInput.toIntOrNull() != null) &&
                                (mileageInput.toIntOrNull() ?: 0) > 0

                        val isYearValid = yearInput.length == 4 &&
                                (yearInput.toIntOrNull() != null) &&
                                (yearInput.toIntOrNull()!! in 1900..currentYear)

                        val isFormValid = isMileageValid && isYearValid && isNumberValid && !isChecking

                        Button(
                            onClick = {
                                val mileage = mileageInput.toIntOrNull()
                                val year = yearInput.toIntOrNull()

                                if (mileage != null && year != null && isNumberValid) {
                                    viewModel.checkPlateAndContinue(
                                        number = carNumberInput,
                                        mileage = mileage,
                                        year = year,
                                        onError = { error ->
                                            checkError = error
                                        }
                                    )
                                }
                            },
                            enabled = isFormValid,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isChecking) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text("Продолжить")
                            }
                        }
                    }
                }

                is AddCarByVinUiState.EnterColor -> {
                    var colorInput by remember { mutableStateOf("") }
                    var colorError by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
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

                is AddCarByVinUiState.PlateAlreadyExistsError -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
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
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "Найден существующий автомобиль:",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "${state.existingCar.brand} ${state.existingCar.model}\n" +
                                            "VIN: ${state.existingCar.vin}\n" +
                                            "Год: ${state.existingCar.yearOfManufacture}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        viewModel.resetToInput()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ввести другой номер")
                                }
                            }
                        }
                    }
                }

                is DrivingSurvey -> {
                    val surveyState by surveyViewModel.surveyState.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        if (surveyState == null) {
                            surveyViewModel.startSurvey(
                                vinData = state.vinData,
                                mileage = state.mileage,
                                year = state.year,
                                number = state.number,
                                color = state.color
                            )
                        }
                    }

                    val currentSurvey = surveyState ?: state

                    DrivingSurveyScreen(
                        survey = currentSurvey,
                        viewModel = surveyViewModel,
                        addCarByVinViewModel = viewModel,
                    )
                }


                is SurveySummary -> {
                    val s = state
                    val context = LocalContext.current
                    var isSaving by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Подтверждение данных",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(24.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Основная информация",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(12.dp))
                                DetailRow("Марка", s.vinData.brand)
                                DetailRow("Модель", s.vinData.model)
                                DetailRow("VIN", s.vinData.vin)
                                DetailRow("Тип топлива",
                                    when(s.fuelType) {
                                        "Petrol" -> "Бензин"
                                        "Diesel" -> "Дизель"
                                        "Electric" -> "Электро"
                                        "Hybrid" -> "Гибрид"
                                        else -> s.fuelType
                                    }
                                )
                                DetailRow("Пробег", "${s.mileage} км")
                                DetailRow("Год выпуска", "${s.year}")
                                DetailRow("Номер", s.number)
                                DetailRow("Цвет", s.color)
                                DetailRow("Привод",
                                    when(s.driveType) {
                                        "FWD" -> "Передний"
                                        "RWD" -> "Задний"
                                        "AWD" -> "Полный"
                                        else -> "Не указан"
                                    }
                                )
                                DetailRow("Коробка передач",
                                    when(s.transmission) {
                                        "MT" -> "Механика"
                                        "AT" -> "Автомат"
                                        "CVT" -> "Вариатор"
                                        "AMT" -> "Робот"
                                        else -> "Не указана"
                                    }
                                )
                                DetailRow("Тип масла",
                                    when (s.oilType) {
                                        "Synthetic" -> "Синтетика"
                                        "SemiSynthetic" -> "Полусинтетика"
                                        "Mineral" -> "Минеральное"
                                        else -> "Не указан"
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                isSaving = true
                                viewModel.addCompleteCarWithData(
                                    surveySummary = s,
                                    onSuccess = {
                                        isSaving = false
                                        viewModel.confirmAllData()
                                    },
                                    onError = { errorMessage ->
                                        isSaving = false
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
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
                            onClick = {
                                viewModel.resetToInput()
                                isSaving = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Изменить данные")
                        }
                    }
                }

                is AddCarByVinUiState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Автомобиль успешно добавлен!", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Вернуться к списку автомобилей")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(question: String, options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Column {
        Text(
            text = question,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
        options.forEachIndexed { index, option ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelect(index) },
                colors = CardDefaults.cardColors(
                    containerColor = if (index == selectedIndex)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = index == selectedIndex,
                        onClick = { onSelect(index) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}