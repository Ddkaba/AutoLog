package com.example.autolog_20.ui.theme.data.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
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
import com.example.autolog_20.R
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
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

    val defaultFuelType = stringResource(R.string.fuel_petrol)

    var vinInput by remember { mutableStateOf("") }
    var brandInput by remember { mutableStateOf("") }
    var modelInput by remember { mutableStateOf("") }
    var fuelTypeInput by remember { mutableStateOf(defaultFuelType) }
    var mileageInput by remember { mutableStateOf("") }
    var yearInput by remember { mutableStateOf("") }
    var numberInput by remember { mutableStateOf("") }
    var colorInput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val fuelTypes = listOf(
        stringResource(R.string.fuel_petrol),
        stringResource(R.string.fuel_diesel),
        stringResource(R.string.fuel_hybrid)
    )
    val fuelTypeMap = mapOf(
        stringResource(R.string.fuel_petrol) to "Petrol",
        stringResource(R.string.fuel_diesel) to "Diesel",
        stringResource(R.string.fuel_hybrid) to "Hybrid"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_car_manual_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back_button_manual))
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
                is AddCarManualUiState.Input,
                is AddCarManualUiState.Error,
                is AddCarManualUiState.Loading,
                is AddCarManualUiState.VinAlreadyExistsError -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(stringResource(R.string.enter_car_data), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(24.dp))

                        OutlinedTextField(
                            value = vinInput,
                            onValueChange = { vinInput = it.uppercase().filter { c -> c.isLetterOrDigit() } },
                            label = { Text(stringResource(R.string.vin_17_symbols_hint)) },
                            placeholder = { Text(stringResource(R.string.vin_placeholder_manual)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = brandInput,
                            onValueChange = { brandInput = it },
                            label = { Text(stringResource(R.string.brand_label_manual)) },
                            placeholder = { Text(stringResource(R.string.brand_placeholder)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = modelInput,
                            onValueChange = { modelInput = it },
                            label = { Text(stringResource(R.string.model_label_manual)) },
                            placeholder = { Text(stringResource(R.string.model_placeholder)) },
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
                                label = { Text(stringResource(R.string.fuel_type_label_manual)) },
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
                                            text = stringResource(R.string.existing_car_found_manual) + "\n" +
                                                    "${state.existingCar.brand} ${state.existingCar.model}\n" +
                                                    "${stringResource(R.string.year_label_manual)}: ${state.existingCar.year_of_manufacture}\n" +
                                                    "${stringResource(R.string.plate_label_manual)}: ${state.existingCar.number_plate}",
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
                                Text(stringResource(R.string.continue_button_manual))
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
                        Text(stringResource(R.string.check_data_title), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(24.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                DetailRow(stringResource(R.string.brand_label_manual), preview.brand)
                                DetailRow(stringResource(R.string.model_label_manual), preview.model)
                                DetailRow(stringResource(R.string.vin_label), preview.vin)
                                DetailRow(stringResource(R.string.fuel_type_label_manual), fuelTypeInput)
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.confirmCar() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.correct_continue_manual))
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.resetToInput() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.change_data_manual))
                        }
                    }
                }

                is AddCarManualUiState.EnterMileage -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(stringResource(R.string.car_info_manual), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(24.dp))

                        OutlinedTextField(
                            value = mileageInput,
                            onValueChange = { mileageInput = it.filter { c -> c.isDigit() } },
                            label = { Text(stringResource(R.string.mileage_label_manual)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = yearInput,
                            onValueChange = { yearInput = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text(stringResource(R.string.year_label_manual)) },
                            placeholder = { Text(stringResource(R.string.year_placeholder_manual)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = numberInput,
                            onValueChange = { numberInput = it.uppercase() },
                            label = { Text(stringResource(R.string.plate_label_manual)) },
                            placeholder = { Text(stringResource(R.string.plate_placeholder_manual)) },
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
                            Text(stringResource(R.string.continue_button_manual))
                        }
                    }
                }

                is AddCarManualUiState.EnterColor -> {
                    var colorError by remember { mutableStateOf<String?>(null) }
                    val colorErrorMessage = stringResource(R.string.please_specify_color_manual)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = stringResource(R.string.specify_car_color_manual),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.color_description_manual),
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
                            text = stringResource(R.string.popular_colors_manual),
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
                            text = stringResource(R.string.or_enter_your_own_manual),
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
                            placeholder = { Text(stringResource(R.string.color_hint_manual)) },
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
                                    colorError = colorErrorMessage
                                } else {
                                    viewModel.saveColorAndContinue(colorInput)
                                }
                            },
                            enabled = colorInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.continue_button_manual))
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
                        Text(stringResource(R.string.confirm_data_manual), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(24.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = stringResource(R.string.basic_info_manual),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(12.dp))
                                DetailRow(stringResource(R.string.brand_label_manual), state.summary.vinData.brand)
                                DetailRow(stringResource(R.string.model_label_manual), state.summary.vinData.model)
                                DetailRow(stringResource(R.string.vin_label), state.summary.vinData.vin)
                                DetailRow(stringResource(R.string.fuel_type_label_manual), state.summary.fuelType)
                                DetailRow(stringResource(R.string.mileage_label_manual), "${state.summary.mileage} ${stringResource(R.string.km)}")
                                DetailRow(stringResource(R.string.year_label_manual), "${state.summary.year}")
                                DetailRow(stringResource(R.string.plate_label_manual), state.summary.number)
                                DetailRow(stringResource(R.string.color_label), state.summary.color)
                                DetailRow(stringResource(R.string.drive_type_manual), state.summary.driveType ?: stringResource(R.string.not_specified_manual))
                                DetailRow(stringResource(R.string.transmission_manual), state.summary.transmission ?: stringResource(R.string.not_specified_female))
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
                                Text(stringResource(R.string.add_car_button_manual))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.resetToInput() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.change_data_manual))
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
                        Text(stringResource(R.string.car_added_success_manual), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}