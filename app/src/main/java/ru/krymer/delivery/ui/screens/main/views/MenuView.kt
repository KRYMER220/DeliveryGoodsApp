package ru.krymer.delivery.ui.screens.main.views

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun MenuView(
    onRouteClick: () -> Unit,
    onProductClick: () -> Unit,
    onCourierClick: () -> Unit,
    onTripClick: () -> Unit,
    onAnaliticClick: () -> Unit,
    sharedViewState: SharedViewState,
    sharedViewModel: SharedViewModel,
    user: UserModel
) {
    Column {
        Text(
            text = user.name,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.textColor
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onTripClick, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.onSecondary
            )
        ) {
            Text(
                text = stringResource(id = R.string.trip), style = TextStyle(
                    color = AppTheme.colors.onPrimary
                ), fontSize = 20.sp
            )
        }
        if (sharedViewModel.initSysAdmMod()) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRouteClick, shape = RoundedCornerShape(10.dp), modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.onSecondary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.route), style = TextStyle(
                        color = AppTheme.colors.onPrimary
                    ), fontSize = 20.sp
                )
            }
        }
        if (sharedViewModel.initSysAdm()) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onProductClick, shape = RoundedCornerShape(10.dp), modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.onSecondary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.products), style = TextStyle(
                        color = AppTheme.colors.onPrimary
                    ), fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onCourierClick, shape = RoundedCornerShape(10.dp), modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.onSecondary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.couriers), style = TextStyle(
                        color = AppTheme.colors.onPrimary
                    ), fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAnaliticClick, shape = RoundedCornerShape(10.dp), modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.onSecondary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.analitic), style = TextStyle(
                        color = AppTheme.colors.onPrimary
                    ), fontSize = 20.sp
                )
            }
        }

    }
}