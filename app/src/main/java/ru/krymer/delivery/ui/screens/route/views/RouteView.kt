package ru.krymer.delivery.ui.screens.route.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.Loader
import ru.krymer.delivery.ui.components.CommonAddDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonSaveDialog
import ru.krymer.delivery.ui.screens.route.models.RouteEvent
import ru.krymer.delivery.ui.screens.route.models.RouteViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun RouteView(
    event: (RouteEvent) -> Unit,
    state: RouteViewState,
    openRoute: (RouteModel, List<RouteModel>) -> Unit,
    popBackStack: () -> Unit,
    user: UserModel
) {

    val routes = state.listRoute.collectAsState().value
    var loader by remember { mutableStateOf(Loader.LOADING) }

    LaunchedEffect(key1 = state.isLoadingData, key2 = routes.size) {
        delay(500)
        loader = if (routes.isNotEmpty()) {
            Loader.LOAD
        } else {
            Loader.EMPTY
        }
    }

    Column(modifier = Modifier.padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_stack),
                contentDescription = "exit",
                modifier = Modifier
                    .clickable(onClick = {
                        popBackStack()
                    })
                    .size(60.dp)
            )
            if (user.isSysOrAdmin()) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "add route",
                    modifier = Modifier
                        .clickable(onClick = {
                            event(RouteEvent.ShowAddDialog)
                        })
                        .size(60.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when(loader) {
                Loader.LOAD -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxSize()) {
                        items(routes) { route ->
                            RouteItem(
                                route = route,
                                openRoute = {
                                    openRoute(it, routes)
                                },
                                deleteRoute = {
                                    if (user.isSysOrAdmin())
                                        event(
                                            RouteEvent.ShowDeleteDialog(
                                                route = it
                                            )
                                        )
                                },
                                updateRoute = {
                                    if (user.isSysOrAdmin())
                                        event(RouteEvent.ShowUpdateDialog(it))
                                },
                                user = user
                            )
                        }
                    }
                }
                Loader.EMPTY -> {
                    Text(text = stringResource(R.string.empty_data), color = AppTheme.colors.onSecondary, fontSize = 18.sp)
                }
                Loader.LOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(60.dp),
                        strokeWidth = 2.dp,
                        color = AppTheme.colors.onSecondary
                    )
                }
            }
        }
    }

    if (state.isDialogDelete) {
        state.routeDeleted?.let {
            CommonDeleteDialog(
                itemName = it.name,
                isVisible = true,
                onDismiss = { event(RouteEvent.DismissDeleteDialog) },
                onConfirm = { event(RouteEvent.DeleteRoute) })
        }
    }

    if (state.showDialogAdd) {
        CommonSaveDialog(dismiss = {
            event(RouteEvent.DismissAddDialog)
        }, confirm = {
            event(RouteEvent.RouteSaveAction)
        }, content = {
            AddRouteView(viewState = state, changeName = {
                event(RouteEvent.NameRouteChangedAdd(it))
            })
        })
    }

    if (state.isDialogUpdate) {
        CommonSaveDialog(dismiss = {
            event(RouteEvent.DismissUpdateDialog)
        }, confirm = {
            event(RouteEvent.RouteUpdateAction)
        }, content = {
            UpdateRouteView(viewState = state, changeName = {
                event(RouteEvent.UpdateNameRoute(it))
            })
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RouteItem(
    route: RouteModel,
    openRoute: (RouteModel) -> Unit,
    updateRoute: (RouteModel) -> Unit,
    deleteRoute: (RouteModel) -> Unit,
    user: UserModel
) {
    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = { openRoute(route) },
                onLongClick = { updateRoute(route) })
            .background(
                color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
            )
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                style = AppTheme.typography.titleMedium,
                text = route.name,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                color = AppTheme.colors.textColor
            )
            if (user.isSysOrAdmin()) {
                Image(
                    contentDescription = "delete route",
                    painter = painterResource(id = R.drawable.delete),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = { deleteRoute(route) })
                )
            }
        }
    }
}