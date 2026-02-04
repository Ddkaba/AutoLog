package com.example.autolog_20.ui.theme.data.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AddCarScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Добавление автомобиля",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Форма добавления будет здесь позже")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Назад")
        }
    }
}