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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.Loader
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
    user: UserModel
) {
    val routes = state.routes
    val loader = when {
        state.isLoading -> Loader.LOADING
        state.routes.isEmpty() -> Loader.EMPTY
        else -> Loader.LOAD
    }

    Column(modifier = Modifier.padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier)
            if (user.isSysOrAdmin()) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "add route",
                    modifier = Modifier
                        .clickable(onClick = {
                            event(RouteEvent.ToggleAddDialog())
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
                                    if (user.isSysOrAdmin()) {
                                        event(RouteEvent.ToggleDeleteDialog(it))
                                    }
                                },
                                updateRoute = {
                                    if (user.isSysOrAdmin()) {
                                        event(RouteEvent.ToggleUpdateDialog(it))
                                    }
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

    if (state.toggleDialogDelete) {
        state.route?.let {
            CommonDeleteDialog(
                itemName = it.name,
                isVisible = true,
                onDismiss = { event(RouteEvent.ToggleDeleteDialog(null)) },
                onConfirm = { event(RouteEvent.DeleteRoute) })
        }
    }

    if (state.toggleDialogAdd) {
        CommonSaveDialog(dismiss = {
            event(RouteEvent.ToggleAddDialog())
        }, confirm = {
            event(RouteEvent.CreateRoute)
        }, content = {
            AddRouteView(changeName = {
                event(RouteEvent.ChangeAddName(it))
            })
        })
    }

    if (state.toggleDialogUpdate) {
        CommonSaveDialog(dismiss = {
            event(RouteEvent.ToggleUpdateDialog(null))
        }, confirm = {
            event(RouteEvent.UpdateRoute)
        }, content = {
            UpdateRouteView(viewState = state, changeName = {
                event(RouteEvent.ChangeUpdateName(it))
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