package com.example.autolog_20.ui.theme.data.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autolog_20.ui.theme.DeleteColor
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.MainUiState
import com.example.autolog_20.ui.theme.data.model.Trip
import com.example.autolog_20.ui.theme.data.model.response.CarResponse
import com.example.autolog_20.ui.theme.data.model.viewmodel.MainViewModel
import com.example.autolog_20.ui.theme.data.model.viewmodel.TripsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTripsScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val mainViewModel: MainViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(
                    authApi = RetrofitClient.api,
                ) as T
            }
        }
    )

    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val cars = when (uiState) {
        is MainUiState.Success -> (uiState as MainUiState.Success).cars
        else -> emptyList()
    }

    val tripsViewModel: TripsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TripsViewModel(context) as T
            }
        }
    )

    val unassignedTrips by tripsViewModel.unassignedTrips.collectAsStateWithLifecycle()
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var editingTrip by remember { mutableStateOf<Trip?>(null) }

    LaunchedEffect(Unit) {
        tripsViewModel.loadUnassignedTrips()
        mainViewModel.loadCars()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Непривязанные поездки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (unassignedTrips.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                tripsViewModel.deleteAllTrips(unassignedTrips)
                            }
                        ) {
                            Text("Удалить все")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (unassignedTrips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Нет непривязанных поездок",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Все поездки привязаны к автомобилям",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Назад")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(unassignedTrips) { trip ->
                    SwipeableTripCard(
                        trip = trip,
                        onAssign = { selectedTrip = trip },
                        onEdit = { editingTrip = trip },
                        onDelete = { tripsViewModel.deleteTrip(trip) }
                    )
                }
            }
        }
    }

    // Диалог привязки к автомобилю
    if (selectedTrip != null) {
        AssignTripDialog(
            trip = selectedTrip!!,
            cars = cars,
            onDismiss = { selectedTrip = null },
            onAssign = { carId, numberPlate ->
                tripsViewModel.assignTripToCar(selectedTrip!!.id, carId, numberPlate)
                selectedTrip = null
            }
        )
    }

    // Диалог редактирования пробега
    if (editingTrip != null) {
        EditTripDistanceDialog(
            trip = editingTrip!!,
            onDismiss = { editingTrip = null },
            onSave = { newDistance ->
                tripsViewModel.updateTripDistance(editingTrip!!, newDistance)
                editingTrip = null
            }
        )
    }
}

@Composable
fun SwipeableTripCard(
    trip: Trip,
    onAssign: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val maxSwipePx = with(density) { 160.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 80.dp.toPx()) {
                            onEdit()
                        } else if (offsetX < -80.dp.toPx()) {
                            onDelete()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-maxSwipePx, maxSwipePx)
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Редактировать", color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        // Фон для удаления (слева)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(DeleteColor),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                modifier = Modifier.padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Удалить", color = MaterialTheme.colorScheme.onError)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onError)
            }
        }

        // Основной контент
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = offsetX.toInt(), y = 0) }
        ) {
            TripCard(trip = trip, onClick = onAssign)
        }
    }
}

@Composable
fun TripCard(
    trip: Trip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    text = trip.startTime.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Расстояние: ${String.format("%.2f", trip.distance / 1000)} км",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Длительность: ${formatDuration(trip.duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Привязать",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EditTripDistanceDialog(
    trip: Trip,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var distanceInput by remember { mutableStateOf((trip.distance / 1000).toString()) }
    var distanceError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактирование пробега") },
        text = {
            Column {
                Text(
                    text = "Дата: ${trip.startTime}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = distanceInput,
                    onValueChange = {
                        distanceInput = it.filter { c -> c.isDigit() || c == '.' }
                        distanceError = null
                    },
                    label = { Text("Пробег (км)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = distanceError != null,
                    supportingText = {
                        if (distanceError != null) {
                            Text(distanceError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = distanceInput.toDoubleOrNull()
                    if (km == null || km <= 0) {
                        distanceError = "Введите корректный пробег"
                        return@Button
                    }
                    onSave(km * 1000) // конвертируем обратно в метры
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun AssignTripDialog(
    trip: Trip,
    cars: List<CarResponse>,
    onDismiss: () -> Unit,
    onAssign: (Int, String) -> Unit
) {
    var selectedCar by remember { mutableStateOf<CarResponse?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите автомобиль") },
        text = {
            Column {
                Text(
                    text = "Поездка: ${String.format("%.2f", trip.distance / 1000)} км",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Дата: ${trip.startTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                cars.forEach { car ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCar = car }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCar == car,
                            onClick = { selectedCar = car }
                        )
                        Text(
                            text = "${car.brand} ${car.model} (${car.numberPlate})",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCar?.let {
                        onAssign(it.id, it.numberPlate)
                    }
                },
                enabled = selectedCar != null
            ) {
                Text("Привязать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
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
