package com.example.autolog_20.ui.theme.data.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autolog_20.R
import com.example.autolog_20.ui.theme.DeleteColor
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.MainUiState
import com.example.autolog_20.ui.theme.data.model.response.CarResponse
import com.example.autolog_20.ui.theme.data.model.viewmodel.MainViewModel
import com.example.autolog_20.ui.theme.data.model.viewmodel.TripsViewModel
import com.example.autolog_20.ui.theme.data.tracking.Trip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(RetrofitClient.api) as T
            }
        }
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAddOptions by remember { mutableStateOf(false) }

    var showUnassignedTripsSheet by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }

    val tripsViewModel: TripsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TripsViewModel(context) as T
            }
        }
    )

    val unassignedTrips by tripsViewModel.unassignedTrips.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadCars()
        tripsViewModel.loadUnassignedTrips()
    }

    LaunchedEffect(unassignedTrips) {
        if (unassignedTrips.isNotEmpty() && !showUnassignedTripsSheet && selectedTrip == null) {
            showUnassignedTripsSheet = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_cars)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.logout(navController) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.exit),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddOptions = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_car)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is MainUiState.Empty -> {
                    EmptyCarsContent(
                        modifier = Modifier.fillMaxSize(),
                        onAddClick = { showAddOptions = true }
                    )
                }

                is MainUiState.Success -> {
                    CarsList(
                        cars = state.cars,
                        navController = navController,
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        scope = scope,
                        context = context,
                        modifier = Modifier.fillMaxSize()
                    )
                    FloatingActionButton(
                        onClick = { navController.navigate("settings") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }

                is MainUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadCars() }) {
                                Text(stringResource(R.string.repeat))
                            }
                        }
                    }
                }
                MainUiState.Unauthorized -> {
                    val sessionExpiredText = stringResource(R.string.session_expired)

                    LaunchedEffect(Unit) {
                        scope.launch {
                            snackbarHostState.showSnackbar(sessionExpiredText)
                        }
                        delay(1500)
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        if (showAddOptions) {
            ModalBottomSheet(
                onDismissRequest = { showAddOptions = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AddCarOptionsBottomSheet(
                    onDismiss = { showAddOptions = false },
                    onVinClick = {
                        showAddOptions = false
                        navController.navigate("add_car_vin")
                    },
                    onScanStsClick = {
                        showAddOptions = false
                        navController.navigate("add_car_scan_sts")
                    },
                    onManualClick = {
                        showAddOptions = false
                        navController.navigate("add_car_manual")
                    }
                )
            }
        }

        // Bottom sheet для списка непривязанных поездок
        if (showUnassignedTripsSheet && unassignedTrips.isNotEmpty()) {
            ModalBottomSheet(
                onDismissRequest = { showUnassignedTripsSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ) {
                UnassignedTripsListSheet(
                    trips = unassignedTrips,
                    onTripClick = { trip ->
                        showUnassignedTripsSheet = false
                        selectedTrip = trip
                    },
                    onDismiss = { showUnassignedTripsSheet = false }
                )
            }
        }

        // Bottom sheet с деталями поездки и привязкой к авто
        if (selectedTrip != null) {
            val cars = (uiState as? MainUiState.Success)?.cars ?: emptyList()
            ModalBottomSheet(
                onDismissRequest = { selectedTrip = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ) {
                TripDetailsSheet(
                    trip = selectedTrip!!,
                    cars = cars,
                    onAssign = { numberPlate ->
                        tripsViewModel.addMileageAndDeleteTrip(
                            numberPlate = numberPlate,
                            tripId = selectedTrip!!.id,
                            onSuccess = {
                                selectedTrip = null
                                scope.launch {
                                    snackbarHostState.showSnackbar("✅ Пробег добавлен для $numberPlate")
                                }
                            },
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("❌ Ошибка: $error")
                                }
                            }
                        )
                    },
                    onDismiss = { selectedTrip = null }
                )
            }
        }
    }
}

@Composable
private fun UnassignedTripsListSheet(
    trips: List<Trip>,
    onTripClick: (Trip) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Непривязанные поездки",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Обнаружено ${trips.size} поездок, которые не привязаны к автомобилю",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(400.dp)
        ) {
            items(trips) { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTripClick(trip) },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📅 ${dateFormat.format(trip.startTime)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📏 ${String.format("%.2f", trip.distance / 1000)} км",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "⏱️ ${formatDuration(trip.duration)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Закрыть")
        }
    }
}

@Composable
private fun TripDetailsSheet(
    trip: Trip,
    cars: List<CarResponse>,
    onAssign: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    var selectedCar by remember { mutableStateOf<CarResponse?>(null) }
    var isAssigning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Детали поездки",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                DetailRow("Дата и время", dateFormat.format(trip.startTime))
                DetailRow("Расстояние", "${String.format("%.2f", trip.distance / 1000)} км")
                DetailRow("Длительность", formatDuration(trip.duration))
                DetailRow("Средняя скорость", "${String.format("%.1f", trip.averageSpeed)} км/ч")
                DetailRow("Макс. скорость", "${String.format("%.1f", trip.maxSpeed)} км/ч")
                if (trip.endTime != null) {
                    DetailRow("Окончание", dateFormat.format(trip.endTime))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Привязать к автомобилю",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (cars.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "😕 Нет добавленных автомобилей",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Добавьте автомобиль, чтобы привязать поездку",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cars) { car ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isAssigning) {
                                    selectedCar = car
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCar == car)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${car.brand} ${car.model}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = car.numberPlate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selectedCar == car) {
                                Text(
                                    text = "✓",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                enabled = !isAssigning
            ) {
                Text("Отмена")
            }

            Button(
                onClick = {
                    selectedCar?.let { car ->
                        isAssigning = true
                        val carIdentifier = car.id ?: 0
                        if (carIdentifier > 0) {
                            onAssign(car.numberPlate)
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = selectedCar != null && !isAssigning
            ) {
                if (isAssigning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Привязка...")
                    }
                } else {
                    Text("Привязать")
                }
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "$hours ч $minutes мин"
        minutes > 0 -> "$minutes мин"
        else -> "меньше минуты"
    }
}

@Composable
private fun CarsList(
    cars: List<CarResponse>,
    navController: NavController,
    viewModel: MainViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    context: Context,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cars) { car ->
            SwipeToDeleteCard(
                car = car,
                navController = navController,
                viewModel = viewModel,
                onDelete = { numberPlate ->
                    viewModel.deleteCarByNumberPlate(
                        numberPlate = numberPlate,
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.delete_car))
                            }
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
}

@Composable
private fun SwipeToDeleteCard(
    car: CarResponse,
    navController: NavController,
    viewModel: MainViewModel,
    onDelete: (String) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val isDeleting by viewModel.isDeleting.collectAsStateWithLifecycle()

    LaunchedEffect(showDeleteDialog) {
        if (!showDeleteDialog) {
            offsetX = 0f
        }
    }

    val maxSwipePx = with(density) { 80.dp.toPx() }
    val deleteThresholdPx = with(density) { 50.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(offsetX) > deleteThresholdPx && !isDeleting) {
                            showDeleteDialog = true
                        }
                        scope.launch {
                            delay(100)
                            offsetX = 0f
                        }
                    },
                    onDragCancel = { offsetX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        if (!isDeleting) {
                            offsetX = (offsetX + dragAmount).coerceIn(-maxSwipePx, 0f)
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(DeleteColor),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onError
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(end = 24.dp).size(32.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = offsetX.toInt(), y = 0) }
        ) {
            CarCard(
                car = car,
                navController = navController,
                modifier = Modifier
            )
        }
    }

    if (showDeleteDialog && !isDeleting) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.removing_vehicle)) },
            text = { Text(stringResource(R.string.delete_car_confirmation, car.brand, car.model, car.numberPlate)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(car.numberPlate)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeleteColor
                    ),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(stringResource(R.string.delete))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeleting
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun CarCard(
    car: CarResponse,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { navController.navigate("car_details/${car.numberPlate}") },
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${car.brand} ${car.model}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = car.numberPlate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyCarsContent(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.no_cars),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.first_car),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onAddClick) {
            Text(stringResource(R.string.add_car_2))
        }
    }
}

@Composable
fun AddCarOptionsBottomSheet(
    onDismiss: () -> Unit,
    onVinClick: () -> Unit,
    onScanStsClick: () -> Unit,
    onManualClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.add_car_2),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        AddOptionItem(
            icon = Icons.Default.QrCodeScanner,
            title = stringResource(R.string.input_vin),
            description =  stringResource(R.string.digit_code_17),
            onClick = onVinClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        AddOptionItem(
            icon = Icons.Default.CameraAlt,
            title = stringResource(R.string.scan_sts),
            description = stringResource(R.string.camera_phone),
            onClick = onScanStsClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        AddOptionItem(
            icon = Icons.Default.Edit,
            title = stringResource(R.string.fill_manually),
            description = stringResource(R.string.transfer),
            onClick = onManualClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = onDismiss) {
            Text( stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}



@Composable
private fun AddOptionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}