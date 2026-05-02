package com.example.autolog_20.ui.theme.data.screen

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autolog_20.ui.theme.DeleteColor
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.locale.TokenManager
import com.example.autolog_20.ui.theme.data.model.DateFormat
import com.example.autolog_20.ui.theme.data.model.MaintenanceUiState
import com.example.autolog_20.ui.theme.data.model.ServiceRecord
import com.example.autolog_20.ui.theme.data.model.response.RecommendationResponse
import com.example.autolog_20.ui.theme.data.model.viewmodel.MaintenanceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    navController: NavController,
    numberPlate: String
) {
    val context = LocalContext.current
    val viewModel: MaintenanceViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MaintenanceViewModel(RetrofitClient.api, numberPlate, context) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentMileage by viewModel.currentMileage.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("completed") }
    var selectedRecommendation by remember { mutableStateOf<RecommendationResponse?>(null) }
    var editingRecommendation by remember { mutableStateOf<RecommendationResponse?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf<ServiceRecord?>(null) }
    var editingService by remember { mutableStateOf<ServiceRecord?>(null) }
    var showAddServiceSheet by remember { mutableStateOf(false) }
    val recommendationsForSheet by viewModel.recommendationsForSheet.collectAsStateWithLifecycle()


    var receiptUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filters = listOf(
        "completed" to "Выполненные",
        "planned" to "Запланированные"
    )

    val currentFilterTitle =
        filters.find { it.first == selectedFilter }?.second ?: "Выполненные"

    LaunchedEffect(Unit) {
        val isFirstTime = TokenManager.isFirstTimeOnMaintenance(numberPlate)
        if (isFirstTime) {
            showInfoDialog = true
            TokenManager.setMaintenanceInfoShown(numberPlate)
        }
    }

    LaunchedEffect(showAddServiceSheet) {
        if (showAddServiceSheet) {
            viewModel.loadRecommendationsForSheet()
        }
    }

    LaunchedEffect(selectedFilter) {
        when (selectedFilter) {
            "planned" -> viewModel.loadPlannedRecommendations()
            "completed" -> viewModel.loadCompletedServices()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ТО") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable { expanded = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentFilterTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Фильтр",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            filters.forEach { (value, title) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = title,
                                            color = if (selectedFilter == value)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        selectedFilter = value
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedFilter == value) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                when (uiState) {
                    MaintenanceUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is MaintenanceUiState.Planned -> {
                        val recommendations =
                            (uiState as MaintenanceUiState.Planned).recommendations

                        if (recommendations.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.Build,
                                title = "Нет запланированных работ",
                                message = "Рекомендации появятся после добавления пробега"
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(recommendations) { recommendation ->
                                    PlannedRecommendationCard(
                                        recommendation = recommendation,
                                        currentMileage = currentMileage,
                                        onClick = { selectedRecommendation = recommendation }
                                    )
                                }
                            }
                        }
                    }

                    is MaintenanceUiState.Completed -> {
                        val services = (uiState as MaintenanceUiState.Completed).services

                        if (services.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.Build,
                                title = "Нет выполненных работ",
                                message = "Выполненные работы будут отображаться здесь"
                            )
                        } else {
                            val groupedServices = services
                                .groupBy { it.date }
                                .toSortedMap(reverseOrder())

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                groupedServices.forEach { (date, servicesOnDate) ->
                                    item(key = "header_$date") {
                                        Text(
                                            text = DateFormat.formatDateHeader(date),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                        )
                                    }

                                    items(
                                        items = servicesOnDate,
                                        key = { it.recordId }
                                    ) { service ->
                                        SwipeToDeleteServiceCard(
                                            service = service,
                                            onClick = { selectedService = service },
                                            onDelete = {
                                                viewModel.deleteService(
                                                    service = service,
                                                    onSuccess = {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Запись ТО удалена")
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

                                    item(key = "spacer_$date") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }

                    is MaintenanceUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = (uiState as MaintenanceUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                if (selectedFilter == "planned") {
                                    viewModel.loadPlannedRecommendations()
                                } else {
                                    viewModel.loadCompletedServices()
                                }
                            }) {
                                Text("Повторить")
                            }
                        }
                    }
                }
            }

            if (selectedFilter == "completed") {
                FloatingActionButton(
                    onClick = { showAddServiceSheet = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить запись ТО"
                    )
                }
            }

            if (selectedService != null) {
                ServiceDetailBottomSheet(
                    service = selectedService!!,
                    onDismiss = { selectedService = null },
                    onViewReceipt = { photoUrl ->
                        receiptUrl = photoUrl
                        selectedService = null
                    },
                    onEdit = {
                        editingService = selectedService
                        selectedService = null
                    }
                )
            }

            if (receiptUrl != null) {
                ReceiptWebViewDialog(
                    imageUrl = receiptUrl!!,
                    onDismiss = { receiptUrl = null }
                )
            }

            if (editingService != null) {
                EditServiceBottomSheet(
                    service = editingService!!,
                    onDismiss = { editingService = null },
                    onSave = { date, mileage, cost, notes ->
                        viewModel.updateService(
                            service = editingService!!,
                            date = date,
                            mileage = mileage,
                            cost = cost,
                            notes = notes,
                            onSuccess = {
                                editingService = null
                                scope.launch {
                                    snackbarHostState.showSnackbar("Данные ТО обновлены")
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

            if (showInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showInfoDialog = false },
                    title = {
                        Text(
                            text = "Добро пожаловать!",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Для более точных рекомендаций по техническому обслуживанию добавьте историю предыдущих ТО.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Чем больше данных, тем точнее будут расчеты интервалов обслуживания.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showInfoDialog = false
                        }) {
                            Text("Понятно")
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }

            if (showAddServiceSheet) {
                AddServiceBottomSheet(
                    recommendations = recommendationsForSheet,
                    currentMileage = currentMileage,  // Добавляем передачу текущего пробега
                    onDismiss = { showAddServiceSheet = false },
                    onAdd = { serviceType, date, mileage, cost, notes, recommendationId, photoUri ->
                        viewModel.addServiceRecord(
                            serviceType = serviceType,
                            date = date,
                            mileage = mileage,
                            cost = cost,
                            notes = notes,
                            recommendationId = recommendationId,
                            photoUri = photoUri,
                            onSuccess = {
                                showAddServiceSheet = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Запись ТО добавлена")
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

            if (selectedRecommendation != null) {
                RecommendationDetailBottomSheet(
                    recommendation = selectedRecommendation!!,
                    onDismiss = { selectedRecommendation = null },
                    onEdit = {
                        editingRecommendation = selectedRecommendation
                        selectedRecommendation = null
                    }
                )
            }

            if (editingRecommendation != null) {
                EditRecommendationBottomSheet(
                    recommendation = editingRecommendation!!,
                    onDismiss = { editingRecommendation = null },
                    onSave = { mileage, serviceType, description ->
                        viewModel.updateRecommendation(
                            recommendation = editingRecommendation!!,
                            recommendedMileage = mileage,
                            serviceType = serviceType,
                            description = description,
                            onSuccess = {
                                editingRecommendation = null
                                scope.launch {
                                    snackbarHostState.showSnackbar("Рекомендация обновлена")
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
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlannedRecommendationCard(
    recommendation: RecommendationResponse,
    currentMileage: Int?,
    onClick: () -> Unit
) {
    val isOverdue = currentMileage != null &&
            recommendation.nextRecommendedMileage != null &&
            currentMileage >= recommendation.nextRecommendedMileage

    val cardColor = if (isOverdue) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val iconTint = if (isOverdue) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    val borderColor = if (isOverdue) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = if (isOverdue) BorderStroke(1.dp, borderColor) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = recommendation.serviceType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (recommendation.nextRecommendedMileage == null) {
                Text(
                    text = "Рекомендуемый пробег: не указан",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Рекомендуемый пробег: ${recommendation.nextRecommendedMileage} км",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            if (recommendation.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isOverdue) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Рекомендуемый пробег превышен!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}



@Composable
fun CompletedServiceCard(
    service: ServiceRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = service.serviceType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${service.cost} ₽",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Пробег: ${service.mileage} км",
                style = MaterialTheme.typography.bodyMedium
            )

        }
    }
}

@Composable
fun SwipeToDeleteServiceCard(
    service: ServiceRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

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
                        if (abs(offsetX) > deleteThresholdPx) {
                            showDeleteDialog = true
                        }
                        scope.launch {
                            delay(100)
                            offsetX = 0f
                        }
                    },
                    onDragCancel = { offsetX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-maxSwipePx, 0f)
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
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Удалить",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.padding(end = 24.dp).size(32.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = offsetX.toInt(), y = 0) }
        ) {
            CompletedServiceCard(
                service = service,
                onClick = onClick
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление записи ТО") },
            text = {
                Text("Вы уверены, что хотите удалить запись ТО: ${service.serviceType}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeleteColor
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailBottomSheet(
    service: ServiceRecord,
    onDismiss: () -> Unit,
    onViewReceipt: (String) -> Unit,
    onEdit: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Детали обслуживания",
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            DetailRow("Тип", service.serviceType)
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow("Дата", DateFormat.formatDateToDisplay(service.date))
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow("Пробег", "${service.mileage} км")
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow("Стоимость", "${service.cost} ₽")

            if (!service.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow("Примечания", service.notes)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!service.receiptPhoto.isNullOrBlank()) {
                Button(
                    onClick = { onViewReceipt(service.receiptPhoto!!) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Посмотреть чек")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationDetailBottomSheet(
    recommendation: RecommendationResponse,
    onDismiss: () -> Unit,
    onEdit: (RecommendationResponse) -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Детали рекомендации",
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = { onEdit(recommendation) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DetailRow("Тип", recommendation.serviceType)

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow("Интервал", "${recommendation.recommendedMileage} км")

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow("Рекомендуемый пробег", "${recommendation.nextRecommendedMileage} км")

            Spacer(modifier = Modifier.height(12.dp))

            if (recommendation.description.isNotBlank()) {
                DetailRow("Описание", recommendation.description)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceBottomSheet(
    service: ServiceRecord,
    onDismiss: () -> Unit,
    onSave: (date: String, mileage: Int, cost: Double, notes: String?) -> Unit
) {
    var dateInput by remember { mutableStateOf(service.date) }
    var mileageInput by remember { mutableStateOf(service.mileage.toString()) }
    var costInput by remember { mutableStateOf(service.cost.replace(" ₽", "")) }
    var notesInput by remember { mutableStateOf(service.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    var dateError by remember { mutableStateOf<String?>(null) }
    var mileageError by remember { mutableStateOf<String?>(null) }
    var costError by remember { mutableStateOf<String?>(null) }

    // Получаем текущую дату для DatePicker
    val currentDate = try {
        java.time.LocalDate.parse(dateInput)
    } catch (e: Exception) {
        java.time.LocalDate.now()
    }

    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    val hasChanges = dateInput != service.date ||
            mileageInput.toIntOrNull() != service.mileage ||
            costInput.toDoubleOrNull() != service.cost.toDoubleOrNull() ||
            notesInput != (service.notes ?: "")

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
                text = "Редактирование ТО",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = DateFormat.formatDateToDisplay(dateInput),
                onValueChange = {},
                readOnly = true,
                label = { Text("Дата") },
                placeholder = { Text("Выберите дату") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                isError = dateError != null,
                supportingText = {
                    if (dateError != null) {
                        Text(dateError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Выбрать дату",
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Пробег
            OutlinedTextField(
                value = mileageInput,
                onValueChange = {
                    mileageInput = it.filter { c -> c.isDigit() }
                    mileageError = null
                },
                label = { Text("Пробег (км)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = mileageError != null,
                supportingText = {
                    if (mileageError != null) {
                        Text(mileageError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Стоимость
            OutlinedTextField(
                value = costInput,
                onValueChange = {
                    costInput = it.filter { c -> c.isDigit() || c == '.' }
                    costError = null
                },
                label = { Text("Стоимость (₽)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = costError != null,
                supportingText = {
                    if (costError != null) {
                        Text(costError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Примечания (необязательно)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        var hasError = false

                        // Проверка даты
                        if (dateInput.isBlank()) {
                            dateError = "Выберите дату"
                            hasError = true
                        }

                        // Проверка пробега
                        val mileage = mileageInput.toIntOrNull()
                        if (mileage == null || mileage <= 0) {
                            mileageError = "Введите корректный пробег"
                            hasError = true
                        }

                        // Проверка стоимости
                        val cost = costInput.toDoubleOrNull()
                        if (cost == null || cost <= 0) {
                            costError = "Введите корректную стоимость"
                            hasError = true
                        }

                        if (!hasError) {
                            onSave(dateInput, mileage!!, cost!!, notesInput.ifBlank { null })
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = hasChanges
                ) {
                    Text("Сохранить")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // DatePicker диалог
    if (showDatePicker) {
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            dateInput = selectedDate.toString()
                            dateError = null
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecommendationBottomSheet(
    recommendation: RecommendationResponse,
    onDismiss: () -> Unit,
    onSave: (recommendedMileage: Int?, serviceType: String?, description: String?) -> Unit
) {
    var mileageInput by remember { mutableStateOf(recommendation.recommendedMileage.toString()) }
    var serviceTypeInput by remember { mutableStateOf(recommendation.serviceType) }
    var descriptionInput by remember { mutableStateOf(recommendation.description) }

    var mileageError by remember { mutableStateOf<String?>(null) }

    // Проверяем, есть ли изменения
    val hasChanges = mileageInput.toIntOrNull() != recommendation.recommendedMileage ||
            serviceTypeInput != recommendation.serviceType ||
            descriptionInput != recommendation.description

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
                text = "Редактирование рекомендации",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Тип обслуживания
            OutlinedTextField(
                value = serviceTypeInput,
                onValueChange = { serviceTypeInput = it },
                label = { Text("Тип обслуживания") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Рекомендуемый пробег
            OutlinedTextField(
                value = mileageInput,
                onValueChange = {
                    mileageInput = it.filter { c -> c.isDigit() }
                    mileageError = null
                },
                label = { Text("Рекомендуемый пробег (км)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = mileageError != null,
                supportingText = {
                    if (mileageError != null) {
                        Text(mileageError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Описание
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                label = { Text("Описание") },
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        var hasError = false

                        val mileage = if (mileageInput.isNotBlank()) {
                            val parsedMileage = mileageInput.toIntOrNull()
                            if (parsedMileage == null || parsedMileage <= 0) {
                                mileageError = "Введите корректный пробег"
                                hasError = true
                                null
                            } else {
                                parsedMileage
                            }
                        } else {
                            null
                        }

                        val serviceType = serviceTypeInput.ifBlank { null }
                        val description = descriptionInput.ifBlank { null }

                        if (!hasError) {
                            val finalMileage = if (mileage != recommendation.recommendedMileage) mileage else null
                            val finalServiceType = if (serviceType != recommendation.serviceType) serviceType else null
                            val finalDescription = if (description != recommendation.description) description else null

                            onSave(finalMileage, finalServiceType, finalDescription)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = hasChanges
                ) {
                    Text("Сохранить")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceBottomSheet(
    recommendations: List<RecommendationResponse>,
    currentMileage: Int?,
    onDismiss: () -> Unit,
    onAdd: (
        serviceType: String,
        date: String,
        mileage: Int,
        cost: Double,
        notes: String?,
        recommendationId: Int?,
        photoUri: Uri?
    ) -> Unit
) {
    val context = LocalContext.current
    var serviceTypeInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf(LocalDate.now().toString()) }
    var mileageInput by remember { mutableStateOf("") }
    var costInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var selectedRecommendationId by remember { mutableStateOf<Int?>(null) }
    var selectedRecommendationType by remember { mutableStateOf<String?>(null) }
    var mileageError by remember { mutableStateOf<String?>(null) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var recommendationExpanded by remember { mutableStateOf(false) }

    var dateError by remember { mutableStateOf<String?>(null) }

    val cameraPermission = android.Manifest.permission.CAMERA

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoFile?.let { file ->
                selectedPhotoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            }
        }
        showPhotoSourceSheet = false
    }

    val cameraPermissionState = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCameraFunction(context, tempPhotoFile, cameraLauncher)
        } else {
            Toast.makeText(context, "Для фото чека нужно разрешение на камеру", Toast.LENGTH_SHORT).show()
            showPhotoSourceSheet = false
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedPhotoUri = uri
        showPhotoSourceSheet = false
    }

    val currentDate = try {
        java.time.LocalDate.parse(dateInput)
    } catch (e: Exception) {
        java.time.LocalDate.now()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

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
                text = "Добавление ТО",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Тип обслуживания
            OutlinedTextField(
                value = serviceTypeInput,
                onValueChange = { serviceTypeInput = it },
                label = { Text("Тип обслуживания *") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Дата
            OutlinedTextField(
                value = DateFormat.formatDateToDisplay(dateInput),
                onValueChange = {},
                readOnly = true,
                label = { Text("Дата *") },
                placeholder = { Text("Выберите дату") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                isError = dateError != null,
                supportingText = {
                    if (dateError != null) {
                        Text(dateError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Выбрать дату",
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val mileageValue = mileageInput.toIntOrNull()
            val isMileageError = if (currentMileage != null && mileageValue != null) {
                mileageValue > currentMileage
            } else {
                false
            }
            val mileageErrorMessage = when {
                currentMileage == null -> "Пробег не может быть пустым"
                mileageValue == null || mileageValue <= 0 -> "Введите корректный пробег"
                mileageValue > currentMileage -> "Пробег не может превышать текущий ($currentMileage км)"
                else -> null
            }

            OutlinedTextField(
                value = mileageInput,
                onValueChange = {
                    mileageInput = it.filter { c -> c.isDigit() }
                    mileageError = null
                },
                label = { Text("Пробег (км) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = isMileageError || mileageError != null,
                supportingText = {
                    when {
                        mileageError != null -> Text(mileageError!!, color = MaterialTheme.colorScheme.error)
                        isMileageError && currentMileage != null -> Text(
                            "Пробег не может превышать $currentMileage км",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Стоимость
            OutlinedTextField(
                value = costInput,
                onValueChange = { costInput = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Стоимость (₽) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (recommendations.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = recommendationExpanded,
                    onExpandedChange = { recommendationExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRecommendationType ?: "Выберите рекомендацию (необязательно)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Связать с рекомендацией") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (recommendationExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = recommendationExpanded,
                        onDismissRequest = { recommendationExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Без рекомендации") },
                            onClick = {
                                selectedRecommendationId = null
                                selectedRecommendationType = null
                                recommendationExpanded = false
                            }
                        )
                        recommendations.forEach { recommendation ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = recommendation.serviceType,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Пробег: ${recommendation.nextRecommendedMileage ?: recommendation.recommendedMileage} км",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedRecommendationId = recommendation.recommendationId
                                    selectedRecommendationType = recommendation.serviceType
                                    recommendationExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Примечания
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Примечания (необязательно)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка прикрепления чека
            Button(
                onClick = { showPhotoSourceSheet = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedPhotoUri == null) "Прикрепить чек" else "Чек прикреплён ✓")
            }

            if (selectedPhotoUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Фото выбрано",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        val mileage = mileageInput.toIntOrNull()
                        val cost = costInput.toDoubleOrNull()

                        if (serviceTypeInput.isBlank()) {
                            Toast.makeText(context, "Введите тип обслуживания", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (mileage == null || mileage <= 0) {
                            Toast.makeText(context, "Введите корректный пробег", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (cost == null || cost <= 0) {
                            Toast.makeText(context, "Введите корректную стоимость", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        onAdd(serviceTypeInput, dateInput, mileage,
                            cost, notesInput.ifBlank { null },
                            selectedRecommendationId, selectedPhotoUri
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Добавить")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            dateInput = selectedDate.toString()
                            dateError = null
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker( state = datePickerState, showModeToggle = false
            )
        }
    }

    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false },
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
                        .clickable {
                            galleryLauncher.launch("image/*")
                        },
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
                        .clickable {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    cameraPermission
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                val timeStamp = System.currentTimeMillis()
                                val photoFile = File(context.cacheDir, "service_$timeStamp.jpg")
                                tempPhotoFile = photoFile
                                openCameraFunction(context, photoFile, cameraLauncher)
                            } else {
                                cameraPermissionState.launch(cameraPermission)
                            }
                            showPhotoSourceSheet = false
                        },
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

                TextButton(onClick = { showPhotoSourceSheet = false }) {
                    Text("Отмена")
                }
            }
        }
    }
}

private fun openCameraFunction(
    context: Context,
    photoFile: File?,
    cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>
) {
    photoFile?.let { file ->
        val photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        cameraLauncher.launch(photoUri)
    }
}

@Composable
fun ReceiptWebViewDialogForMaintenance(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var localUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(imageUrl) {
        if (imageUrl.startsWith("file://")) {
            try {
                val filePath = imageUrl.substringAfter("file://")
                val file = File(filePath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    localUri = uri
                }
            } catch (e: Exception) {
                localUri = Uri.parse(imageUrl)
            }
        } else {
            localUri = Uri.parse(imageUrl)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        },
        text = {
            localUri?.let { uri ->
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            settings.allowFileAccessFromFileURLs = true
                            settings.allowUniversalAccessFromFileURLs = true

                            webViewClient = WebViewClient()

                            loadUrl(uri.toString())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Не удалось загрузить изображение")
                }
            }
        }
    )
}

