package com.example.autolog_20.ui.theme.data.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarByScanDataScreen(
    navController: NavController,
    vin: String,
    brand: String,
    model: String,
    year: Int,
    color: String,
    numberPlate: String
) {
    var editableVin by remember { mutableStateOf(vin) }
    var editableBrand by remember { mutableStateOf(brand) }
    var editableModel by remember { mutableStateOf(model) }
    var editableYear by remember { mutableStateOf(year.toString()) }
    var editableColor by remember { mutableStateOf(color) }
    var editableNumberPlate by remember { mutableStateOf(numberPlate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("@string/data_from_STS") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "@string/back")
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
                text = "@string/data_from_STS",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "@string/check_data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "@string/info_about_car",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editableVin,
                        onValueChange = { editableVin = it.uppercase() },
                        label = { Text("VIN") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = editableVin.length != 17 && editableVin.isNotEmpty(),
                        supportingText = {
                            if (editableVin.length != 17 && editableVin.isNotEmpty()) {
                                Text("@string/vin_17")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editableBrand,
                        onValueChange = { editableBrand = it },
                        label = { Text("@string/brand") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editableModel,
                        onValueChange = { editableModel = it },
                        label = { Text("@string/brand") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editableYear,
                        onValueChange = { editableYear = it.filter { char -> char.isDigit() }.take(4) },
                        label = { Text("@string/manufacture_year") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = editableYear.isNotEmpty() &&
                                (editableYear.length != 4 || editableYear.toIntOrNull() !in 1900..2028),
                        supportingText = {
                            if (editableYear.isNotEmpty() && editableYear.length == 4) {
                                val yearInt = editableYear.toIntOrNull()
                                if (yearInt != null && yearInt !in 1900..2028) {
                                    Text("@string/uncorrected_year")
                                }
                            } else if (editableYear.isNotEmpty() && editableYear.length != 4) {
                                Text("@string/year_4")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editableColor,
                        onValueChange = { editableColor = it },
                        label = { Text("@string/color") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editableNumberPlate,
                        onValueChange = { editableNumberPlate = it.uppercase() },
                        label = { Text("@string/number") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = editableNumberPlate.isNotEmpty() &&
                                !Regex("^[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$").matches(editableNumberPlate),
                        supportingText = {
                            if (editableNumberPlate.isNotEmpty() &&
                                !Regex("^[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$").matches(editableNumberPlate)) {
                                Text("@string/uncorrected_number")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate(
                        "add_car_manual_with_data_confirm/" +
                                "$editableVin/" +
                                "$editableBrand/" +
                                "$editableModel/" +
                                "${editableYear.toIntOrNull() ?: 0}/" +
                                "$editableColor/" +
                                "$editableNumberPlate"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = editableVin.length == 17 &&
                        editableBrand.isNotBlank() &&
                        editableModel.isNotBlank() &&
                        (editableYear.toIntOrNull() ?: 0) in 1900..2028 &&
                        editableColor.isNotBlank() &&
                        editableNumberPlate.isNotBlank()
            ) {
                Text("@string/conti")
            }
        }
    }
}