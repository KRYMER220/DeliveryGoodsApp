package ru.krymer.delivery.ui.screens

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.krymer.delivery.Screens
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.ui.screens.analitic.AnaliticScreen
import ru.krymer.delivery.ui.screens.client.ClientScreen
import ru.krymer.delivery.ui.screens.courier.CourierScreen
import ru.krymer.delivery.ui.screens.login.LoginScreen
import ru.krymer.delivery.ui.screens.main.MenuScreen
import ru.krymer.delivery.ui.screens.product.ProductScreen
import ru.krymer.delivery.ui.screens.route.RouteScreen
import ru.krymer.delivery.ui.screens.shared.models.SharedEvents
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState
import ru.krymer.delivery.ui.screens.shop.ShopScreen
import ru.krymer.delivery.ui.screens.splash.SplashScreen
import ru.krymer.delivery.ui.screens.trip.TripScreen
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants

@ExperimentalMaterial3Api
@Composable
fun ApplicationScreen(
    sharedState: State<SharedViewState>,
    event: (SharedEvents) -> Unit,
    backStack: SnapshotStateList<Screens>,
    modifier: Modifier,
) {

    val user = sharedState.value.user.collectAsState().value
    NavDisplay(
        modifier = modifier,
        backStack = backStack, onBack = { backStack.removeLastOrNull() }, transitionSpec = {
            ContentTransform(
                targetContentEnter = fadeIn(animationSpec = tween(durationMillis = 0)),
                initialContentExit = fadeOut(animationSpec = tween(durationMillis = 0)),
            )
        }, popTransitionSpec = {
            ContentTransform(
                targetContentEnter = fadeIn(animationSpec = tween(durationMillis = 0)),
                initialContentExit = fadeOut(animationSpec = tween(durationMillis = 0)),
            )
        }, entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ), entryProvider = entryProvider {

            entry<Screens.Splash> {
                SplashScreen()
            }

            entry<Screens.Menu> {
                MenuScreen(
                    navigateTo = {
                        when (it) {
                            Screens.Analitic -> backStack.add(Screens.Analitic)
                            Screens.Auth -> {
                                backStack.removeLastOrNull()
                                backStack.add(Screens.Auth)
                                event(SharedEvents.LogOut)
                            }

                            Screens.Courier -> backStack.add(Screens.Courier)
                            Screens.Product -> backStack.add(Screens.Product)
                            Screens.Route -> backStack.add(Screens.Route)
                            Screens.Trip -> backStack.add(Screens.Trip)
                            else -> {}
                        }
                    }, user = user
                )
            }

            entry<Screens.Route> {
                RouteScreen(
                    user = user, openRoute = { route, routes ->
                        backStack.add(Screens.Client(route = route, routes = routes))
                    },
                    popBackStack = {
                        backStack.removeLastOrNull()
                    })
            }

            entry<Screens.Auth> {
                LoginScreen()
            }

            entry<Screens.Analitic> {
                AnaliticScreen(
                    popBackStack = { backStack.removeLastOrNull() }
                )
            }

            entry<Screens.Shop> { key ->
                ShopScreen(
                    popBackStack = { backStack.removeLastOrNull() }, user = user, trip = key.trip
                )
            }

            entry<Screens.Trip> {
                TripScreen(
                    openTrip = {
                        backStack.add(Screens.Shop(trip = it))
                    }, popBackStack = { backStack.removeLastOrNull() }, user = user
                )
            }

            entry<Screens.Client> { key ->
                ClientScreen(
                    route = key.route,
                    user = user,
                    popBackStack = {
                        backStack.removeLastOrNull()
                    },
                    routes = key.routes
                )
            }

            entry<Screens.Product> {
                ProductScreen(
                    popBackStack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Screens.Courier> {
                CourierScreen(
                    popBackStack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Screens.Ban> {
                UserBlocked(logout = {
                    backStack.removeLastOrNull()
                    backStack.add(Screens.Auth)
                    event(SharedEvents.LogOut)
                })
            }
        })
    MessageSnackBar(sharedState.value, modifier = modifier)
}

@Composable
fun UserBlocked(logout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Constants.ERROR.USER_BANNED,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(5.dp))
            Button(
                onClick = logout,
                colors = ButtonColors(
                    containerColor = AppTheme.colors.onSecondary,
                    contentColor = AppTheme.colors.onSecondary,
                    disabledContentColor = AppTheme.colors.onSecondary,
                    disabledContainerColor = AppTheme.colors.onSecondary
                )
            ) {
                Text(
                    text = Constants.ACTIONS.EXIT,
                    textAlign = TextAlign.Center,
                    color = AppTheme.colors.onPrimary
                )
            }
        }
    }
}

@Composable
fun MessageSnackBar(sharedViewState: SharedViewState, modifier: Modifier) {
    val messages = sharedViewState.listMessage.collectAsState().value
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages) {
        scope.launch {
            messages.forEach { message ->
                delay(2000)
                sharedViewState.listMessage.value -= message
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1f), contentAlignment = Alignment.BottomCenter
    ) {
        LazyColumn(modifier = Modifier.height(150.dp), verticalArrangement = Arrangement.spacedBy(space = 5.dp, alignment = Alignment.Bottom)) {
            items(messages) { message ->
                Snackbar(
                    modifier = Modifier.padding(vertical = 4.dp),
                    action = {},
                    containerColor = AppTheme.colors.onPrimary
                ) {
                    when (message.type) {
                        TypeMessageModel.INFO -> Text(
                            text = Constants.ACTIONS.INFO + message.message,
                            color = AppTheme.colors.onSecondary,
                        )

                        TypeMessageModel.ERROR -> Text(
                            text = Constants.ACTIONS.ERROR + message.message,
                            color = AppTheme.colors.onSecondary,
                        )

                        TypeMessageModel.SUCCEED -> Text(
                            text = Constants.ACTIONS.SUCCEED + message.message,
                            color = AppTheme.colors.onSecondary,
                        )
                    }
                }
            }
        }
    }
}