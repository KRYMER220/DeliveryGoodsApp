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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.navigation.NavigationTree
import ru.krymer.delivery.ui.screens.main.models.MenuAction
import ru.krymer.delivery.ui.screens.main.models.MenuEvent
import ru.krymer.delivery.ui.screens.main.models.MenuViewState
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState

@Composable
fun MainView(state: MenuViewState, user: UserModel, navigateTo: (String) -> Unit, event: (MenuEvent) -> Unit) {
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
                            event(MenuEvent.SignOutUser)
                        }
                        .align(Alignment.TopEnd)
                        .size(40.dp))
            }
            Spacer(modifier = Modifier.padding(20.dp))
            MenuView(
                onRouteClick = {
                    event(MenuEvent.RouteClickedToOpen)
                }, onProductClick = {
                    event(MenuEvent.ProductClickedToOpen)
                }, onCourierClick = {
                    event(MenuEvent.CourierClickedToOpen)
                }, onTripClick = {
                    event(MenuEvent.TripClickedToOpen)
                }, onAnaliticClick = {
                    event(MenuEvent.AnaliticClickedToOpen)
                }, user = user)
        }

        when (state.menuAction) {
            is MenuAction.OpenRoute -> {
                navigateTo(NavigationTree.Route.name)
            }

            is MenuAction.OpenProduct -> {
                navigateTo(NavigationTree.Product.name)
            }

            is MenuAction.OpenCouriers -> {
                navigateTo(NavigationTree.Courier.name)
            }

            is MenuAction.OpenTrips -> {
                navigateTo(NavigationTree.Trip.name)
            }

            is MenuAction.None -> {}

            is MenuAction.OpenAnalitic -> {
                navigateTo(NavigationTree.Analitic.name)
            }
        }

        DisposableEffect(key1 = Unit) {
            onDispose {
                event(MenuEvent.MenuActionInvoked)
            }
        }
    }
}