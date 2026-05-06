package com.example.autolog_20.ui.theme.data.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autolog_20.ui.theme.DeleteColor
import com.example.autolog_20.ui.theme.TileMileage
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.MileageLog
import com.example.autolog_20.ui.theme.data.model.viewmodel.MileageViewModel
import com.example.autolog_20.ui.theme.data.model.DateFormat
import com.example.autolog_20.ui.theme.data.model.MileageUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MileageScreen(
    navController: NavController,
    numberPlate: String
) {
    val context = LocalContext.current
    val viewModel: MileageViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MileageViewModel(RetrofitClient.api, numberPlate) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPeriodSheet by remember { mutableStateOf(false) }
    var showAddMileageSheet by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<MileageLog?>(null) }


    fun getPeriodTitle(uiState: MileageUiState): String {
        return when (uiState.selectedPeriod) {
            "all" -> "За всё время"
            "year" -> "Последний год"
            "month" -> "Последний месяц"
            "week" -> "Последняя неделя"
            "custom" ->  "Свой период"
            else -> {
                "За всё время"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История пробега") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddMileageSheet = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить запись пробега")
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
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadMileage() }) {
                            Text("Повторить")
                        }
                    }
                }

                else -> {
                    if (uiState.logs.size >= 2) {
                        MileageChart(logs = uiState.logs)
                        Spacer(modifier = Modifier.height(16.dp))
                    } else if (uiState.logs.size == 1) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Недостаточно данных",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Добавьте минимум 2 записи пробега для отображения графика",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MileageInfoCard(
                            logs = uiState.logs,
                            modifier = Modifier.weight(1f)
                        )

                        FilterTile(
                            title = "Период",
                            subtitle = getPeriodTitle(uiState),
                            modifier = Modifier.weight(1f),
                            onClick = { showPeriodSheet = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    // Список записей пробега
                    if (uiState.logs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Нет записей о пробеге",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        MileageLogsList(
                            logs = uiState.logs,
                            onEdit = { log -> editingLog = log },
                            onDelete = { log ->
                                viewModel.deleteMileageRecord(
                                    log = log,
                                    onSuccess = {
                                        Toast.makeText(context, "Запись удалена", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet для выбора периода
    if (showPeriodSheet) {
        PeriodSelectionBottomSheet(
            currentPeriod = uiState.selectedPeriod,
            onPeriodSelected = { period ->
                viewModel.changePeriod(period)
                showPeriodSheet = false
            },
            onCustomPeriodSelected = { from, to ->
                viewModel.changeCustomPeriod(from, to)
                showPeriodSheet = false
            },
            onDismiss = { showPeriodSheet = false }
        )
    }

    if (showAddMileageSheet) {
        AddMileageBottomSheet(
            currentMileage = uiState.currentMileage,
            onDismiss = { showAddMileageSheet = false },
            onAdd = { date, mileage, route ->
                viewModel.addMileageRecord(
                    date = date,
                    mileage = mileage,
                    route = route,
                    onSuccess = {
                        showAddMileageSheet = false
                        Toast.makeText(context, "Запись пробега добавлена", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    if (editingLog != null) {
        EditMileageBottomSheet(
            log = editingLog!!,
            currentMileage = uiState.currentMileage,
            onDismiss = { editingLog = null },
            onSave = { date, mileage, route ->
                viewModel.updateMileageRecord(
                    log = editingLog!!,
                    date = date,
                    mileage = mileage,
                    route = route,
                    onSuccess = {
                        editingLog = null
                        Toast.makeText(context, "Запись обновлена", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

@Composable
fun MileageInfoCard(
    logs: List<MileageLog>,
    modifier: Modifier = Modifier
) {
    val sortedLogs = logs.sortedWith(compareBy<MileageLog> { it.date }.thenBy { it.logId })
    val currentMileage = sortedLogs.lastOrNull()?.mileage
    val firstMileage = sortedLogs.firstOrNull()?.mileage
    val totalDistance = if (currentMileage != null && firstMileage != null) {
        currentMileage - firstMileage
    } else null

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Пробег",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (currentMileage != null) "${currentMileage} км" else "Нет данных",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MileageChart(logs: List<MileageLog>) {
    val sortedLogs = logs.sortedWith(compareBy<MileageLog> { it.date }.thenBy { it.logId })

    val realMin = sortedLogs.minOfOrNull { it.mileage } ?: 0
    val realMax = sortedLogs.maxOfOrNull { it.mileage } ?: 1

    fun roundUpToHundred(value: Int): Int {
        return ((value + 99) / 100) * 100
    }
    fun roundDownToHundred(value: Int): Int {
        return (value / 100) * 100
    }

    val minMileage = if (realMin > 0) roundDownToHundred(realMin) else 0
    val maxMileage = roundUpToHundred(realMax)
    val range = max(maxMileage - minMileage, 1)

    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "График изменения пробега",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // График
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val paddingLeft = 10f
                    val paddingRight = 10f
                    val paddingBottom = 40f
                    val paddingTop = 45f

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    val gridColor = Color.Gray.copy(alpha = 0.3f)

                    // Горизонтальные линии сетки (4 линии)
                    for (i in 0..4) {
                        val y = paddingTop + (chartHeight / 4) * i
                        drawLine(
                            color = gridColor,
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1f
                        )
                    }

                    // Вертикальные линии сетки
                    if (sortedLogs.size >= 2) {
                        val xStep = chartWidth / (sortedLogs.size - 1)
                        for (i in 0 until sortedLogs.size) {
                            val x = paddingLeft + i * xStep
                            drawLine(
                                color = gridColor,
                                start = Offset(x, paddingTop),
                                end = Offset(x, height - paddingBottom),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // Линия графика
                    if (sortedLogs.size >= 2) {
                        val path = Path()
                        val xStep = chartWidth / (sortedLogs.size - 1)

                        sortedLogs.forEachIndexed { index, log ->
                            val x = paddingLeft + index * xStep
                            val normalizedMileage = (log.mileage - minMileage).toFloat() / range
                            val y = paddingTop + chartHeight * (1 - normalizedMileage)
                            val clampedY = y.coerceIn(paddingTop, height - paddingBottom)

                            if (index == 0) {
                                path.moveTo(x, clampedY)
                            } else {
                                path.lineTo(x, clampedY)
                            }
                        }

                        drawPath(
                            path = path,
                            color = TileMileage,
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )

                        // Точки на графике с подписями
                        sortedLogs.forEachIndexed { index, log ->
                            val x = paddingLeft + index * xStep
                            val normalizedMileage = (log.mileage - minMileage).toFloat() / range
                            val y = paddingTop + chartHeight * (1 - normalizedMileage)
                            val clampedY = y.coerceIn(paddingTop, height - paddingBottom)

                            // Рисуем точку
                            drawCircle(
                                color = primaryColor,
                                radius = 7f,
                                center = Offset(x, clampedY)
                            )

                            // Рисуем подпись значения пробега НАД точкой
                            val mileagePaint = android.graphics.Paint().apply {
                                color = primaryColor.toArgb()
                                textSize = 32f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }

                            val mileageText = "${log.mileage}"
                            drawContext.canvas.nativeCanvas.drawText(
                                mileageText,
                                x,
                                clampedY - 22f,
                                mileagePaint
                            )

                            val datePaint = android.graphics.Paint().apply {
                                color = Color.Gray.toArgb()
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }

                            val dateText = formatDateShort(log.date)
                            drawContext.canvas.nativeCanvas.drawText(
                                dateText,
                                x,
                                clampedY + 28f,
                                datePaint
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MileageLogsList(
    logs: List<MileageLog>,
    onEdit: (MileageLog) -> Unit,
    onDelete: (MileageLog) -> Unit
) {
    val sortedByDateAscending = logs.sortedBy { it.date }

    val previousMileageMap = mutableMapOf<Int, Int?>()
    for (i in sortedByDateAscending.indices) {
        val currentLog = sortedByDateAscending[i]
        val previousLog = if (i > 0) sortedByDateAscending[i - 1] else null
        previousMileageMap[currentLog.logId] = previousLog?.mileage
    }

    val sortedLogs = logs.sortedByDescending { it.date }

    val groupedLogs = sortedLogs
        .groupBy { it.date }
        .toSortedMap(reverseOrder())

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        groupedLogs.forEach { (date, logsOnDate) ->
            val sortedLogsOnDate = logsOnDate.sortedByDescending { it.logId }

            item(key = "header_$date") {
                Text(
                    text = DateFormat.formatDateHeader(date),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }

            items(items = sortedLogsOnDate, key = { it.logId }) { log ->
                SwipeableMileageCard(
                    log = log,
                    previousMileage = previousMileageMap[log.logId],
                    onEdit = { onEdit(log) },
                    onDelete = { onDelete(log) }
                )
            }

            item(key = "spacer_$date") {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun MileageLogCard(
    log: MileageLog,
    previousMileage: Int?,
    onClick: () -> Unit
) {
    val difference = if (previousMileage != null) {
        log.mileage - previousMileage
    } else {
        log.mileage
    }

    val differenceText = if (previousMileage != null) {
        "+${difference} км"
    } else {
        "Начальный пробег: ${difference} км"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${log.mileage} км",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = log.route,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = differenceText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = DateFormat.formatDateToDisplay(log.date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDateShort(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("dd.MM")
        date.format(formatter)
    } catch (e: Exception) {
        dateStr
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMileageBottomSheet(
    currentMileage: Int?,
    onDismiss: () -> Unit,
    onAdd: (date: String, mileage: Int, route: String) -> Unit
) {
    val context = LocalContext.current
    var dateInput by remember { mutableStateOf(LocalDate.now().toString()) }
    var mileageInput by remember { mutableStateOf("") }
    var routeInput by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var mileageError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    val currentDate = try {
        LocalDate.parse(dateInput)
    } catch (e: Exception) {
        LocalDate.now()
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
                text = "Добавление записи пробега",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

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

            OutlinedTextField(
                value = mileageInput,
                onValueChange = {
                    mileageInput = it.filter { c -> c.isDigit() }
                    mileageError = null
                },
                label = { Text("Пробег (км) *") },
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

            OutlinedTextField(
                value = routeInput,
                onValueChange = { routeInput = it },
                label = { Text("Маршрут / описание") },
                placeholder = { Text("Например: Поездка в Москву") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

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
                        var hasError = false

                        if (dateInput.isBlank()) {
                            dateError = "Выберите дату"
                            hasError = true
                        }

                        val mileage = mileageInput.toIntOrNull()
                        if (mileage == null || mileage <= 0) {
                            mileageError = "Введите корректный пробег"
                            hasError = true
                        } else if (currentMileage != null && mileage < currentMileage) {
                            mileageError = "Пробег не может быть меньше текущего ($currentMileage км)"
                            hasError = true
                        }

                        if (!hasError) {
                            onAdd( dateInput, mileage!!, routeInput.ifBlank { "Запись пробега" }
                            )
                        }
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
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMileageBottomSheet(
    log: MileageLog,
    currentMileage: Int?,
    onDismiss: () -> Unit,
    onSave: (date: String, mileage: Int, route: String) -> Unit
) {
    var dateInput by remember { mutableStateOf(log.date) }
    var mileageInput by remember { mutableStateOf(log.mileage.toString()) }
    var routeInput by remember { mutableStateOf(log.route) }
    var showDatePicker by remember { mutableStateOf(false) }
    var mileageError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

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
                text = "Редактирование записи",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

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

            // Пробег
            OutlinedTextField(
                value = mileageInput,
                onValueChange = {
                    mileageInput = it.filter { c -> c.isDigit() }
                    mileageError = null
                },
                label = { Text("Пробег (км) *") },
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

            // Маршрут
            OutlinedTextField(
                value = routeInput,
                onValueChange = { routeInput = it },
                label = { Text("Маршрут / описание") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

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
                        var hasError = false

                        if (dateInput.isBlank()) {
                            dateError = "Выберите дату"
                            hasError = true
                        }

                        val mileage = mileageInput.toIntOrNull()
                        if (mileage == null || mileage <= 0) {
                            mileageError = "Введите корректный пробег"
                            hasError = true
                        } else if (currentMileage != null && mileage < currentMileage && mileage != log.mileage) {
                            mileageError = "Пробег не может быть меньше текущего ($currentMileage км)"
                            hasError = true
                        }

                        if (!hasError) {
                            onSave(dateInput, mileage!!, routeInput)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
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
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }
}

// PeriodSelectionBottomSheet.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelectionBottomSheet(
    currentPeriod: String,
    onPeriodSelected: (String) -> Unit,
    onCustomPeriodSelected: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var fromDate by remember { mutableStateOf<String?>(null) }
    var toDate by remember { mutableStateOf<String?>(null) }

    val periods = listOf(
        "all" to "За всё время",
        "year" to "Последний год",
        "month" to "Последний месяц",
        "week" to "Последняя неделя"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Выберите период",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            periods.forEach { (value, title) ->
                val isSelected = currentPeriod == value

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onPeriodSelected(value) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected)
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Выбрано",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка для выбора своего периода
            OutlinedButton(
                onClick = { showCustomDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выбрать свой период")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    }

    // DatePicker для выбора своего периода
    if (showCustomDatePicker) {
        CustomDateRangePicker(
            onConfirm = { from, to ->
                onCustomPeriodSelected(from, to)
                showCustomDatePicker = false
                onDismiss()
            },
            onDismiss = { showCustomDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePicker(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()
    var fromDate by remember { mutableStateOf<String?>(null) }
    var toDate by remember { mutableStateOf<String?>(null) }

    // Обновляем отображаемые даты при изменении выбора
    LaunchedEffect(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis) {
        fromDate = dateRangePickerState.selectedStartDateMillis?.let {
            java.time.Instant.ofEpochMilli(it)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString()
        }
        toDate = dateRangePickerState.selectedEndDateMillis?.let {
            java.time.Instant.ofEpochMilli(it)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                title = {
                    Text("Выберите период")
                },
                headline = {
                    Text(
                        text = when {
                            fromDate != null && toDate != null -> {
                                val from = DateFormat.formatDateToDisplay(fromDate!!)
                                val to = DateFormat.formatDateToDisplay(toDate!!)
                                "$from — $to"
                            }
                            fromDate != null -> {
                                val from = DateFormat.formatDateToDisplay(fromDate!!)
                                "$from — ..."
                            }
                            else -> "Начало — Конец"
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        val fromMillis = dateRangePickerState.selectedStartDateMillis
                        val toMillis = dateRangePickerState.selectedEndDateMillis

                        if (fromMillis != null && toMillis != null) {
                            val from = java.time.Instant.ofEpochMilli(fromMillis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                            val to = java.time.Instant.ofEpochMilli(toMillis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                            onConfirm(from, to)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = dateRangePickerState.selectedStartDateMillis != null &&
                            dateRangePickerState.selectedEndDateMillis != null
                ) {
                    Text("Применить")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun SwipeableMileageCard(
    log: MileageLog,
    previousMileage: Int?,
    onEdit: () -> Unit,
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

    val maxSwipeRightPx = with(density) { 80.dp.toPx() }
    val maxSwipeLeftPx = with(density) { 80.dp.toPx() }
    val editThresholdPx = with(density) { 40.dp.toPx() }
    val deleteThresholdPx = with(density) { 40.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            offsetX > editThresholdPx -> {
                                onEdit()
                            }
                            offsetX < -deleteThresholdPx -> {
                                showDeleteDialog = true
                            }
                        }
                        scope.launch {
                            delay(100)
                            offsetX = 0f
                        }
                    },
                    onDragCancel = { offsetX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount)
                            .coerceIn(-maxSwipeLeftPx, maxSwipeRightPx)
                    }
                )
            }
    ) {

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        offsetX < 0 -> DeleteColor
                        offsetX > 0 -> MaterialTheme.colorScheme.primary
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = when {
                offsetX < 0 -> Alignment.CenterEnd
                offsetX > 0 -> Alignment.CenterStart
                else -> Alignment.Center
            }
        ) {
            when {
                offsetX < -10f -> {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier
                            .padding(end = 24.dp)
                            .size(32.dp)
                    )
                }

                offsetX > 10f -> {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(start = 24.dp)
                            .size(32.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = offsetX.toInt(), y = 0) }
        ) {
            MileageLogCard(
                log = log,
                previousMileage = previousMileage,
                onClick = {}
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление записи") },
            text = {
                Text(
                    "Вы уверены, что хотите удалить запись о пробеге ${log.mileage} км от ${
                        DateFormat.formatDateToDisplay(log.date)
                    }?"
                )
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