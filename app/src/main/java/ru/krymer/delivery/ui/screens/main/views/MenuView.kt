package ru.krymer.delivery.ui.screens.main.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants

@Composable
fun MenuView(
    onRouteClick: () -> Unit,
    onProductClick: () -> Unit,
    onCourierClick: () -> Unit,
    onTripClick: () -> Unit,
    onAnaliticClick: () -> Unit,
    user: UserModel
) {
    val listMenu = listOf(
        R.string.route,
        R.string.products,
        R.string.couriers,
        R.string.analitic,
        R.string.trip
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = user.name,
                style = AppTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = AppTheme.colors.onSecondary
            )

            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(items = listMenu) { index, nameMenu ->
                    when(nameMenu) {
                        R.string.trip -> CustomButton(buttonName = stringResource(nameMenu), routeTo = onTripClick)
                        R.string.route -> if (user.isModOrAdminOrSys()) {
                            CustomButton(buttonName = stringResource(nameMenu), routeTo = onRouteClick)
                        }

                        R.string.couriers -> if (user.isSysOrAdmin()) {
                            CustomButton(buttonName = stringResource(nameMenu), routeTo = onCourierClick)
                        }

                        R.string.analitic -> if (user.isSysOrAdmin()) {
                            CustomButton(buttonName = stringResource(nameMenu), routeTo = onAnaliticClick)
                        }

                        R.string.products -> if (user.isSysOrAdmin()) {
                            CustomButton(buttonName = stringResource(nameMenu), routeTo = onProductClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomButton(routeTo: () -> Unit, buttonName: String) {
    Button(
        onClick = routeTo, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.secondary,
            disabledContainerColor = AppTheme.colors.secondaryVariant,
            disabledContentColor = AppTheme.colors.secondaryVariant,
        )
    ) {
        Text(
            text = buttonName,
            color = AppTheme.colors.onSecondary,
            style = AppTheme.typography.titleLarge
        )
    }
}