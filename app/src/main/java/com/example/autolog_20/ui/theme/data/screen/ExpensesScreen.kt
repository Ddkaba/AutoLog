package com.example.autolog_20.ui.theme.data.screen

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
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
import com.example.autolog_20.ui.theme.BackgroundDark
import com.example.autolog_20.ui.theme.PastelExpenseSubtitle
import com.example.autolog_20.ui.theme.SurfaceDark
import com.example.autolog_20.ui.theme.TileExpenses
import com.example.autolog_20.ui.theme.DeleteColor
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.ExpenseItem
import com.example.autolog_20.ui.theme.data.model.viewmodel.ExpensesViewModel
import java.time.LocalDate
import java.util.Locale
import com.example.autolog_20.ui.theme.PastelExpenseBackground
import com.example.autolog_20.ui.theme.data.model.ExpensesUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.jar.Manifest
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    navController: NavController,
    numberPlate: String
) {
    val context = LocalContext.current

    val viewModel: ExpensesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ExpensesViewModel(
                    RetrofitClient.api,
                    numberPlate,
                    context
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val totalAllTime by viewModel.totalAllTime.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showPeriodSheet by remember { mutableStateOf(false) }
    var showCategoriesBottomSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedExpense by remember { mutableStateOf<ExpenseItem?>(null) }
    var receiptUrl by remember { mutableStateOf<String?>(null) }
    val dateRangePickerState = rememberDateRangePickerState()
    var selectedExpenseForEdit by remember { mutableStateOf<ExpenseItem?>(null) }

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
                        ExpensesList(
                            expenses = state.expenses,
                            onExpenseClick = { expense ->
                                selectedExpense = expense
                            },
                            onExpenseDelete = { expense ->
                                viewModel.deleteExpense(
                                    expense = expense,
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Расход успешно удален")
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
                receiptUrl = photoUrl
                selectedExpense = null
            },
            onEdit = { expense ->
                selectedExpenseForEdit = expense
            }
        )
    }

    if (selectedExpenseForEdit != null) {
        EditExpenseBottomSheet(
            expense = selectedExpenseForEdit!!,
            onDismiss = { selectedExpenseForEdit = null },
            onSave = { amount, date, description, categoryId ->
                viewModel.updateExpense(
                    expense = selectedExpenseForEdit!!,
                    amount = amount,
                    date = date,
                    description = description,
                    categoryId = categoryId,
                    onSuccess = {
                        selectedExpenseForEdit = null
                        scope.launch {
                            snackbarHostState.showSnackbar("Расход успешно обновлен")
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
            onAdd = { category, categoryId, amount, date, description, photoUri ->
                viewModel.addExpenseWithReceipt(
                    category = category,
                    categoryId = categoryId,
                    amount = amount,
                    date = date,
                    description = description,
                    photoUri = photoUri
                )
            }
        )
    }

    if (receiptUrl != null) {
        ReceiptWebViewDialog(
            imageUrl = receiptUrl!!,
            onDismiss = { receiptUrl = null }
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
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }

        val toDate = toMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
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
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy",
                Locale("ru")
            )
            date.format(formatter)
        }
    }
}

@Composable
fun ExpensesList(
    expenses: List<ExpenseItem>,
    onExpenseClick: (ExpenseItem) -> Unit,
    onExpenseDelete: (ExpenseItem) -> Unit,
    modifier: Modifier = Modifier
) {

    val groupedExpenses = expenses
        .groupBy { it.date }
        .toSortedMap(reverseOrder())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                key = { it.expenseId }
            ) { expense ->
                SwipeToDeleteExpenseItem(
                    expense = expense,
                    onClick = onExpenseClick,
                    onDelete = onExpenseDelete
                )
            }

            item(key = "spacer_$date") {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun SwipeToDeleteExpenseItem(
    expense: ExpenseItem,
    onClick: (ExpenseItem) -> Unit,
    onDelete: (ExpenseItem) -> Unit
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
            ExpenseItemCard(
                expense = expense,
                onClick = onClick
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление расхода") },
            text = {
                Text("Вы уверены, что хотите удалить расход на сумму ${expense.amount} ₽ от ${expense.date}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(expense)
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
    onAdd: (category: String, categoryId: Int, amount: Double, date: String, description: String?, photoUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Топливо") }
    var selectedCategoryId by remember { mutableStateOf(3) }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var description by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    var categoryMenuExpanded by remember { mutableStateOf(false) }

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

            openCamera(context, tempPhotoFile, cameraLauncher)
        } else {
            Toast.makeText(context, "Для фото чека нужно разрешение на камеру", Toast.LENGTH_SHORT).show()
            showPhotoSourceSheet = false
        }
    }

    // Ланчер для галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedPhotoUri = uri
        showPhotoSourceSheet = false
    }


    val categories = listOf(
        1 to "Техническое обслуживание",
        2 to "Ремонт",
        3 to "Топливо",
        4 to "Страхование",
        5 to "Налоги и пошлины",
        6 to "Мойка и уход",
        7 to "Парковка и хранение",
        8 to "Штрафы",
        9 to "Запчасти и расходники",
        10 to "Прочие расходы"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый расход") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                // Выпадающий список категорий
                Text(
                    text = "Категория",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Выберите категорию") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (categoryMenuExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        categories.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedCategory = name
                                    selectedCategoryId = id
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Сумма
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
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

                // Описание
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (необязательно)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

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
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Фото выбрано",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onAdd(
                            selectedCategory,
                            selectedCategoryId,
                            amt,
                            date,
                            description.ifBlank { null },
                            selectedPhotoUri
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Добавить расход")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )

    // Bottom Sheet для выбора источника фото
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

                // Кнопка галереи
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

                // Кнопка камеры
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
                                val photoFile = File(context.cacheDir, "receipt_$timeStamp.jpg")
                                tempPhotoFile = photoFile
                                openCamera(context, photoFile, cameraLauncher)
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

private fun openCamera(
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
        .mapValues { entry ->
            entry.value.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        }

    val total = categorySums.values.sum()
    if (total <= 0.0) {
        EmptyChartPlaceholder(modifier)
        return
    }

    val categoryColorMap = mapOf(
        "Техническое обслуживание" to Color(0xFFFF0000),
        "Ремонт"                   to Color(0xFFFF8C00),
        "Топливо"                  to Color(0xFFFFFF00),
        "Страхование"              to Color(0xFF00FF00),
        "Налоги и пошлины"         to Color(0xFF00FFFF),
        "Мойка и уход"             to Color(0xFF0000CD),
        "Парковка и хранение"      to Color(0xFF8A2BE2),
        "Штрафы"                   to Color(0xFFFF1493),
        "Запчасти и расходники"    to Color(0xFFF0B2AB),
        "Прочие расходы"           to Color(0xFFF05340)
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

        categorySums.entries.forEachIndexed { index, (categoryName, amount) ->
            val sweepAngle = (amount / total * 360f).toFloat()
            val color = categoryColorMap[categoryName] ?: Color.Gray

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(radius * 2f, radius * 2f),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            startAngle += sweepAngle
        }

        drawCircle(
            color = BackgroundDark,
            radius = radius * 0.62f,
            center = center
        )

        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 48f
                textAlign = Paint.Align.CENTER
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

    val categoryColorMap = mapOf(
        "Техническое обслуживание" to Color(0xFFFF0000),
        "Ремонт"                   to Color(0xFFFF8C00),
        "Топливо"                  to Color(0xFFFFFF00),
        "Страхование"              to Color(0xFF00FF00),
        "Налоги и пошлины"         to Color(0xFF00FFFF),
        "Мойка и уход"             to Color(0xFF0000CD),
        "Парковка и хранение"      to Color(0xFF8A2BE2),
        "Штрафы"                   to Color(0xFFFF1493),
        "Запчасти и расходники"    to Color(0xFFF0B2AB),
        "Прочие расходы"           to Color(0xFFF05340)
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
                    val color = categoryColorMap[categoryName] ?: Color.Gray

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(color, RoundedCornerShape(6.dp))
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
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
    onViewReceipt: (String) -> Unit,
    onEdit: (ExpenseItem) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Подробности расхода",
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(
                    onClick = {
                        onDismiss()
                        onEdit(expense)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

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

            if (!expense.receiptPhoto.isNullOrBlank()) {
                Button(
                    onClick = { onViewReceipt(expense.receiptPhoto!!) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Посмотреть чек")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseBottomSheet(
    expense: ExpenseItem,
    onDismiss: () -> Unit,
    onSave: (amount: Double?, date: String?, description: String?, categoryId: Int?) -> Unit
) {
    var amountInput by remember { mutableStateOf(expense.amount.replace(" ₽", "")) }
    var dateInput by remember { mutableStateOf(expense.date) }
    var descriptionInput by remember { mutableStateOf(expense.description ?: "") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    val hasChanges = amountInput.toDoubleOrNull() != expense.amount.toDoubleOrNull() ||
            dateInput != expense.date ||
            descriptionInput != (expense.description ?: "")

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
                text = "Редактирование расхода",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = {
                    amountInput = it.filter { c -> c.isDigit() || c == '.' }
                    amountError = null
                },
                label = { Text("Сумма (₽)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = amountError != null,
                supportingText = {
                    if (amountError != null) {
                        Text(amountError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = dateInput,
                onValueChange = {
                    dateInput = it
                    dateError = null
                },
                label = { Text("Дата (YYYY-MM-DD)") },
                placeholder = { Text("2024-01-01") },
                modifier = Modifier.fillMaxWidth(),
                isError = dateError != null,
                supportingText = {
                    if (dateError != null) {
                        Text(dateError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                label = { Text("Описание (необязательно)") },
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

                        val amount = if (amountInput.isNotBlank()) {
                            val parsedAmount = amountInput.toDoubleOrNull()
                            if (parsedAmount == null || parsedAmount <= 0) {
                                amountError = "Введите корректную сумму"
                                hasError = true
                                null
                            } else {
                                parsedAmount
                            }
                        } else {
                            null
                        }

                        val date = if (dateInput.isNotBlank()) {
                            if (!dateInput.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                                dateError = "Неверный формат даты (ГГГГ-ММ-ДД)"
                                hasError = true
                                null
                            } else {
                                dateInput
                            }
                        } else {
                            null
                        }

                        val description = descriptionInput.ifBlank { null }

                        if (!hasError) {
                            val finalAmount = if (amount != expense.amount.toDoubleOrNull()) amount else null
                            val finalDate = if (date != expense.date) date else null
                            val finalDescription = if (description != expense.description) description else null

                            onSave(finalAmount, finalDate, finalDescription, null)
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

@Composable
fun ReceiptWebViewDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        },
        text = {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false

                        loadUrl(imageUrl)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
        }
    )
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