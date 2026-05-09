package com.example.autolog_20.ui.theme.data.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.autolog_20.ui.theme.data.model.DrivingSurvey
import com.example.autolog_20.ui.theme.data.model.SurveySummary
import com.example.autolog_20.ui.theme.data.model.viewmodel.AddCarByVinViewModel
import com.example.autolog_20.ui.theme.data.model.viewmodel.DrivingSurveyViewModel

@Composable
fun DrivingSurveyScreen(
    survey: DrivingSurvey,
    viewModel: DrivingSurveyViewModel,
    addCarByVinViewModel: AddCarByVinViewModel? = null,
    onFinish: ((SurveySummary) -> Unit)? = null
) {
    var currentQuestion by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Расскажите о своей эксплуатации автомобиля",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        when (currentQuestion) {
            1 -> QuestionCard(
                question = "Как часто вы ездите по городу с пробками?",
                options = listOf("Редко", "Иногда", "Часто", "Почти всегда"),
                selectedIndex = survey.cityDriving - 1,
                onSelect = { index -> viewModel.updateCityDriving(index + 1) }
            )

            2 -> QuestionCard(
                question = "Как часто вы совершаете дальние поездки по трассе?",
                options = listOf("Редко", "Иногда", "Часто", "Очень часто"),
                selectedIndex = survey.highwayDriving - 1,
                onSelect = { index -> viewModel.updateHighwayDriving(index + 1) }
            )

            3 -> QuestionCard(
                question = "Как часто вы ездите по бездорожью или плохим дорогам?",
                options = listOf("Почти никогда", "Редко", "Иногда", "Часто"),
                selectedIndex = survey.offroadDriving - 1,
                onSelect = { index -> viewModel.updateOffroadDriving(index + 1) }
            )

            4 -> QuestionCard(
                question = "Какой у вас стиль вождения?",
                options = listOf("Спокойный", "Нормальный", "Динамичный", "Агрессивный"),
                selectedIndex = when(survey.drivingStyle) {
                    "calm" -> 0
                    "normal" -> 1
                    "dynamic" -> 2
                    "aggressive" -> 3
                    else -> -1
                },
                onSelect = { index ->
                    val style = when(index) {
                        0 -> "calm"
                        1 -> "normal"
                        2 -> "dynamic"
                        3 -> "aggressive"
                        else -> "normal"
                    }
                    viewModel.updateDrivingStyle(style)
                }
            )

            5 -> QuestionCard(
                question = "Какой тип масла вы используете?",
                options = listOf("Синтетика", "Полусинтетика", "Минеральное"),
                selectedIndex = when (survey.oilType) {
                    "Synthetic" -> 0
                    "SemiSynthetic" -> 1
                    "Mineral" -> 2
                    else -> -1
                },
                onSelect = { index ->
                    val oilType = when (index) {
                        0 -> "Synthetic"
                        1 -> "SemiSynthetic"
                        2 -> "Mineral"
                        else -> "Synthetic"
                    }
                    viewModel.updateOilType(oilType)
                }
            )

            6 -> QuestionCard(
                question = "В каком климате вы эксплуатируете автомобиль?",
                options = listOf("Жаркий", "Холодный", "Умеренный", "Частые перепады температур"),
                selectedIndex = when (survey.climate) {
                    "Hot" -> 0
                    "Cold" -> 1
                    "Moderate" -> 2
                    "Extreme" -> 3
                    else -> -1
                },
                onSelect = { index ->
                    val climate = when (index) {
                        0 -> "Hot"
                        1 -> "Cold"
                        2 -> "Moderate"
                        3 -> "Extreme"
                        else -> "Moderate"
                    }
                    viewModel.updateClimate(climate)
                }
            )

            7 -> QuestionCard(
                question = "Как часто вы ездите на короткие расстояния (менее 10 км)?",
                options = listOf("Редко", "Иногда", "Часто"),
                selectedIndex = survey.shortTrips - 1,
                onSelect = { index -> viewModel.updateShortTrips(index + 1) }
            )

            8 -> QuestionCard(
                question = "Как часто вы ездите с полной загрузкой или буксируете прицеп?",
                options = listOf("Никогда", "Редко", "Иногда", "Часто"),
                selectedIndex = survey.heavyLoad - 1,
                onSelect = { index -> viewModel.updateHeavyLoad(index + 1) }
            )

            9 -> QuestionCard(
                question = "Как часто вы ездите на высоких оборотах (более 3000 об/мин)?",
                options = listOf("Редко", "Иногда", "Часто"),
                selectedIndex = survey.highRpm - 1,
                onSelect = { index -> viewModel.updateHighRpm(index + 1) }
            )

            10 -> QuestionCard(
                question = "Ездите ли вы по пыльным или грязным дорогам?",
                options = listOf("Нет", "Иногда", "Да"),
                selectedIndex = survey.dustyRoads - 1,
                onSelect = { index -> viewModel.updateDustyRoads(index + 1) }
            )

            11 -> QuestionCard(
                question = "Какой у вас тип привода?",
                options = listOf("Передний (FWD)", "Задний (RWD)", "Полный (AWD)"),
                selectedIndex = when (survey.driveType) {
                    "FWD" -> 0
                    "RWD" -> 1
                    "AWD" -> 2
                    else -> -1
                },
                onSelect = { index: Int ->
                    val drive = when(index) {
                        0 -> "FWD"
                        1 -> "RWD"
                        2 -> "AWD"
                        else -> "FWD"
                    }
                    viewModel.setDriveType(drive)
                }
            )

            12 -> QuestionCard(
                question = "Какая у вас коробка передач?",
                options = listOf("Механика", "Автомат", "Вариатор (CVT)", "Робот (AMT)"),
                selectedIndex = when (survey.transmission) {
                    "MT" -> 0
                    "AT" -> 1
                    "CVT" -> 2
                    "AMT" -> 3
                    else -> -1
                },
                onSelect = { index: Int ->
                    val trans = when(index) {
                        0 -> "MT"
                        1 -> "AT"
                        2 -> "CVT"
                        3 -> "AMT"
                        else -> "AT"
                    }
                    viewModel.setTransmission(trans)
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentQuestion > 1) {
                OutlinedButton(
                    onClick = { currentQuestion-- },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Назад")
                }
            }

            Button(
                onClick = {
                    if (currentQuestion < 12) {
                        currentQuestion++
                    } else {
                        viewModel.finishSurvey(
                            addCarByVinViewModel = addCarByVinViewModel,
                            onFinish = onFinish
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = true
            ) {
                Text(if (currentQuestion < 12) "Далее" else "Завершить")
            }
        }
    }
}

@Composable
fun QuestionCard(question: String, options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Column {
        Text(
            text = question,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        options.forEachIndexed { index, option ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelect(index) },
                colors = CardDefaults.cardColors(
                    containerColor = if (index == selectedIndex)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = index == selectedIndex,
                        onClick = { onSelect(index) }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
