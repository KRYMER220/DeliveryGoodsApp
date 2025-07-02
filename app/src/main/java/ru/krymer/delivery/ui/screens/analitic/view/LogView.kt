package ru.krymer.delivery.ui.screens.analitic.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.data.model.LoggerModel
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.convertToTextDate

@Composable
fun LogView(state: AnaliticViewState) {
    val logs = state.logs.collectAsState().value
    if (logs.isNotEmpty()) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = logs) { log ->
                    ItemLog(log)
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}

@Composable
fun ItemLog(log: LoggerModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.secondaryVariant, shape = RoundedCornerShape(10.dp))
            .padding(3.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = convertToTextDate(log.date, Constants.PatternDate.FULL),
            fontSize = 12.sp,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.weight(0.4f), style = AppTheme.typography.titleSmall
        )
        Text(
            text = log.log,
            fontSize = 14.sp,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.weight(0.6f), style = AppTheme.typography.titleSmall
        )
    }
}