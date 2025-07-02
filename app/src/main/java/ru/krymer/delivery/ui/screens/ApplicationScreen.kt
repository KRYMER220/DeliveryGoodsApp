package ru.krymer.delivery.ui.screens

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.listenMany
import com.dokar.sonner.rememberToasterState
import ru.krymer.delivery.Screens
import ru.krymer.delivery.data.model.utilModel.MessageModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.ui.screens.analitic.AnaliticScreen
import ru.krymer.delivery.ui.screens.client.ClientScreen
import ru.krymer.delivery.ui.screens.courier.CourierScreen
import ru.krymer.delivery.ui.screens.login.LoginScreen
import ru.krymer.delivery.ui.screens.main.MenuScreen
import ru.krymer.delivery.ui.screens.product.ProductScreen
import ru.krymer.delivery.ui.screens.route.RouteScreen
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shared.models.SharedEvents
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState
import ru.krymer.delivery.ui.screens.shop.ShopScreen
import ru.krymer.delivery.ui.screens.splash.SplashScreen
import ru.krymer.delivery.ui.screens.trip.TripScreen
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants
import kotlin.time.Duration.Companion.milliseconds

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
    ToasterMessages(sharedState.value, event = event)
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

fun MessageModel.toToast(): Toast = when (this.type) {
    TypeMessageModel.INFO -> Toast(id = id, message = message, type = ToastType.Info, duration = 5000.milliseconds)
    TypeMessageModel.ERROR -> Toast(id = id, message = message, type = ToastType.Error, duration = 5000.milliseconds)
    TypeMessageModel.SUCCEED -> Toast(id = id, message = message, type = ToastType.Success, duration = 5000.milliseconds)
}

@Composable
fun ToasterMessages(sharedViewState: SharedViewState, event: (SharedEvents) -> Unit) {
    val messages = sharedViewState.listMessage.collectAsState()
    val toaster = rememberToasterState(
        onToastDismissed = { event(SharedEvents.DeleteMessage(it.id as Long)) },
    )

    val currentMessages by rememberUpdatedState(messages)

    LaunchedEffect(toaster, messages) {
        toaster.listenMany { currentMessages.value.map(MessageModel::toToast) }
    }

    Toaster(
        state = toaster,
        darkTheme = isSystemInDarkTheme(), contentColor = { toast ->
            when (toast.type) {
                ToastType.Info -> AppTheme.colors.onSecondary
                ToastType.Error -> AppTheme.colors.error
                ToastType.Success -> AppTheme.colors.success
                else -> AppTheme.colors.secondary
            }
        },
        maxVisibleToasts = 1
    )
}