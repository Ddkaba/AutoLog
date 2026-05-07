package com.example.autolog_20.ui.theme

import androidx.compose.ui.graphics.Color

val BackgroundDark = Color(0xFF1A1C22)
val SurfaceDark = Color(0xFF24272F)
val SurfaceVariantDark = Color(0xFF2C2C2C)
val OnSurfaceVariantDark = Color(0xFF9E9E9E)

val BackgroundLight = Color(0xFFF5F5F5)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFE8E8E8)
val OnSurfaceVariantLight = Color(0xFF666666)
val OnSurfaceLight = Color(0xFF1A1A1A)

val Primary = Color(0xFF6B7280)
val PrimaryVariant = Color(0xFF9CA3AF)
val OnPrimary = Color.White
val TextPrimary = Color(0xFFE5E7EB)
val Error = Color(0xFFFCA5A5)
val DeleteColor = Color(0xFFE73030)

val FilterTileBackgroundLight = Color(0xFFE8E8E8)  // Светло-серый фон
val FilterTileTextTitleLight = Color(0xFF666666)   // Серый для заголовка
val FilterTileTextSubtitleLight = Color(0xFF1A1A1A) // Тёмный для подзаголовка

val FilterTileBackgroundDark = Color(0xFF2C2C2C)  // Тёмно-серый фон
val FilterTileTextTitleDark = Color(0xFF9E9E9E)   // Светло-серый для заголовка
val FilterTileTextSubtitleDark = Color(0xFFFFFFFF) // Белый для подзаголовка

val TileToDark = Color(0xFF4A6FA5)
val TileTiresDark = Color(0xFF6B9E78)
val TileExpensesDark = Color(0xFFB07D57)
val TileMileageDark = Color(0xFF8A9A5B)
val TileServiceDark = Color(0xFF9F7EAA)

val TileToLight = Color(0xFF2E5BFF)
val TileTiresLight = Color(0xFF34C759)
val TileExpensesLight = Color(0xFFFF6B35)
val TileMileageLight = Color(0xFFCCF35A)
val TileServiceLight = Color(0xFFEABAFF)

val TotalAllTimeTextDark = Color(0xFFFFFFFF)
val TotalAllTimeTextLight = Color(0xFF1A1A1A)

val ExpenseAmountLight = Color(0xFFB07D57)
val ExpenseAmountDark = Color(0xFFF5E8D3)

val CategoryTechnical = Color(0xFFFF4444)      // Техническое обслуживание - красный
val CategoryRepair = Color(0xFFFF8C00)         // Ремонт - оранжевый
val CategoryFuel = Color(0xFFFFFF00)           // Топливо - желтый
val CategoryInsurance = Color(0xFF00FF00)      // Страхование - зеленый
val CategoryTaxes = Color(0xFF00FFFF)          // Налоги - голубой
val CategoryWash = Color(0xFF0000CD)           // Мойка и уход - синий
val CategoryParking = Color(0xFF8A2BE2)        // Парковка - фиолетовый
val CategoryFines = Color(0xFFFF1493)          // Штрафы - розовый
val CategoryParts = Color(0xFFF0B2AB)          // Запчасти - светло-розовый
val CategoryOther = Color(0xFFF05340)          // Прочие - коралловый

val categoryColorMap = mapOf(
    "Техническое обслуживание" to CategoryTechnical,
    "Ремонт" to CategoryRepair,
    "Топливо" to CategoryFuel,
    "Страхование" to CategoryInsurance,
    "Налоги и пошлины" to CategoryTaxes,
    "Мойка и уход" to CategoryWash,
    "Парковка и хранение" to CategoryParking,
    "Штрафы" to CategoryFines,
    "Запчасти и расходники" to CategoryParts,
    "Прочие расходы" to CategoryOther
)

object ThemeColors {
    var tileTo: Color = TileToDark
    var tileTires: Color = TileTiresDark
    var tileExpenses: Color = TileExpensesDark
    var tileMileage: Color = TileMileageDark
    var tileService: Color = TileServiceDark
    var filterTileBackground: Color = FilterTileBackgroundDark
    var filterTileTextTitle: Color = FilterTileTextTitleDark
    var filterTileTextSubtitle: Color = FilterTileTextSubtitleDark
    var totalAllTimeText: Color = TotalAllTimeTextDark
    var expenseAmount: Color = ExpenseAmountDark

    fun updateColors(isDark: Boolean) {
        if (isDark) {
            tileTo = TileToDark
            tileTires = TileTiresDark
            tileExpenses = TileExpensesDark
            tileMileage = TileMileageDark
            tileService = TileServiceDark
            filterTileBackground = FilterTileBackgroundDark
            filterTileTextTitle = FilterTileTextTitleDark
            filterTileTextSubtitle = FilterTileTextSubtitleDark
            totalAllTimeText = TotalAllTimeTextDark
            expenseAmount = ExpenseAmountDark
        } else {
            tileTo = TileToLight
            tileTires = TileTiresLight
            tileExpenses = TileExpensesLight
            tileMileage = TileMileageLight
            tileService = TileServiceLight
            filterTileBackground = FilterTileBackgroundLight
            filterTileTextTitle = FilterTileTextTitleLight
            filterTileTextSubtitle = FilterTileTextSubtitleLight
            totalAllTimeText = TotalAllTimeTextLight
            expenseAmount = ExpenseAmountLight
        }
    }
}

val TileTo get() = ThemeColors.tileTo
val TileTires get() = ThemeColors.tileTires
val TileExpenses get() = ThemeColors.tileExpenses
val TileMileage get() = ThemeColors.tileMileage
val TileService get() = ThemeColors.tileService
val FilterTileBackground get() = ThemeColors.filterTileBackground
val FilterTileTextTitle get() = ThemeColors.filterTileTextTitle
val FilterTileTextSubtitle get() = ThemeColors.filterTileTextSubtitle
val TotalAllTimeText get() = ThemeColors.totalAllTimeText
val ExpenseAmountColor get() = ThemeColors.expenseAmount