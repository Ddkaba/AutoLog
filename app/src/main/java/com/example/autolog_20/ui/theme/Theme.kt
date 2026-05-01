package com.example.autolog_20.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.autolog_20.R

@Composable
fun AutoLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        background = BackgroundDark,
        surface = SurfaceDark,
        onSurface = TextPrimary,
        error = Error,
        onError = Color.White,
    )

    val typography = Typography(
        // Заголовок крупный (название приложения, экраны)
        titleLarge = TextStyle(
            fontFamily = FontFamily(Font(R.font.montserrat_semibold)),
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),

        // Основной текст
        bodyLarge = TextStyle(
            fontFamily = FontFamily(Font(R.font.montserrat_regular)),
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),

        // Кнопки, лейблы, мелкий текст
        labelLarge = TextStyle(
            fontFamily = FontFamily(Font(R.font.montserrat_medium)),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),

        // Дополнительные стили, которые часто используются
        bodyMedium = TextStyle(
            fontFamily = FontFamily(Font(R.font.montserrat_regular)),
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),

        labelMedium = TextStyle(
            fontFamily = FontFamily(Font(R.font.montserrat_medium)),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}