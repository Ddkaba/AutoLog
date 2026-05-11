package com.example.autolog_20.ui.theme.data.screen

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
import androidx.compose.ui.res.stringResource
import com.example.autolog_20.R
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
                title = { Text(stringResource(R.string.add_car_3)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
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
                        Text(stringResource(R.string.input_vin_car), style = MaterialTheme.typography.titleLarge)

                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = vinInput,
                            onValueChange = { vinInput = it.uppercase().filter { c -> c.isLetterOrDigit() } },
                            label = { Text(stringResource(R.string.vin_17_symbols)) },
                            placeholder = { Text(stringResource(R.string.vin_example)) },
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
                                            text = stringResource(R.string.exist_car),
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
                                Text(stringResource(R.string.check_vin_button))
                            }
                        }
                    }
                }

                is AddCarByVinUiState.Preview -> {
                    val preview = state.data
                    Text(stringResource(R.string.car_found_title), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(24.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            DetailRow(stringResource(R.string.brand_label), preview.brand)
                            DetailRow(stringResource(R.string.model_label), preview.model)
                            DetailRow(stringResource(R.string.vin_label), preview.vin)
                            DetailRow(stringResource(R.string.fuel_type_label),
                                when(preview.fuel) {
                                    "Petrol" -> stringResource(R.string.petrol)
                                    "Diesel" -> stringResource(R.string.diesel)
                                    "Electric" -> stringResource(R.string.electric)
                                    else -> preview.fuel
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(40.dp))

                    Text(stringResource(R.string.is_this_correct), style = MaterialTheme.typography.titleMedium)

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.confirmVin() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.yes_correct))
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetToInput() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.no_different_vin))
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
                            text = stringResource(R.string.car_info_title),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = mileageInput,
                            onValueChange = { mileageInput = it.filter { c -> c.isDigit() } },
                            label = { Text(stringResource(R.string.mileage_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = mileageInput.isNotBlank() && (mileageInput.toIntOrNull() == null || (mileageInput.toIntOrNull()
                                ?: 0) <= 0),
                            supportingText = {
                                if (mileageInput.isNotBlank() && (mileageInput.toIntOrNull() == null || (mileageInput.toIntOrNull()
                                        ?: 0) <= 0)
                                ) {
                                    Text(stringResource(R.string.mileage_error))
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                        OutlinedTextField(
                            value = yearInput,
                            onValueChange = { yearInput = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text(stringResource(R.string.year_label)) },
                            placeholder = { Text(stringResource(R.string.year_placeholder)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = yearInput.length == 4 && (yearInput.toIntOrNull() == null ||
                                    yearInput.toIntOrNull()!! < 1900 ||
                                    yearInput.toIntOrNull()!! > currentYear),
                            supportingText = {
                                if (yearInput.length == 4 && (yearInput.toIntOrNull() == null ||
                                            yearInput.toIntOrNull()!! < 1900 ||
                                            yearInput.toIntOrNull()!! > currentYear)) {
                                    Text(stringResource(R.string.year_error, currentYear))
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
                            label = { Text(stringResource(R.string.plate_label)) },
                            placeholder = { Text(stringResource(R.string.plate_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = (carNumberInput.isNotBlank() && !isNumberValid) || checkError != null,
                            supportingText = {
                                when {
                                    checkError != null -> Text(checkError!!, color = MaterialTheme.colorScheme.error)
                                    carNumberInput.isNotBlank() && !isNumberValid -> Text(stringResource(R.string.plate_example))
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
                                Text(stringResource(R.string.continue_button))
                            }
                        }
                    }
                }

                is AddCarByVinUiState.EnterColor -> {
                    var colorInput by remember { mutableStateOf("") }
                    var colorError by remember { mutableStateOf<String?>(null) }
                    val resources = LocalContext.current.resources

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.color_title),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.color_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        val suggestedColors = listOf(
                            stringResource(R.string.color_black),
                            stringResource(R.string.color_white),
                            stringResource(R.string.color_silver),
                            stringResource(R.string.color_gray),
                            stringResource(R.string.color_red),
                            stringResource(R.string.color_blue),
                            stringResource(R.string.color_green),
                            stringResource(R.string.color_yellow)
                        )

                        Text(
                            text = stringResource(R.string.popular_colors_title),
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
                            text = stringResource(R.string.or_enter_your_own),
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
                            label = { Text(stringResource(R.string.color_label)) },
                            placeholder = { Text(stringResource(R.string.color_hint)) },
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
                                    colorError = resources.getString(R.string.color_error)
                                } else {
                                    viewModel.saveColorAndContinue(colorInput)
                                }
                            },
                            enabled = colorInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.continue_button))
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
                                    text = stringResource(R.string.existing_car_found),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "${state.existingCar.brand} ${state.existingCar.model}\n" +
                                            "${stringResource(R.string.vin_label)}: ${state.existingCar.vin}\n" +
                                            "${stringResource(R.string.manufacture_year_label)}: ${state.existingCar.yearOfManufacture}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        viewModel.resetToInput()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.enter_different_plate))
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
                            text = stringResource(R.string.confirm_data_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(24.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = stringResource(R.string.basic_info_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(12.dp))
                                DetailRow(stringResource(R.string.brand_label), s.vinData.brand)
                                DetailRow(stringResource(R.string.model_label), s.vinData.model)
                                DetailRow(stringResource(R.string.vin_label), s.vinData.vin)
                                DetailRow(stringResource(R.string.fuel_type_label),
                                    when(s.fuelType) {
                                        "Petrol" -> stringResource(R.string.petrol)
                                        "Diesel" -> stringResource(R.string.diesel)
                                        "Electric" -> stringResource(R.string.electric)
                                        "Hybrid" -> stringResource(R.string.hybrid)
                                        else -> s.fuelType
                                    }
                                )
                                DetailRow(stringResource(R.string.mileage_label), "${s.mileage} ${stringResource(R.string.km)}")
                                DetailRow(stringResource(R.string.manufacture_year_label), "${s.year}")
                                DetailRow(stringResource(R.string.plate_label), s.number)
                                DetailRow(stringResource(R.string.color_label), s.color)
                                DetailRow(stringResource(R.string.drive_type_label),
                                    when(s.driveType) {
                                        "FWD" -> stringResource(R.string.fwd)
                                        "RWD" -> stringResource(R.string.rwd)
                                        "AWD" -> stringResource(R.string.awd)
                                        else -> stringResource(R.string.not_specified)
                                    }
                                )
                                DetailRow(stringResource(R.string.transmission_label),
                                    when(s.transmission) {
                                        "MT" -> stringResource(R.string.mt)
                                        "AT" -> stringResource(R.string.at)
                                        "CVT" -> stringResource(R.string.cvt)
                                        "AMT" -> stringResource(R.string.amt)
                                        else -> stringResource(R.string.not_specified)
                                    }
                                )
                                DetailRow(stringResource(R.string.oil_type_label),
                                    when (s.oilType) {
                                        "Synthetic" -> stringResource(R.string.synthetic_oil)
                                        "SemiSynthetic" -> stringResource(R.string.semi_synthetic_oil)
                                        "Mineral" -> stringResource(R.string.mineral_oil)
                                        else -> stringResource(R.string.not_specified)
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
                                Text(stringResource(R.string.add_car_button))
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
                            Text(stringResource(R.string.change_data_button))
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
                        Text(stringResource(R.string.car_added_success), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text(stringResource(R.string.back_to_cars))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
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