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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonAddBottomSheetDialog
import ru.krymer.delivery.ui.components.CommonShowDeleteDialog
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.components.CommonUpdateBottomSheetDialog
import ru.krymer.delivery.ui.navigation.NavigationTree
import ru.krymer.delivery.ui.screens.route.models.RouteAction
import ru.krymer.delivery.ui.screens.route.models.RouteEvent
import ru.krymer.delivery.ui.screens.route.models.RouteViewState
import ru.krymer.delivery.ui.screens.route.views.RouteView
import ru.krymer.delivery.ui.screens.shared.SharedViewModel

@Composable
fun RouteScreen(
    navController: NavController, viewModel: RouteViewModel

) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState by viewModel.viewState.collectAsState()

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
        if (!viewState.isLoadRouteData) {
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
            RouteView(viewState = viewState, onItemClicked = {
                viewModel.obtainEvent(RouteEvent.RouteItemClickedToShop(route = it))
            }, onItemDelete = {
                viewModel.obtainEvent(
                    RouteEvent.ShowDeleteDialog(
                        itemID = it.id, itemName = it.name
                    )
                )
            }, onItemLongClicked = {
                viewModel.obtainEvent(RouteEvent.RouteItemLongClicked(it))
            }, sharedViewModel = sharedViewModel)

        }
    }

    if (viewState.isDialogDelete) {
        CommonShowDeleteDialog(itemName = viewState.itemNameDelete!!,
            isVisible = true,
            onDismiss = { viewModel.obtainEvent(RouteEvent.DismissDeleteDialog) },
            onConfirm = { viewModel.obtainEvent(RouteEvent.DeleteRoute) })
    }

    if (viewState.showDialogAdd) {
        CommonAddBottomSheetDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(RouteEvent.DismissAddDialog)
        }, onConfirm = {
            viewModel.obtainEvent(RouteEvent.RouteSaveAction)
        }, content = {
            BottomSheetDialogAddRoute(viewState = viewState, onValueChange = {
                viewModel.obtainEvent(RouteEvent.NameRouteChangedAdd(it))
            })
        })
    }

    if (viewState.isDialogUpdate) {
        CommonUpdateBottomSheetDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(RouteEvent.DismissUpdateDialog)
        }, onConfirm = {
            viewModel.obtainEvent(RouteEvent.RouteUpdateAction)
        }, content = {
            BottomSheetDialogUpdateRoute(viewState = viewState, onValueChange = {
                viewModel.obtainEvent(RouteEvent.NameRouteChangedUpdate(it))
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


@Composable
private fun BottomSheetDialogAddRoute(
    viewState: RouteViewState, onValueChange: (String) -> Unit
) {
    var nameState by remember { mutableStateOf("") }
    Column {
        CommonTextField(
            value = nameState,
            placeholder = stringResource(
                id = R.string.name
            ),
            onVC = { str ->
                nameState = str
                onValueChange(str)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = viewState.isErrorName,
            errorValue = viewState.errorName ?: ""
        )
    }
}

@Composable
private fun BottomSheetDialogUpdateRoute(
    viewState: RouteViewState, onValueChange: (String) -> Unit
) {
    var nameState by remember { mutableStateOf(viewState.itemNameUpdate) }

    Column {
        CommonTextField(
            value = nameState,
            placeholder = stringResource(
                id = R.string.name
            ),
            onVC = { str ->
                nameState = str
                onValueChange(str)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = viewState.isErrorName,
            errorValue = viewState.errorName ?: ""
        )
    }
}



