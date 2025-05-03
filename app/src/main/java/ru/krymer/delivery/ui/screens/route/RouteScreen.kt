package ru.krymer.delivery.ui.screens.route

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import ru.krymer.delivery.ui.components.CommonAddDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonUpdateDialog
import ru.krymer.delivery.ui.navigation.NavigationTree
import ru.krymer.delivery.ui.screens.route.models.RouteAction
import ru.krymer.delivery.ui.screens.route.models.RouteEvent
import ru.krymer.delivery.ui.screens.route.views.AddRouteView
import ru.krymer.delivery.ui.screens.route.views.UpdateRouteView
import ru.krymer.delivery.ui.screens.route.views.RouteView
import ru.krymer.delivery.ui.screens.shared.SharedViewModel

@Composable
fun RouteScreen(
    navController: NavController, viewModel: RouteViewModel

) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState by viewModel.viewState.collectAsState()
    val routes = viewState.listRoute.collectAsState().value

    Column(modifier = Modifier.padding(15.dp)) {
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
                        viewModel.obtainEvent(RouteEvent.RouteActionInvoked)
                        navController.popBackStack()
                    })
                    .size(40.dp)
            )
            if (sharedViewModel.initSysAdm()) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "add route",
                    modifier = Modifier
                        .clickable(onClick = {
                            viewModel.obtainEvent(RouteEvent.ShowAddDialog)
                        })
                        .size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        if (routes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            RouteView(onItemClicked = {
                viewModel.obtainEvent(RouteEvent.RouteItemClickedToShop(route = it))
            }, onItemDelete = {
                viewModel.obtainEvent(
                    RouteEvent.ShowDeleteDialog(
                        route = it
                    )
                )
            }, onItemLongClicked = {
                viewModel.obtainEvent(RouteEvent.ShowUpdateDialog(it))
            }, sharedViewModel = sharedViewModel, routes = routes)
        }
    }

    if (viewState.isDialogDelete) {
        viewState.routeDeleted?.let {
            CommonDeleteDialog(
                itemName = it.name,
                isVisible = true,
                onDismiss = { viewModel.obtainEvent(RouteEvent.DismissDeleteDialog) },
                onConfirm = { viewModel.obtainEvent(RouteEvent.DeleteRoute) })
        }
    }

    if (viewState.showDialogAdd) {
        CommonAddDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(RouteEvent.DismissAddDialog)
        }, onConfirm = {
            viewModel.obtainEvent(RouteEvent.RouteSaveAction)
        }, content = {
            AddRouteView(viewState = viewState, changeName = {
                viewModel.obtainEvent(RouteEvent.NameRouteChangedAdd(it))
            })
        })
    }

    if (viewState.isDialogUpdate) {
        CommonUpdateDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(RouteEvent.DismissUpdateDialog)
        }, onConfirm = {
            viewModel.obtainEvent(RouteEvent.RouteUpdateAction)
        }, content = {
            UpdateRouteView(viewState = viewState, changeName = {
                viewModel.obtainEvent(RouteEvent.UpdateNameRoute(it))
            })
        })
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.obtainEvent(RouteEvent.RouteActionInvoked)
        }
    }

    LaunchedEffect(key1 = viewState.routeAction) {
        when (viewState.routeAction) {
            is RouteAction.OpenClients -> {
                navController.navigate(NavigationTree.Clients.name)
            }

            is RouteAction.None -> {}
        }
    }

}



