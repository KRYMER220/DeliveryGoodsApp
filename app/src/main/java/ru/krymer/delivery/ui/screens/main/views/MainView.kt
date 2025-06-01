package ru.krymer.delivery.ui.screens.main.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.Screens
import ru.krymer.delivery.data.model.user.UserModel

@Composable
fun MainView(user: UserModel, navigateTo: (Screens) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.exit_app),
                    contentDescription = "sign_out",
                    modifier = Modifier
                        .clickable {
                            navigateTo(Screens.Auth)
                        }
                        .align(Alignment.TopEnd)
                        .size(40.dp))
            }
            Spacer(modifier = Modifier.padding(20.dp))
            MenuView(
                onRouteClick = {
                    navigateTo(Screens.Route)
                }, onProductClick = {
                    navigateTo(Screens.Product)
                }, onCourierClick = {
                    navigateTo(Screens.Courier)
                }, onTripClick = {
                    navigateTo(Screens.Trip)
                }, onAnaliticClick = {
                    navigateTo(Screens.Analitic)
                }, user = user)
        }

    }
}