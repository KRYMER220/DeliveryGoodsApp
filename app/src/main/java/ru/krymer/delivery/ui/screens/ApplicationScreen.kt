package ru.krymer.delivery.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.ui.navigation.NavigationTree
import ru.krymer.delivery.ui.screens.analitic.AnaliticScreen
import ru.krymer.delivery.ui.screens.analitic.AnaliticViewModel
import ru.krymer.delivery.ui.screens.client.ClientShopScreen
import ru.krymer.delivery.ui.screens.courier.CourierScreen
import ru.krymer.delivery.ui.screens.login.LoginScreen
import ru.krymer.delivery.ui.screens.main.MenuScreen
import ru.krymer.delivery.ui.screens.product.ProductScreen
import ru.krymer.delivery.ui.screens.route.RouteScreen
import ru.krymer.delivery.ui.screens.shared.models.AuthAction
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
    navController: NavHostController, sharedState: SharedViewState, event: (SharedEvents) -> Unit
) {

    val isUserBlocked by remember(sharedState.isUserBlocked) {
        derivedStateOf { sharedState.isUserBlocked }
    }
    if (isUserBlocked) {
        UserBlocked(event = event)
    } else {

        LaunchedEffect(sharedState.authAction) {

            when (sharedState.authAction) {
                AuthAction.Authorized -> {
                    navigateToTap(
                        navController = navController,
                        NavigationTree.Main.name
                    )
                }

                AuthAction.Unauthorized -> {
                    navigateToTap(
                        navController = navController,
                        NavigationTree.Login.name
                    )
                }
                AuthAction.None -> {}
            }
        }

        MessageSnackBar(sharedState)

        NavHost(
            navController = navController,
            startDestination = NavigationTree.Splash.name, enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(0)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(0)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(0)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(0)
                )
            }
            ) {

            composable(NavigationTree.Splash.name) { SplashScreen() }

            composable(NavigationTree.Login.name) { LoginScreen() }


            composable(NavigationTree.Main.name) {
                val user = sharedState.user.collectAsState().value
                MenuScreen(
                    navigateTo = {
                        navigateToTap(
                            navController = navController,
                            route = it
                        )
                    }, user = user
                )
            }
            composable(NavigationTree.Route.name) {
                val user = sharedState.user.collectAsState().value
                RouteScreen(
                    user = user,
                    navigateTo = {
                        navigateToTap(navController = navController, route = it)
                    },
                    popBackStack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(NavigationTree.Clients.name) {
                val user = sharedState.user.collectAsState().value
                ClientShopScreen(
                    user = user,
                    popBackStack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(NavigationTree.Product.name) {
                ProductScreen(
                    popBackStack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(NavigationTree.Courier.name) {
                CourierScreen(
                    popBackStack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(NavigationTree.Trip.name) {
                val user = sharedState.user.collectAsState().value
                TripScreen(
                    navigateTo = {
                        navigateToTap(
                            navController = navController,
                            route = it
                        )
                    },
                    popBackStack = { navController.popBackStack() },
                    user = user
                )
            }

            composable(NavigationTree.Shop.name) {
                val user = sharedState.user.collectAsState().value
                ShopScreen(
                    popBackStack = { navController.popBackStack() },
                    user = user
                )
            }

            composable(NavigationTree.Analitic.name) {
                AnaliticScreen(
                    popBackStack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun UserBlocked(event: (SharedEvents) -> Unit) {
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
                onClick = { event(SharedEvents.ClearToken) },
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
fun MessageSnackBar(sharedViewState: SharedViewState) {
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
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .zIndex(1f), contentAlignment = Alignment.BottomCenter
    ) {
        LazyColumn(modifier = Modifier.height(150.dp), verticalArrangement = Arrangement.Bottom) {
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

fun navigateToTap(navController: NavController, route: String) {
    val startDestination = navController.graph.findStartDestination()
    val isStartDestinationSplash = startDestination.route == NavigationTree.Splash.name

    navController.navigate(route) {
        popUpTo(startDestination.id) {
            saveState = true
            inclusive = isStartDestinationSplash
        }
        restoreState = true
        launchSingleTop = true
    }
}