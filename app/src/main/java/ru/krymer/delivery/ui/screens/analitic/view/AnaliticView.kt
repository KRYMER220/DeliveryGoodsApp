package ru.krymer.delivery.ui.screens.analitic.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun AnaliticView(
    onClickAll: () -> Unit,
    onClickTrip: () -> Unit,
    onClickClient: () -> Unit,
    onClickLogs: () -> Unit
) {
    Column(verticalArrangement = Arrangement.Center) {
        Button(
            onClick = onClickAll, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.onSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.all_info), style = TextStyle(
                    color = AppTheme.colors.onPrimary
                ), fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onClickTrip, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.onSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.trip), style = TextStyle(
                    color = AppTheme.colors.onPrimary
                ), fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onClickClient, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.onSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.shop), style = TextStyle(
                    color = AppTheme.colors.onPrimary
                ), fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onClickLogs, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.onSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.logs), style = TextStyle(
                    color = AppTheme.colors.onPrimary
                ), fontSize = 20.sp
            )
        }
    }
}