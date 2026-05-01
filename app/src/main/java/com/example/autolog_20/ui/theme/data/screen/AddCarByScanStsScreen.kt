package com.example.autolog_20.ui.theme.data.screen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarByScanStsScreen(navController: NavController) {
    val context = LocalContext.current
    var showImageSourceSheet by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    var extractedVin by remember { mutableStateOf("") }
    var extractedBrand by remember { mutableStateOf("") }
    var extractedModel by remember { mutableStateOf("") }
    var extractedYear by remember { mutableStateOf(0) }
    var extractedColor by remember { mutableStateOf("") }
    var extractedNumberPlate by remember { mutableStateOf("") }
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    var dataExtracted by remember { mutableStateOf(false) }

    fun isValidRussianPlate(plate: String): Boolean {
        var cleanPlate = plate.uppercase()
            .replace("O", "0")
            .replace("I", "1")
            .replace("L", "1")
            .replace("\\s".toRegex(), "")

        // Конвертируем латиницу в кириллицу
        cleanPlate = cleanPlate
            .replace('A', 'А')
            .replace('B', 'В')
            .replace('E', 'Е')
            .replace('K', 'К')
            .replace('M', 'М')
            .replace('H', 'Н')
            .replace('O', 'О')
            .replace('P', 'Р')
            .replace('C', 'С')
            .replace('T', 'Т')
            .replace('Y', 'У')
            .replace('X', 'Х')

        // Стандартный российский номер
        val standardRegex = Regex("^[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$")

        return standardRegex.matches(cleanPlate)
    }

    fun extractCarInfo(text: String) {
        val upperText = text.uppercase()
        val lines = text.lines()

        Log.d("STS_SCAN", "=== НАЧАЛО РАСПОЗНАВАНИЯ ===")
        Log.d("STS_SCAN", "Исходный текст:\n$text")
        Log.d("STS_SCAN", "==================================")

        // 1. Поиск VIN (17 символов)
        val vinRegex = Regex("[A-HJ-NPR-Z0-9]{17}")
        val vinMatch = vinRegex.find(upperText)
        extractedVin = vinMatch?.value ?: ""
        Log.d("STS_SCAN", "VIN: $extractedVin")

        var yearFound = false
        val yearRegex = Regex("\\b(19[8-9][0-9]|20[0-2][0-9]|202[0-8])\\b")

        // Варианты написания "ГОД ВЫПУСКА" с ошибками OCR
        val yearPatterns = listOf(
            "ГОД ВЫПУСКА", "ГОД ВЫПУСКА ТС", "ГОД ВЫПУСКА ТС:", "ГОД ВЫПУСКА:",
            "GOD VYPUSKA", "LOX YEXA", "L'OA BELYKCKA", "L'O A BENYCKA",
            "I'O A BENYCKA", "TOA BENYCKA", "FOA BENYCKA", "IOA BENYCKA",
            "l'oA BEnyCka", "lox yexa", "l'oA BEnyCka", "1'O A BEHY CKA"
        )

        for (i in lines.indices) {
            val line = lines[i].uppercase()

            // Проверяем любую из вариаций
            val containsYearLabel = yearPatterns.any { line.contains(it.uppercase()) }

            if (containsYearLabel) {
                // Ищем год в этой строке
                var yearMatch = yearRegex.find(line)
                if (yearMatch == null && i + 1 < lines.size) {
                    // Ищем в следующей строке
                    yearMatch = yearRegex.find(lines[i + 1].uppercase())
                }
                if (yearMatch != null) {
                    extractedYear = yearMatch.value.toIntOrNull() ?: 0
                    yearFound = true
                    break
                }
            }
        }

        // Если не нашли, ищем любое 4-значное число в разумном диапазоне
        if (!yearFound || extractedYear == 0) {
            val allNumbers = yearRegex.findAll(upperText).toList()
            for (match in allNumbers) {
                val year = match.value.toIntOrNull() ?: 0
                if (year in 1980..2028) {
                    extractedYear = year
                    yearFound = true
                    break
                }
            }
        }
        Log.d("STS_SCAN", "Год выпуска: $extractedYear")

        // 3. Поиск номера (с учетом пробелов)
        var numberFound = false

        // Ищем строки с "Регистрационный знак" или похожими фразами
        for (i in lines.indices) {
            val line = lines[i]
            val lowerLine = line.lowercase()

            if (lowerLine.contains("регистрационный") || lowerLine.contains("регистрационны") ||
                lowerLine.contains("perncтpau") || lowerLine.contains("perherp") ||
                lowerLine.contains("государственный") || lowerLine.contains("t'ocyaap")) {

                // Ищем номер в этой строке
                val parts = line.split(Regex("[:\\s]+"))
                for (part in parts) {
                    val candidate = part.uppercase()
                        .replace("O", "0")
                        .replace("I", "1")
                        .replace("L", "1")
                    if (isValidRussianPlate(candidate) && candidate.length > 5) {
                        extractedNumberPlate = candidate
                        numberFound = true
                        break
                    }
                }

                // Проверяем следующую строку
                if (!numberFound && i + 1 < lines.size) {
                    val nextLine = lines[i + 1].uppercase()
                        .replace("O", "0")
                        .replace("I", "1")
                        .replace("L", "1")
                    if (isValidRussianPlate(nextLine) && nextLine.length > 5) {
                        extractedNumberPlate = nextLine
                        numberFound = true
                    }
                }
                break
            }
        }

        if (!numberFound) {
            val plateWithSpaceRegex = Regex("[АВЕКМНОРСТУХ]\\d{3}\\s?[АВЕКМНОРСТУХ]{2}\\s?\\d{2,3}")
            val match = plateWithSpaceRegex.find(upperText)
            if (match != null) {
                extractedNumberPlate = match.value.replace("\\s".toRegex(), "")
            }
        }

        Log.d("STS_SCAN", "Номер: $extractedNumberPlate")

        var colorFound = false
        val colorPatterns = listOf(
            "ЦВЕТ", "LJET", "LJET:", "lsEr", "lser", "LSER", "LJET", "LJET:"
        )

        for (i in lines.indices) {
            val line = lines[i]
            for (pattern in colorPatterns) {
                if (line.contains(pattern, ignoreCase = true)) {
                    // Ищем цвет в этой строке
                    val parts = line.split(Regex("[:\\s]+"))
                    for (j in parts.indices) {
                        if (parts[j].equals(pattern, ignoreCase = true) && j + 1 < parts.size) {
                            var color = parts[j + 1].uppercase()
                                .replace(Regex("[^А-ЯA-Z\\-\\/]"), "")
                            if (color.isNotEmpty() && color.length < 30) {
                                extractedColor = color
                                    .lowercase()
                                    .replaceFirstChar { it.uppercase() }
                                colorFound = true
                                break
                            }
                        }
                    }
                    if (!colorFound && i + 1 < lines.size) {
                        val nextLine = lines[i + 1].trim().uppercase()
                            .replace(Regex("[^А-ЯA-Z\\-\\/]"), "")
                        if (nextLine.isNotEmpty() && nextLine.length < 30) {
                            extractedColor = nextLine
                                .lowercase()
                                .replaceFirstChar { it.uppercase() }
                            colorFound = true
                        }
                    }
                    break
                }
            }
            if (colorFound) break
        }

        // Если не нашли, ищем по списку цветов
        if (!colorFound) {
            val commonColors = listOf(
                "ЧЕРНЫЙ", "БЕЛЫЙ", "СЕРЕБРИСТЫЙ", "СЕРЫЙ", "КРАСНЫЙ", "СИНИЙ",
                "ЗЕЛЕНЫЙ", "ЖЕЛТЫЙ", "ОРАНЖЕВЫЙ", "ФИОЛЕТОВЫЙ", "КОРИЧНЕВЫЙ",
                "БЕЖЕВЫЙ", "ГОЛУБОЙ", "СЕРЫЙ/GREY", "GREY", "3EAEHbIЙ", "3EAEHBA"
            )

            for (color in commonColors) {
                if (upperText.contains(color)) {
                    extractedColor = color
                        .replace("3EAEHBA", "Зеленый")
                        .replace("3EAEHbIЙ", "Зеленый")
                        .split("/")[0]
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                    break
                }
            }
        }

        val colorMapping = mapOf(
            "BLACK" to "Черный", "WHITE" to "Белый", "SILVER" to "Серебристый",
            "GRAY" to "Серый", "GREY" to "Серый", "RED" to "Красный", "BLUE" to "Синий",
            "GREEN" to "Зеленый", "YELLOW" to "Желтый"
        )

        for ((eng, rus) in colorMapping) {
            if (upperText.contains(eng)) {
                extractedColor = rus
                break
            }
        }

        Log.d("STS_SCAN", "Цвет: $extractedColor")


        var brandModelLine = ""
        var brandModelFound = false

        for (i in lines.indices) {
            val line = lines[i]
            if (line.contains("МАРКА") && line.contains("МОДЕЛЬ")) {
                // Строка с маркой и моделью
                brandModelLine = line
                brandModelFound = true
                break
            } else if (line.contains("МАРКА")) {
                // Может быть в следующей строке
                if (i + 1 < lines.size && !lines[i + 1].contains("ТИП ТС")) {
                    brandModelLine = lines[i + 1]
                    brandModelFound = true
                } else if (line.length > 10) {
                    brandModelLine = line
                    brandModelFound = true
                }
                break
            }
        }

        if (brandModelFound && brandModelLine.isNotEmpty()) {
            var cleanLine = brandModelLine
                .replace(Regex("МАРКА[,.]?\\s*МОДЕЛЬ[:]?"), "")
                .replace(Regex("МАРКА[:]?"), "")
                .replace(Regex("МОДЕЛЬ[:]?"), "")
                .trim()

            Log.d("STS_SCAN", "Строка марка/модель: $cleanLine")

            val knownBrands = listOf(
                "TOYOTA", "NISSAN", "HONDA", "MAZDA", "MITSUBISHI", "SUZUKI", "SUBARU",
                "BMW", "MERCEDES", "AUDI", "VOLKSWAGEN", "VW", "OPEL", "FORD", "CHEVROLET",
                "HYUNDAI", "KIA", "GEELY", "CHERY", "LADA", "ВАЗ", "UAZ", "УАЗ", "GAZ", "ГАЗ",
                "RENAULT", "PEUGEOT", "CITROEN", "SKODA", "SEAT", "VOLVO", "LEXUS", "INFINITI",
                "JAGUAR", "LAND ROVER", "PORSCHE", "TESLA", "HAVAL", "BYD", "ISUZU", "ИЖ", "ИЖБ",
                "ГАЗ", "УАЗ", "КАМАЗ", "ЗАЗ", "РАФ", "ЕВРОАРГО"
            )

            for (brand in knownBrands) {
                if (cleanLine.startsWith(brand) && cleanLine.length > brand.length) {
                    val remaining = cleanLine.substring(brand.length)
                    extractedBrand = brand
                    extractedModel = remaining
                    break
                }
            }

            if (extractedBrand.isEmpty()) {
                val parts = cleanLine.split(Regex("\\s+"), limit = 2)
                if (parts.isNotEmpty()) {
                    extractedBrand = parts[0].trim()
                    if (parts.size > 1) {
                        extractedModel = parts[1].trim()
                    }
                }
            }

            extractedModel = extractedModel.replace(Regex("[^A-Z0-9\\-\\/]"), "")
        }

        if (extractedBrand.isEmpty()) {
            val knownBrands = listOf(
                "TOYOTA", "NISSAN", "HONDA", "MAZDA", "MITSUBISHI", "SUZUKI", "SUBARU",
                "BMW", "MERCEDES", "AUDI", "VOLKSWAGEN", "VW", "OPEL", "FORD", "CHEVROLET",
                "HYUNDAI", "KIA", "GEELY", "CHERY", "LADA", "ВАЗ", "UAZ", "УАЗ", "GAZ", "ГАЗ",
                "RENAULT", "PEUGEOT", "CITROEN", "SKODA", "SEAT", "VOLVO", "LEXUS", "INFINITI",
                "JAGUAR", "LAND ROVER", "PORSCHE", "TESLA", "HAVAL", "BYD", "ISUZU", "ИЖ", "ИЖБ",
                "ГАЗ", "УАЗ", "КАМАЗ", "ЗАЗ", "РАФ", "ЕВРОАРГО"
            )

            for (brand in knownBrands) {
                if (upperText.contains(brand)) {
                    extractedBrand = brand
                    break
                }
            }
        }

        if (extractedModel.isEmpty() && extractedBrand.isNotEmpty()) {
            val brandIndex = upperText.indexOf(extractedBrand)
            if (brandIndex >= 0) {
                val afterBrand = upperText.substring(brandIndex + extractedBrand.length).trim()
                val modelMatch = Regex("^\\s*([A-Z0-9\\-\\/\\s]+?)(?=\\s|$)").find(afterBrand)
                if (modelMatch != null) {
                    extractedModel = modelMatch.groupValues[1].trim().take(20)
                }
            }
        }

        Log.d("STS_SCAN", "Марка: $extractedBrand")
        Log.d("STS_SCAN", "Модель: $extractedModel")
        Log.d("STS_SCAN", "=== КОНЕЦ РАСПОЗНАВАНИЯ ===")
    }



    fun recognizeTextFromUri(uri: Uri) {
        isProcessing = true
        dataExtracted = false
        /*
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val resultText = visionText.text
                        extractCarInfo(resultText)
                        dataExtracted = true
                        isProcessing = false
                        Log.d("STS_SCAN", "Распознанный текст: $resultText")
                    }
                    .addOnFailureListener { e ->
                        Log.e("STS_SCAN", "Ошибка распознавания", e)
                        Toast.makeText(context, "Ошибка распознавания текста", Toast.LENGTH_SHORT).show()
                        isProcessing = false
                    }
            } else {
                Toast.makeText(context, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show()
                isProcessing = false
            }
        } catch (e: Exception) {
            Log.e("STS_SCAN", "Ошибка", e)
            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            isProcessing = false
        }
         */
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoFile?.let { file ->
                selectedImageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                recognizeTextFromUri(selectedImageUri!!)
            }
        }
        showImageSourceSheet = false
    }

    val cameraPermission = Manifest.permission.CAMERA
    val cameraPermissionState = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val timeStamp = System.currentTimeMillis()
            val photoFile = File(context.cacheDir, "sts_$timeStamp.jpg")
            tempPhotoFile = photoFile
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoUri)
        } else {
            Toast.makeText(context, "Для сканирования СТС нужно разрешение на камеру", Toast.LENGTH_SHORT).show()
            showImageSourceSheet = false
        }
    }

    // Ланчер для камеры


    // Ланчер для галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            recognizeTextFromUri(it)
        }
        showImageSourceSheet = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавление по СТС") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
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
            Text(
                text = "Сканирование СТС",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Сфотографируйте или выберите фото свидетельства о регистрации ТС",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка для выбора источника фото
            Button(
                onClick = { showImageSourceSheet = true },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выбрать фото СТС")
            }

            // Показываем изображение, если оно выбрано
            selectedImageUri?.let { uri ->
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val bitmap = runCatching {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }.getOrNull()

                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "СТС фото",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }

            // Индикатор загрузки
            if (isProcessing) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Распознавание текста...")
            }

            // Результаты распознавания
            if (dataExtracted && !isProcessing) {
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Распознанные данные",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = extractedVin,
                            onValueChange = { extractedVin = it.uppercase() },
                            label = { Text("VIN") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = extractedBrand,
                            onValueChange = { extractedBrand = it },
                            label = { Text("Марка") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = extractedModel,
                            onValueChange = { extractedModel = it },
                            label = { Text("Модель") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = if (extractedYear > 0) extractedYear.toString() else "",
                            onValueChange = {
                                extractedYear = it.toIntOrNull() ?: 0
                            },
                            label = { Text("Год выпуска") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = extractedColor,
                            onValueChange = { extractedColor = it },
                            label = { Text("Цвет") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = extractedNumberPlate,
                            onValueChange = { extractedNumberPlate = it.uppercase() },
                            label = { Text("Номер автомобиля") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedImageUri = null
                                    dataExtracted = false
                                    extractedVin = ""
                                    extractedBrand = ""
                                    extractedModel = ""
                                    extractedYear = 0
                                    extractedColor = ""
                                    extractedNumberPlate = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Сбросить")
                            }

                            Button(
                                onClick = {
                                    if (extractedVin.length == 17 &&
                                        extractedBrand.isNotBlank() &&
                                        extractedModel.isNotBlank() &&
                                        extractedYear in 1900..2028 &&
                                        extractedColor.isNotBlank() &&
                                        extractedNumberPlate.isNotBlank()
                                    ) {
                                        navController.navigate(
                                            "add_car_scan_data/" +
                                                    "$extractedVin/" +
                                                    "$extractedBrand/" +
                                                    "$extractedModel/" +
                                                    "$extractedYear/" +
                                                    "$extractedColor/" +
                                                    "$extractedNumberPlate"
                                        )
                                    } else {
                                        val errors = mutableListOf<String>()
                                        if (extractedVin.length != 17) errors.add("VIN")
                                        if (extractedBrand.isBlank()) errors.add("марку")
                                        if (extractedModel.isBlank()) errors.add("модель")
                                        if (extractedYear !in 1900..2028) errors.add("год выпуска")
                                        if (extractedColor.isBlank()) errors.add("цвет")
                                        if (extractedNumberPlate.isBlank()) errors.add("номер")

                                        val message = when (errors.size) {
                                            1 -> "Заполните поле: ${errors[0]}"
                                            else -> "Заполните поля: ${errors.joinToString(", ")}"
                                        }
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Продолжить")
                            }
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet для выбора источника фото
    if (showImageSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
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
                                val photoFile = File(context.cacheDir, "sts_$timeStamp.jpg")
                                tempPhotoFile = photoFile
                                val photoUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                cameraLauncher.launch(photoUri)
                            } else {
                                cameraPermissionState.launch(cameraPermission)
                            }
                            showImageSourceSheet = false
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

                TextButton(onClick = { showImageSourceSheet = false }) {
                    Text("Отмена")
                }
            }
        }
    }
}