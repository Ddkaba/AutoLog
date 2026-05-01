package com.example.autolog_20.ui.theme.data.model

object DateFormat {
    fun formatDateToDisplay(dateStr: String): String {
        return try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                "${parts[2]}.${parts[1]}.${parts[0]}"
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

     fun formatDateHeader(dateStr: String): String {
        val date = try {
            java.time.LocalDate.parse(dateStr)
        } catch (e: Exception) {
            return dateStr
        }

        return when {
            date == java.time.LocalDate.now() -> "Сегодня"
            date == java.time.LocalDate.now().minusDays(1) -> "Вчера"
            else -> {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale("ru"))
                date.format(formatter)
            }
        }
    }
}