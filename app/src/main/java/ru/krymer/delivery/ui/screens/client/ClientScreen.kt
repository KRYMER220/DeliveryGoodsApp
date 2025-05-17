package ru.krymer.delivery.ui.screens.client

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
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.view.AddClientView
import ru.krymer.delivery.ui.screens.client.view.UpdateClientView
import ru.krymer.delivery.ui.screens.client.view.ClientView
import ru.krymer.delivery.ui.screens.shared.SharedViewModel

@Composable
fun ClientShopScreen(
    viewModel: ClientViewModel,
    navController: NavController
) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState by viewModel.viewState.collectAsState()
    val clients = viewState.listClient.collectAsState().value

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
                        viewModel.obtainEvent(ClientEvent.ClientActionInvoked)
                        navController.popBackStack()
                    })
                    .size(40.dp)
            )
            if (sharedViewModel.initSysAdmMod()) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "add route",
                    modifier = Modifier
                        .clickable(onClick = {
                            viewModel.obtainEvent(ClientEvent.ShowAddDialog)
                        })
                        .size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        if (clients.isEmpty()) {
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
            ClientView(
                viewState = viewState, onItemClicked = {
                    val route = sharedViewModel.firstRouteToId(it.idRoute)
                    viewModel.obtainEvent(
                        ClientEvent.ShowUpdateDialog(
                            route = route, client = it
                        )
                    )
                }, onItemDelete = {
                    viewModel.obtainEvent(
                        ClientEvent.ShowDeleteDialog(client = it)
                    )
                }, viewModel = viewModel, sharedViewModel = sharedViewModel, clients = clients
            )
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.obtainEvent(ClientEvent.ClientActionInvoked)
        }
    }

    if (viewState.isDialogDelete) {
        viewState.clientDelete?.let {
            CommonDeleteDialog(
                itemName = it.name,
                isVisible = true,
                onDismiss = { viewModel.obtainEvent(ClientEvent.DismissDeleteDialog) },
                onConfirm = { viewModel.deleteItemConfirmed() })
        }
    }

    if (viewState.isDialogAdd) {
        CommonAddDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(ClientEvent.DismissAddDialog)
        }, onConfirm = {
            viewModel.obtainEvent(ClientEvent.ClientAddAction)
        }, content = {
            AddClientView(changeName = {
                viewModel.obtainEvent(ClientEvent.ChangeNameClient(name = it))
            }, changeCords = {
                viewModel.obtainEvent(ClientEvent.ChangeCordClient(cords = it))
            }, changePhone = {
                viewModel.obtainEvent(ClientEvent.ChangePhoneClient(phone = it))
            }, changeArrears = {
                viewModel.obtainEvent(ClientEvent.ChangeArrearsClient(arrears = it))
            })
        })
    }


    if (viewState.isDialogUpdate) {
        CommonUpdateDialog(isVisible = true, dismiss = {
            viewModel.obtainEvent(ClientEvent.DismissUpdateDialog)
        }, confirm = {
            viewModel.obtainEvent(ClientEvent.ClientUpdateAction)
        }, content = {
            UpdateClientView(
                viewState = viewState,
                viewModelClient = viewModel,
                changeCords = {
                    viewModel.obtainEvent(ClientEvent.ChangeCordClient(cords = it))
                },
                changePhone = {
                    viewModel.obtainEvent(ClientEvent.ChangePhoneClient(phone = it))
                },
                changeArrears = {
                    viewModel.obtainEvent(ClientEvent.ChangeArrearsClient(arrears = it))
                },
                changeName = {
                    viewModel.obtainEvent(ClientEvent.ChangeNameClient(name = it))
                },
                sharedViewModel = sharedViewModel
            )
        })
    }
}




