package ru.krymer.delivery.ui.screens.main

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.navigation.NavigationTree
import ru.krymer.delivery.ui.screens.main.models.MenuAction
import ru.krymer.delivery.ui.screens.main.models.MenuEvent
import ru.krymer.delivery.ui.screens.main.views.MenuView
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun MenuScreen(
    menuViewModel: MainViewModel,
    navController: NavController
) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState = menuViewModel.viewState.collectAsState().value
    val sharedViewState by sharedViewModel.viewState.collectAsState()
    val user = sharedViewState.user.collectAsState().value
    if (user != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(painter = painterResource(id = R.drawable.exit_app),
                        contentDescription = "sign_out",
                        modifier = Modifier
                            .clickable {
                                menuViewModel.obtainEvent(MenuEvent.SignOutUser)
                            }
                            .align(Alignment.TopEnd)
                            .size(40.dp))
                }
                Spacer(modifier = Modifier.padding(20.dp))
                MenuView(onRouteClick = {
                    menuViewModel.obtainEvent(MenuEvent.RouteClickedToOpen)
                }, onProductClick = {
                    menuViewModel.obtainEvent(MenuEvent.ProductClickedToOpen)
                }, onCourierClick = {
                    menuViewModel.obtainEvent(MenuEvent.CourierClickedToOpen)
                }, onTripClick = {
                    menuViewModel.obtainEvent(MenuEvent.TripClickedToOpen)
                }, onAnaliticClick = {
                    menuViewModel.obtainEvent(MenuEvent.AnaliticClickedToOpen)
                }, sharedViewModel = sharedViewModel,
                    user = user
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = viewState.versionValue,
                        color = AppTheme.colors.onSecondary,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                    )
                }
            }
        }

        LaunchedEffect(key1 = viewState.menuAction) {
            when (viewState.menuAction) {
                is MenuAction.OpenRoute -> {
                    navController.navigate(NavigationTree.Route.name)
                }

                is MenuAction.OpenProduct -> {
                    navController.navigate(NavigationTree.Product.name)
                }

                is MenuAction.OpenCouriers -> {
                    navController.navigate(NavigationTree.Courier.name)
                }

                is MenuAction.OpenTrips -> {
                    navController.navigate(NavigationTree.Trip.name)
                }

                is MenuAction.None -> {}

                is MenuAction.OpenAnalitic -> {
                    navController.navigate(NavigationTree.Analitic.name)
                }
            }
        }

        DisposableEffect(key1 = Unit) {
            onDispose {
                menuViewModel.obtainEvent(MenuEvent.MenuActionInvoked)
            }
        }

    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}