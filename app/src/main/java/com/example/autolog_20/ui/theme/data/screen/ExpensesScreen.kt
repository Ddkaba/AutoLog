package com.example.autolog_20.ui.theme.data.screen
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autolog_20.ui.theme.BackgroundDark
import com.example.autolog_20.ui.theme.PastelExpenseSubtitle
import com.example.autolog_20.ui.theme.SurfaceDark
import com.example.autolog_20.ui.theme.TileExpenses
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.ExpenseItem
import com.example.autolog_20.ui.theme.data.model.ExpensesUiState
import com.example.autolog_20.ui.theme.data.model.ExpensesViewModel
import java.time.LocalDate
import java.util.Locale
import com.example.autolog_20.ui.theme.PastelExpenseBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    navController: NavController,
    numberPlate: String,
    viewModel: ExpensesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ExpensesViewModel(RetrofitClient.api, numberPlate) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val totalAllTime by viewModel.totalAllTime.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()

    var showPeriodSheet by remember { mutableStateOf(false) }
    var showCategoriesBottomSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedExpense by remember { mutableStateOf<ExpenseItem?>(null) }
    val dateRangePickerState = rememberDateRangePickerState()


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Расходы") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Добавить расход")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ExpensesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ExpensesUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InteractiveExpensePieChart(
                            expenses = (uiState as? ExpensesUiState.Success)?.expenses ?: emptyList(),
                            modifier = Modifier.weight(1f),
                            onChartClick = {
                                showCategoriesBottomSheet = true
                            }
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TotalAllTimeCard(totalAllTime = totalAllTime)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth().padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterTile(
                            title = "Период",
                            subtitle = getPeriodLabel(selectedPeriod),
                            modifier = Modifier.weight(1f),
                            onClick = { showPeriodSheet = true }
                        )
                        FilterTile(
                            title = "Категории",
                            subtitle = when {
                                selectedCategories.isEmpty() -> "Все категории"
                                selectedCategories.size == 1 -> selectedCategories.first()
                                else -> "Несколько категорий"
                            },
                            modifier = Modifier.weight(1f),
                            onClick = { showCategorySheet = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.expenses.isEmpty()) {
                        EmptyExpensesContent(onAddClick = { showAddDialog = true })
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val groupedExpenses = state.expenses
                                .groupBy { it.date }
                                .toSortedMap(reverseOrder())

                            groupedExpenses.forEach { (date, expensesOnDate) ->
                                item(key = "header_$date") {
                                    Text(
                                        text = formatDateHeader(date),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp, vertical = 8.dp)
                                    )
                                }

                                items(
                                    items = expensesOnDate,
                                    key = { it.expense_id }
                                ) { expense ->
                                    ExpenseItemCard(
                                        expense = expense,
                                        onClick = { selectedExpense = it }
                                    )
                                }

                                item(key = "spacer_$date") {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }

            is ExpensesUiState.Error -> {
                ErrorContent(message = state.message, onRetry = viewModel::loadExpenses)
            }
        }
    }

    if (selectedExpense != null) {
        ExpenseDetailBottomSheet(
            expense = selectedExpense!!,
            onDismiss = { selectedExpense = null },
            onViewReceipt = { photoUrl ->
                // TODO: открыть изображение (например через Coil или browser)
                selectedExpense = null
            }
        )
    }

    if (showCategorySheet) {
        CategoryMultiBottomSheet(
            selectedCategories = selectedCategories,
            onCategoriesChanged = {
                viewModel.changeCategories(it)
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    if (showCategoriesBottomSheet) {
        ExpenseCategoriesBottomSheet(
            expenses = (uiState as? ExpensesUiState.Success)?.expenses ?: emptyList(),
            onDismiss = { showCategoriesBottomSheet = false }
        )
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { category, amount, date ->
                viewModel.addExpense(category, amount, date)
                showAddDialog = false
            }
        )
    }

    if (showPeriodSheet) {
        PeriodBottomSheet(
            selectedPeriod = selectedPeriod,
            onQuickPeriodSelected = { period ->
                viewModel.changePeriod(period)
                showPeriodSheet = false
            },
            onCustomPeriodClick = {
                showPeriodSheet = false
                showDatePicker = true
            },
            onDismiss = { showPeriodSheet = false }
        )
    }

    if (showDatePicker) {
        val fromMillis = dateRangePickerState.selectedStartDateMillis
        val toMillis = dateRangePickerState.selectedEndDateMillis

        val fromDate = fromMillis?.let {
            java.time.Instant.ofEpochMilli(it)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        }

        val toDate = toMillis?.let {
            java.time.Instant.ofEpochMilli(it)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        }

        ModalBottomSheet(
            onDismissRequest = { showDatePicker = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {

                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    title = {
                        Text("Выберите период")
                    },
                    headline = {
                        Text(
                            text = when {
                                fromDate != null && toDate != null -> "$fromDate — $toDate"
                                fromDate != null -> "$fromDate — ..."
                                else -> "Начало — Конец"
                            }
                        )
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Отмена")
                    }

                    Button(
                        enabled = fromDate != null && toDate != null,
                        onClick = {
                            viewModel.setCustomPeriod(fromDate!!, toDate!!)
                            showDatePicker = false
                        }
                    ) {
                        Text("Применить")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { category, amount, date ->
                viewModel.addExpense(category, amount, date)
            }
        )
    }
}


private fun getPeriodLabel(period: String): String {
    return when (period) {
        "week" -> "Неделя"
        "month" -> "Месяц"
        "year" -> "Год"
        "custom" -> "Свой период"
        else -> "За всё время"
    }
}
private fun Double.format(digits: Int) = "%.${digits}f".format(this)

private fun formatDateHeader(dateStr: String): String {
    val date = try {
        LocalDate.parse(dateStr)
    } catch (e: Exception) {
        return dateStr
    }

    return when {
        date == LocalDate.now() -> "Сегодня"
        date == LocalDate.now().minusDays(1) -> "Вчера"
        else -> {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy",
                Locale("ru")
            )
            date.format(formatter)
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: ExpenseItem,
    onClick: (ExpenseItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(expense) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    text = expense.category?.name ?: "Прочие расходы",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = expense.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${expense.amount} ₽",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PastelExpenseBackground
            )
        }
    }
}

@Composable
fun EmptyExpensesContent(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Пока нет расходов",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Здесь будут отображаться все ваши траты на автомобиль.\nДобавьте первый расход с помощью кнопки «+»",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onAddClick,
            modifier = Modifier.height(52.dp)
        ) {
            Text("Добавить первый расход")
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Не удалось загрузить расходы",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.height(52.dp)
        ) {
            Text("Повторить попытку")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { /* можно добавить переход назад */ }) {
            Text("Вернуться назад")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (category: String, amount: Double, date: String) -> Unit
) {
    var category by remember { mutableStateOf("Топливо") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый расход") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Категория
                Text("Категория", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = false, // можно сделать полноценный dropdown позже
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Выберите категорию") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Сумма
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Сумма (₽)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Дата
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Дата (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Описание (опционально)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (необязательно)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onAdd(category, amt, date)
                    }
                }
            ) {
                Text("Добавить")
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
fun InteractiveExpensePieChart(
    expenses: List<ExpenseItem>,
    modifier: Modifier = Modifier,
    onChartClick: () -> Unit = {}
) {
    if (expenses.isEmpty()) {
        EmptyChartPlaceholder(modifier)
        return
    }

    val categorySums = expenses
        .groupBy { it.category?.name ?: "Прочие расходы" }
        .mapValues { it.value.sumOf { exp -> exp.amount.toDoubleOrNull() ?: 0.0 } }

    val total = categorySums.values.sum()
    if (total <= 0.0) {
        EmptyChartPlaceholder(modifier)
        return
    }

    val colors = listOf(
        Color(0xFFFF0000), Color(0xFFFF8C00), Color(0xFFFFFF00),
        Color(0xFF00FF00), Color(0xFF00FFFF), Color(0xFF0000CD)
    )

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures {
                    onChartClick()
                }
            }
    ) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f

        categorySums.entries.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total * 360f).toFloat()

            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(radius * 2f, radius * 2f),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            startAngle += sweepAngle
        }

        // Внутренний круг
        drawCircle(
            color = BackgroundDark,
            radius = radius * 0.62f,
            center = center
        )

        // Сумма в центре
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 48f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            drawText(
                "${total.format(0)} ₽",
                center.x,
                center.y + 18f,
                paint
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCategoriesBottomSheet(
    expenses: List<ExpenseItem>,
    onDismiss: () -> Unit
) {
    val categorySums = expenses
        .groupBy { it.category?.name ?: "Прочие расходы" }
        .mapValues { it.value.sumOf { exp -> exp.amount.toDoubleOrNull() ?: 0.0 } }

    val total = categorySums.values.sum()
    val colors = listOf(
        Color(0xFFFF0000), Color(0xFFFF8C00), Color(0xFFFFFF00),
        Color(0xFF00FF00), Color(0xFF00FFFF), Color(0xFF0000CD)
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Распределение расходов",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn {
                items(categorySums.entries.toList()) { (categoryName, amount) ->
                    val percentage = if (total > 0) (amount / total * 100).format(1) else "0.0"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Цветной индикатор
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = colors[categorySums.keys.indexOf(categoryName) % colors.size],
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${amount.format(2)} ₽",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (categorySums.keys.indexOf(categoryName) < categorySums.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Нет данных для диаграммы",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TotalAllTimeCard(totalAllTime: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth().aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = TileExpenses
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "За всё время",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = totalAllTime.format(0)+" ₽",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SurfaceDark
                )
            }
        }
    }
}

@Composable
fun FilterTile(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = PastelExpenseSubtitle),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodBottomSheet(
    selectedPeriod: String,
    onQuickPeriodSelected: (String) -> Unit,
    onCustomPeriodClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Выберите период", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(24.dp))

            val periods = listOf(
                "all" to "За всё время",
                "week" to "Последняя неделя",
                "month" to "Последний месяц",
                "year" to "Последний год"
            )

            periods.forEach { (value, label) ->
                ListItem(
                    headlineContent = { Text(label) },
                    leadingContent = {
                        RadioButton(
                            selected = selectedPeriod == value,
                            onClick = { onQuickPeriodSelected(value) }
                        )
                    },
                    modifier = Modifier.clickable {
                        onQuickPeriodSelected(value)
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onCustomPeriodClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Выбрать свой период")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CategoryMultiBottomSheet(
        selectedCategories: List<String>,
        onCategoriesChanged: (List<String>) -> Unit,
        onDismiss: () -> Unit
    ) {
        val allCategories = listOf(
            "Техническое обслуживание", "Ремонт", "Топливо", "Страхование",
            "Налоги и пошлины", "Мойка и уход", "Парковка и хранение",
            "Штрафы", "Запчасти и расходники", "Прочие расходы"
        )

        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Выберите категории", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(16.dp))

                LazyColumn {
                    items(allCategories) { category ->
                        val isSelected = selectedCategories.contains(category)
                        ListItem(
                            headlineContent = { Text(category) },
                            leadingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        val newList = if (isSelected) {
                                            selectedCategories - category
                                        } else {
                                            selectedCategories + category
                                        }
                                        onCategoriesChanged(newList)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailBottomSheet(
    expense: ExpenseItem,
    onDismiss: () -> Unit,
    onViewReceipt: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Подробности расхода",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            DetailRow("Категория", expense.category?.name ?: "Прочие расходы")

            DetailRow("Сумма", "${expense.amount} ₽")

            DetailRow("Дата", expense.date)

            if (expense.mileage != null) {
                DetailRow("Пробег", "${expense.mileage} км")
            }

            if (!expense.description.isNullOrBlank()) {
                DetailRow("Описание", expense.description)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!expense.receipt_photo.isNullOrBlank()) {
                Button(
                    onClick = { onViewReceipt(expense.receipt_photo!!) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Посмотреть чек")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}