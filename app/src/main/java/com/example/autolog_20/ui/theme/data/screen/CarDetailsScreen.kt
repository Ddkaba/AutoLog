package com.example.autolog_20.ui.theme.data.screen

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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
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
import com.example.autolog_20.ui.theme.data.model.response.CarDetailResponse
import com.example.autolog_20.ui.theme.data.model.viewmodel.CarDetailsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.Manifest
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CarDetailsScreen(
    navController: NavController,
    numberPlate: String
) {
    val context = LocalContext.current
    val viewModel: CarDetailsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CarDetailsViewModel(RetrofitClient.api, numberPlate, context) as T
            }
        }
    )

    val carDetail by viewModel.carDetail.collectAsStateWithLifecycle()
    val showTireDialog by viewModel.showTireDialog.collectAsStateWithLifecycle()
    val tireRecommendation by viewModel.tireRecommendation.collectAsStateWithLifecycle()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsStateWithLifecycle()
    val isLoadingExpenses by viewModel.isLoadingExpenses.collectAsStateWithLifecycle()
    val nextServiceDistance by viewModel.nextServiceDistance.collectAsStateWithLifecycle()
    val currentMileage by viewModel.currentMileage.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()

    var expandedTires by remember { mutableStateOf(false) }
    var expandedExpenses by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var expandedTo by remember { mutableStateOf(false) }
    var expandedMileage by remember { mutableStateOf(false) }
    var showTireSelectionSheet by remember { mutableStateOf(false) }
    var showFirstTireSelection by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    val hasLocationPermission = locationPermissionsState.allPermissionsGranted

    LaunchedEffect(carDetail) {
        if (carDetail != null && monthlyExpenses == null && !isLoadingExpenses) {
            viewModel.loadMonthlyExpenses(carDetail!!.carId)
        }
    }

    LaunchedEffect(carDetail) {
        if (carDetail != null) {
            viewModel.loadCurrentMileageAndCalculateNextService(carDetail!!.carId)
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(hasLocationPermission, showTireDialog, carDetail) {
        if (hasLocationPermission && !showTireDialog && carDetail != null) {
            viewModel.checkTireRecommendation()
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            viewModel.fetchCurrentLocation()
        }
    }

    LaunchedEffect(expandedExpenses) {
        if (expandedExpenses && carDetail != null) {
            viewModel.loadMonthlyExpenses(carDetail!!.carId)
        }
    }

    LaunchedEffect(showTireDialog) {
        if (showTireDialog) {
            showFirstTireSelection = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.car_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_description))
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_description))
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
            carDetail?.let { CarHeader(car = it) } ?: run {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            Spacer(Modifier.height(16.dp))

            FeatureTile(
                title = stringResource(R.string.maintenance_to),
                subtitle = when {
                    nextServiceDistance == null -> stringResource(R.string.loading_dots)
                    nextServiceDistance!! <= 0 -> stringResource(R.string.maintenance_required)
                    else -> stringResource(R.string.until_maintenance, nextServiceDistance!!)
                },
                icon = rememberVectorPainter(Icons.Default.Build),
                color = TileTo,
                expanded = expandedTo,
                onExpandChange = { expandedTo = it },
                onClick = { navController.navigate("maintenance/${numberPlate}") },
                isClickable = true
            ) {
                Column {
                    if (currentMileage != null) {
                        Text(
                            text = stringResource(R.string.current_mileage_prefix, currentMileage!!),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    nextServiceDistance?.let {
                        Text(
                            text = if (it <= 0) {
                                stringResource(R.string.maintenance_overdue)
                            } else {
                                stringResource(R.string.recommended_maintenance, nextServiceDistance!!)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (it <= 0)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            FeatureTile(
                title = stringResource(R.string.tires_title),
                subtitle = when {
                    !hasLocationPermission -> stringResource(R.string.no_location_permission)
                    TokenManager.shouldShowTireDialog(numberPlate) -> stringResource(R.string.specify_tire_type)
                    tireRecommendation == null -> stringResource(R.string.checking_dots)
                    tireRecommendation?.shouldChangeTo != tireRecommendation?.currentTires ->
                        stringResource(R.string.tire_change_recommended, tireRecommendation?.recommendation ?: "")
                    else -> stringResource(R.string.tires_ok)
                },
                icon = painterResource(id = R.drawable.ic_action_name),
                color = TileTires,
                expanded = expandedTires,
                onExpandChange = { expandedTires = it },
                onClick = {
                    if (hasLocationPermission) {
                        showTireSelectionSheet = true
                        expandedTires = false
                    }
                },
                isWarning = !hasLocationPermission ||
                        TokenManager.shouldShowTireDialog(numberPlate) ||
                        (tireRecommendation?.shouldChangeTo != tireRecommendation?.currentTires),
                isClickable = hasLocationPermission
            ) {
                val currentTires = TokenManager.getCurrentTires(numberPlate)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (currentTires) {
                            "summer"    -> stringResource(R.string.tire_summer_installed)
                            "winter"    -> stringResource(R.string.tire_winter_installed)
                            "allseason" -> stringResource(R.string.tire_allseason_installed)
                            else        -> stringResource(R.string.tire_type_not_specified)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentTires == null)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showFirstTireSelection) {
                ModalBottomSheet(
                    onDismissRequest = { },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                ) {
                    TireTypeSelectionSheet(
                        currentType = null,
                        onTypeSelected = { selectedType ->
                            viewModel.setTireType(selectedType)
                            showFirstTireSelection = false
                        },
                        onDismiss = {
                            showFirstTireSelection = false
                        }
                    )
                }
            }

            if (showTireSelectionSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showTireSelectionSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                ) {
                    TireTypeSelectionSheet(
                        currentType = TokenManager.getCurrentTires(numberPlate),
                        onTypeSelected = { selectedType ->
                            TokenManager.setCurrentTires(numberPlate, selectedType)
                            showTireSelectionSheet = false
                            viewModel.checkTireRecommendation()
                        },
                        onDismiss = { showTireSelectionSheet = false }
                    )
                }
            }

            FeatureTile(
                title = stringResource(R.string.expenses_title),
                subtitle = when {
                    isLoadingExpenses -> stringResource(R.string.loading_dots)
                    monthlyExpenses != null -> stringResource(R.string.last_month)
                    else -> stringResource(R.string.no_expenses_data)
                },
                icon = rememberVectorPainter(Icons.Default.AttachMoney),
                color = TileExpenses,
                expanded = expandedExpenses,
                onExpandChange = { expandedExpenses = it },
                onClick = { navController.navigate("expenses/${numberPlate}") },
                isClickable = true
            ) {
                when {
                    isLoadingExpenses -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                    monthlyExpenses != null -> {
                        Text(
                            text = stringResource(R.string.total_expenses, monthlyExpenses!!.totalSpent),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (monthlyExpenses!!.categories.isNotEmpty()) {
                            Text(
                                text = monthlyExpenses!!.categories.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.no_expenses_last_month),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.no_expenses_data_text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            FeatureTile(
                title = stringResource(R.string.mileage_title),
                subtitle = if (!hasLocationPermission) stringResource(R.string.no_location_permission) else "",
                icon = rememberVectorPainter(Icons.Default.Speed),
                color = TileMileage,
                expanded = expandedMileage,
                onExpandChange = { expandedMileage = it },
                onClick = {
                    if (hasLocationPermission) {
                        navController.navigate("mileage/${numberPlate}")
                    }
                },
                isClickable = hasLocationPermission
            ) {
                if (currentMileage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.last_recorded_mileage, currentMileage!!),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FeatureTile(
                title = stringResource(R.string.services_title),
                subtitle = if (!hasLocationPermission) stringResource(R.string.no_location_permission) else stringResource(R.string.nearest_services),
                icon = rememberVectorPainter(Icons.Default.LocationOn),
                color = TileService,
                expanded = false,
                onExpandChange = {},
                onClick = {
                    if (hasLocationPermission && currentLocation != null) {
                        navController.navigate(
                            "services/${numberPlate}/${currentLocation!!.latitude}/${currentLocation!!.longitude}"
                        )
                    }
                },
                isClickable = hasLocationPermission
            )
        }
    }

    if (showEditSheet && carDetail != null) {
        val successMessage = stringResource(R.string.data_updated_success)
        EditCarBottomSheet(
            car = carDetail!!,
            onDismiss = { showEditSheet = false },
            onSave = { color, number ->
                viewModel.updateCarDetails(
                    color = color,
                    numberPlate = number,
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar(successMessage)
                        }
                        showEditSheet = false
                    },
                    onError = { error ->
                        scope.launch {
                            snackbarHostState.showSnackbar(error)
                        }
                    }
                )
            }
        )
    }
}

@Composable
fun TireTypeSelectionSheet(
    currentType: String?,
    onTypeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val types = listOf(
        Triple("summer",    stringResource(R.string.tire_summer),    Icons.Default.WbSunny),
        Triple("winter",    stringResource(R.string.tire_winter),    Icons.Default.AcUnit),
        Triple("allseason", stringResource(R.string.tire_allseason), Icons.Default.Autorenew)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (currentType == null) stringResource(R.string.select_tire_type) else stringResource(R.string.change_tire_type),
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
                            contentDescription = stringResource(R.string.selected_desc),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.later_button))
        }
    }
}

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
                text = stringResource(R.string.license_plate_prefix, car.numberPlate),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.year_of_manufacture_prefix, car.yearOfManufacture))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.color_prefix, car.color))
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
    color: Color,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    isWarning: Boolean = false,
    isClickable: Boolean = true,
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
                            contentDescription = if (expanded) stringResource(R.string.collapse_desc) else stringResource(R.string.expand_desc),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarBottomSheet(
    car: CarDetailResponse,
    onDismiss: () -> Unit,
    onSave: (color: String, numberPlate: String) -> Unit
) {
    var colorInput by remember { mutableStateOf(car.color) }
    var numberPlateInput by remember { mutableStateOf(car.numberPlate) }
    var colorError by remember { mutableStateOf<String?>(null) }
    var numberPlateError by remember { mutableStateOf<String?>(null) }

    val colorEmptyMessage = stringResource(R.string.color_empty_error)
    val plateEmptyMessage = stringResource(R.string.plate_empty_error)
    val plateInvalidMessage = stringResource(R.string.plate_invalid_error)

    fun isValidCarNumber(number: String): Boolean {
        val regex = Regex("^[АВЕКМНОРСТУХABEKMHOPCTYX]\\d{3}[АВЕКМНОРСТУХABEKMHOPCTYX]{2}\\d{2,3}$")
        return regex.matches(number.uppercase().trim())
    }

    fun isColorValid(color: String): Boolean {
        return color.trim().isNotBlank() && color.trim().length >= 2
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.edit_car_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = colorInput,
                onValueChange = {
                    colorInput = it
                    colorError = null
                },
                label = { Text(stringResource(R.string.color_label_edit)) },
                placeholder = { Text(stringResource(R.string.color_placeholder_edit)) },
                modifier = Modifier.fillMaxWidth(),
                isError = colorError != null,
                supportingText = {
                    if (colorError != null) {
                        Text(colorError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = numberPlateInput,
                onValueChange = {
                    numberPlateInput = it.uppercase()
                    numberPlateError = null
                },
                label = { Text(stringResource(R.string.plate_label_edit)) },
                placeholder = { Text(stringResource(R.string.plate_placeholder_edit)) },
                modifier = Modifier.fillMaxWidth(),
                isError = numberPlateError != null,
                supportingText = {
                    when {
                        numberPlateError != null -> Text(
                            numberPlateError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        numberPlateInput.isNotBlank() && !isValidCarNumber(numberPlateInput) -> Text(
                            stringResource(R.string.plate_example_edit),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel_button_edit))
                }

                Button(
                    onClick = {
                        var hasError = false

                        if (!isColorValid(colorInput)) {
                            colorError = colorEmptyMessage
                            hasError = true
                        }

                        if (numberPlateInput.isBlank()) {
                            numberPlateError = plateEmptyMessage
                            hasError = true
                        } else if (!isValidCarNumber(numberPlateInput)) {
                            numberPlateError = plateInvalidMessage
                            hasError = true
                        }

                        if (!hasError) {
                            onSave(colorInput.trim(), numberPlateInput.trim().uppercase())
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = colorInput.isNotBlank() && numberPlateInput.isNotBlank()
                ) {
                    Text(stringResource(R.string.save_button_edit))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}