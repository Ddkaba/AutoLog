package com.example.autolog_20.ui.theme.data.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autolog_20.R
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.AddCarFromSTSUiState
import com.example.autolog_20.ui.theme.data.model.DrivingSurvey
import com.example.autolog_20.ui.theme.data.model.STSRecognitionData
import com.example.autolog_20.ui.theme.data.model.SurveySummary
import com.example.autolog_20.ui.theme.data.model.viewmodel.AddCarFromSTSViewModel
import com.example.autolog_20.ui.theme.data.model.viewmodel.DrivingSurveyViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarFromSTSScreen(
    navController: NavController,
    stsData: STSRecognitionData
) {
    val viewModel: AddCarFromSTSViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddCarFromSTSViewModel(RetrofitClient.api) as T
            }
        }
    )

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val surveyViewModel: DrivingSurveyViewModel = viewModel()

    var mileageInput by remember { mutableStateOf("") }
    var yearInput by remember { mutableStateOf("") }
    var numberInput by remember { mutableStateOf(stsData.numberPlate) }
    var colorInput by remember { mutableStateOf(stsData.color) }
    var isChecking by remember { mutableStateOf(false) }
    var checkError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.processSTSData(stsData)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.data_from_sts_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back_button))
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
            when (val state = uiState) {
                AddCarFromSTSUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.checking_data))
                    }
                }

                is AddCarFromSTSUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text(stringResource(R.string.back_button))
                        }
                    }
                }

                is AddCarFromSTSUiState.Preview -> {
                    val data = state.data

                    Text(stringResource(R.string.confirm_data), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(24.dp))

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            DetailRow(stringResource(R.string.brand_label), data.brand)
                            DetailRow(stringResource(R.string.model_label), data.model)
                            DetailRow(stringResource(R.string.vin_label), data.vin)
                            DetailRow(stringResource(R.string.year_label_short), data.year.toString())
                            DetailRow(stringResource(R.string.color_label), data.color)
                            DetailRow(stringResource(R.string.plate_label), data.numberPlate)
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.confirmData() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.correct_continue))
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.enter_manually))
                    }
                }

                AddCarFromSTSUiState.EnterMileage -> {
                    Text(stringResource(R.string.car_info_title_short), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(24.dp))

                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val enterCorrectMileageMessage = stringResource(R.string.enter_correct_mileage)

                    OutlinedTextField(
                        value = mileageInput,
                        onValueChange = { mileageInput = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.mileage_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val mileage = mileageInput.toIntOrNull()
                            val year = if (yearInput.isNotBlank()) yearInput.toIntOrNull() else stsData.year

                            if (mileage != null && mileage > 0 && year != null && year in 1900..currentYear && numberInput.isNotBlank()) {
                                isChecking = true
                                viewModel.checkPlateAndContinue(
                                    number = numberInput,
                                    mileage = mileage,
                                    year = year,
                                    onError = { error ->
                                        isChecking = false
                                        checkError = error
                                    }
                                )
                            } else {
                                if (mileage == null || mileage <= 0) {
                                    Toast.makeText(context, enterCorrectMileageMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isChecking,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text(stringResource(R.string.continue_button))
                        }
                    }
                }

                AddCarFromSTSUiState.EnterColor -> {
                    var colorError by remember { mutableStateOf<String?>(null) }
                    val colorErrorMessage = stringResource(R.string.please_specify_color)

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.specify_car_color), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(24.dp))

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

                        Text(stringResource(R.string.popular_colors_title_short), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))

                        FlowRow(
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
                                    label = { Text(color) }
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(stringResource(R.string.or_enter_your_own_short), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = colorInput,
                            onValueChange = {
                                colorInput = it
                                colorError = null
                            },
                            label = { Text(stringResource(R.string.color_label)) },
                            placeholder = { Text(stsData.color) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = colorError != null,
                            supportingText = {
                                if (colorError != null) {
                                    Text(colorError!!, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        Spacer(Modifier.height(32.dp))

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
                            Text(stringResource(R.string.continue_button))
                        }
                    }
                }

                is DrivingSurvey -> {
                    val surveyState by surveyViewModel.surveyState.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        if (surveyState == null) {
                            surveyViewModel.startSurvey(
                                state.vinData,
                                state.mileage,
                                state.year,
                                state.number,
                                state.color
                            )
                        }
                    }

                    val currentSurvey = surveyState ?: state

                    DrivingSurveyScreen(
                        survey = currentSurvey,
                        viewModel = surveyViewModel,
                        onFinish = { summary ->
                            viewModel.goToSummary(summary)
                        }
                    )
                }

                is SurveySummary -> {
                    val context = LocalContext.current
                    var isSaving by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.confirm_data_title_short), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(24.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                DetailRow(stringResource(R.string.brand_label), state.vinData.brand)
                                DetailRow(stringResource(R.string.model_label), state.vinData.model)
                                DetailRow(stringResource(R.string.vin_label), state.vinData.vin)
                                DetailRow(stringResource(R.string.year_label_short), state.year.toString())
                                DetailRow(stringResource(R.string.plate_label), state.number)
                                DetailRow(stringResource(R.string.color_label), state.color)
                                DetailRow(stringResource(R.string.mileage_label), "${state.mileage} ${stringResource(R.string.km)}")
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                isSaving = true
                                viewModel.addCompleteCarWithData(
                                    surveySummary = state,
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
                                Text(stringResource(R.string.add_car_button_short))
                            }
                        }
                    }
                }

                AddCarFromSTSUiState.Success -> {
                    LaunchedEffect(Unit) {
                        delay(1500)
                        navController.navigate("main") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.car_added_success_short),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.redirecting_to_garage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = {
                                navController.navigate("main") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        ) {
                            Text(stringResource(R.string.return_button))
                        }
                    }
                }
            }
        }
    }
}