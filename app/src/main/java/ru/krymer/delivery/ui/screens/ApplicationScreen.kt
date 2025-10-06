@file:OptIn(ExperimentalFoundationApi::class)

package ru.krymer.delivery.ui.screens

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.listenMany
import com.dokar.sonner.rememberToasterState
import ru.krymer.delivery.R
import ru.krymer.delivery.Screens
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.data.model.utilModel.MessageModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.ui.components.CommonInfoAlertDialog
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.request.RequestScreen
import ru.krymer.delivery.ui.screens.analitic.AnaliticScreen
import ru.krymer.delivery.ui.screens.client.ClientScreen
import ru.krymer.delivery.ui.screens.courier.CourierScreen
import ru.krymer.delivery.ui.screens.login.LoginScreen
import ru.krymer.delivery.ui.screens.main.MenuScreen
import ru.krymer.delivery.ui.screens.main.views.CustomButton
import ru.krymer.delivery.ui.screens.product.ProductScreen
import ru.krymer.delivery.ui.screens.route.RouteScreen
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

    val user = sharedState.value.user
    val isShowSettings = sharedState.value.isShowSettings
    val isShowPassChanger = sharedState.value.isShowPassChanger

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
                    }, user = user, event = event
                )
            }

            entry<Screens.Route> {
                RouteScreen(
                    user = user, openRoute = { route, routes ->
                        backStack.add(Screens.Client(route = route, routes = routes))
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
                ShopScreen(user = user, trip = key.trip, routeToRequest = { shop ->
                    backStack.add(Screens.Request(shop = shop))
                })
            }

            entry<Screens.Trip> {
                TripScreen(
                    openTrip = {
                        backStack.add(Screens.Shop(trip = it))
                    }, user = user
                )
            }

            entry<Screens.Client> { key ->
                ClientScreen(
                    route = key.route,
                    user = user,
                    routes = key.routes
                )
            }

            entry<Screens.Product> {
                ProductScreen()
            }

            entry<Screens.Courier> {
                CourierScreen()
            }

            entry<Screens.Ban> {
                UserBlocked(logout = {
                    backStack.removeLastOrNull()
                    backStack.add(Screens.Auth)
                    event(SharedEvents.LogOut)
                })
            }

            entry<Screens.Request> { key ->
                RequestScreen(
                    shop = key.shop, backStack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        })
    ToasterMessages(sharedState.value, event = event)

    if (isShowSettings) {
        CommonInfoAlertDialog(
            onDismissRequest = {
                event(SharedEvents.OpenHideSettingsApp)
            },
            content = {
                SettingsApp(
                    event = event, state = sharedState,
                    onRouteClick = {
                        backStack.add(Screens.Route)
                        event(SharedEvents.OpenHideSettingsApp)
                    },
                    user = user
                )
            },
            modifier = Modifier
        )
    }

    if (isShowPassChanger) {
        CommonInfoAlertDialog(
            onDismissRequest = { event(SharedEvents.OpenHideChangerPass) },
            content = {
                ChangerPass(event = event)
            }
        )
    }
}

@Composable
fun ChangerPass(event: (SharedEvents) -> Unit) {
    var newPass by remember { mutableStateOf("") }
    var oldPass by remember { mutableStateOf("") }
    val errorEmpty = stringResource(R.string.empty_input)
    val errorPass = stringResource(R.string.empty_input)

    var errorNewPass by remember { mutableStateOf(Error()) }
    var errorOldPass by remember { mutableStateOf(Error()) }

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonTextField(
            value = oldPass,
            placeholder = stringResource(
                id = R.string.pass_old
            ),
            changerText = { str ->
                oldPass = str
                errorOldPass = when {
                    str == "" -> Error(visible = true, error = errorEmpty)
                    str.length < 8 -> Error(visible = true, error = errorPass)
                    else -> {
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = errorOldPass.visible,
            errorValue = errorOldPass.error
        )

        CommonTextField(
            value = newPass,
            placeholder = stringResource(
                id = R.string.pass_new
            ),
            changerText = { str ->
                newPass = str
                errorNewPass = when {
                    str == "" -> Error(visible = true, error = errorEmpty)
                    str.length < 8 -> Error(visible = true, error = errorPass)
                    else -> {
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = errorNewPass.visible,
            errorValue = errorNewPass.error
        )

        Image(
            contentDescription = "submit",
            painter = painterResource(id = R.drawable.submit),
            modifier = Modifier
                .size(60.dp)
                .combinedClickable(onClick = {
                    if (!errorNewPass.visible && !errorOldPass.visible) {
                        event(SharedEvents.ChangePass(oldPass = oldPass, newPass = newPass))
                    }
                })
        )

    }
}

@Composable
fun SettingsApp(
    state: State<SharedViewState>,
    event: (SharedEvents) -> Unit,
    onRouteClick: () -> Unit,
    user: UserModel?
) {
    user?.let {
        val isVersion = state.value.lightVersion
        val context = LocalContext.current
        val versionName = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }        } catch (_: PackageManager.NameNotFoundException) {
            "Unknown"
        }
        val animatedValue by animateFloatAsState(
            targetValue = state.value.fontSizeIndex.toFloat(),
            animationSpec = tween(durationMillis = 300),
            label = "fontSizeSliderAnimation"
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    modifier = Modifier.size(60.dp),
                    checked = isVersion,
                    onCheckedChange = {
                        event(SharedEvents.ChangeSettings)
                    },
                    colors = CheckboxColors(
                        checkedCheckmarkColor = AppTheme.colors.onSecondary,
                        uncheckedCheckmarkColor = AppTheme.colors.secondary,
                        checkedBoxColor = Color.Transparent,
                        uncheckedBoxColor = Color.Transparent,
                        disabledCheckedBoxColor = Color.Transparent,
                        disabledUncheckedBoxColor = Color.Transparent,
                        disabledIndeterminateBoxColor = Color.Transparent,
                        checkedBorderColor = AppTheme.colors.onSecondary,
                        uncheckedBorderColor = AppTheme.colors.onSecondary,
                        disabledBorderColor = Color.Transparent,
                        disabledUncheckedBorderColor = Color.Transparent,
                        disabledIndeterminateBorderColor = Color.Transparent
                    )
                )
                Text(
                    text = if (isVersion) "Выключить режим" else "Включить режим",
                    color = AppTheme.colors.onSecondary,
                    style = AppTheme.typography.titleMedium
                )
            }
            Text(
                text = "Размер шрифта",
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onSecondary
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Slider(
                    value = animatedValue,
                    onValueChange = { newValue ->
                        event(SharedEvents.ChangeFontSizeIndex(newValue.toInt()))
                    },
                    valueRange = 0f..4f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(text = animatedValue.toString())
            }

            if (user.isMod()) {
                CustomButton(buttonName = stringResource(R.string.route), routeTo = onRouteClick)
            }

            CustomButton(buttonName = stringResource(R.string.change_pass_title), routeTo = {
                event(SharedEvents.OpenHideChangerPass)
            })

            Text(
                text = "Версия: $versionName",
                style = AppTheme.typography.titleSmall,
                color = AppTheme.colors.onSecondary
            )
        }
    }
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
                    text = stringResource(R.string.exit),
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
    val messages = sharedViewState.listMessage
    val toaster = rememberToasterState(
        onToastDismissed = { event(SharedEvents.DeleteMessage(it.id as Long)) },
    )

    LaunchedEffect(toaster, messages) {
        toaster.listenMany { messages.map(MessageModel::toToast) }
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