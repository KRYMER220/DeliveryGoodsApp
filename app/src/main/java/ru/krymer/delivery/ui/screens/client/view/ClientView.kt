package ru.krymer.delivery.ui.screens.client.view

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.components.CommonAddDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonUpdateDialog
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.models.ClientViewState
import ru.krymer.delivery.ui.theme.AppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ClientView(
    event: (ClientEvent) -> Unit, state: ClientViewState, popBackStack: () -> Unit, user: UserModel
) {
    var isFirstLoad by remember { mutableStateOf(true) }
    val list = state.listClient.collectAsState().value
    var clients = remember { mutableStateListOf<ClientModel>() }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        clients = clients.apply {
            add(to.index, removeAt(from.index))
        }
        event(ClientEvent.ReorderClients(fromIndex = from.index, toIndex = to.index))
    }

    LaunchedEffect(list) {
        if (list.isNotEmpty() && isFirstLoad) {
            clients.clear()
            clients.addAll(list)
        }
    }

    LaunchedEffect(list.size) {
        clients.clear()
        clients.addAll(list)
    }

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
                        event(ClientEvent.ClientActionInvoked)
                        popBackStack()
                    })
                    .size(40.dp)
            )
            if (user.isModOrAdminOrSys()) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "add route",
                    modifier = Modifier
                        .clickable(onClick = {
                            event(ClientEvent.ShowAddDialog)
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
            LazyColumn(state = lazyListState, verticalArrangement = Arrangement.spacedBy(15.dp)) {
                items(clients, key = { client -> client.id }) { client ->
                    ReorderableItem(reorderableLazyListState, key = client.id) { isDragging ->
                        ClientItem(
                            modifier = Modifier.draggableHandle(), client = client, user = user, event = event
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            event(ClientEvent.ClientActionInvoked)
        }
    }

    if (state.isDialogDelete) {
        state.clientDelete?.let {
            CommonDeleteDialog(
                itemName = it.name,
                isVisible = true,
                onDismiss = { event(ClientEvent.DismissDeleteDialog) },
                onConfirm = {
                    clients - it
                    event(ClientEvent.DeleteClient)
                })
        }
    }

    if (state.isDialogAdd) {
        CommonAddDialog(isVisible = true, onDismiss = {
            event(ClientEvent.DismissAddDialog)
        }, onConfirm = {
            event(ClientEvent.ClientAddAction)
        }, content = {
            AddClientView(changeName = {
                event(ClientEvent.ChangeNameClient(name = it))
            }, changeCords = {
                event(ClientEvent.ChangeCordClient(cords = it))
            }, changePhone = {
                event(ClientEvent.ChangePhoneClient(phone = it))
            }, changeArrears = {
                event(ClientEvent.ChangeArrearsClient(arrears = it))
            })
        })
    }


    if (state.isDialogUpdate) {
        CommonUpdateDialog(isVisible = true, dismiss = {
            event(ClientEvent.DismissUpdateDialog)
        }, confirm = {
            event(ClientEvent.ClientUpdateAction)
        }, content = {
            UpdateClientView(
                viewState = state, event = event, user = user
            )
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClientItem(
    client: ClientModel, user: UserModel, event: (ClientEvent) -> Unit, modifier: Modifier
) {
    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    event(
                        ClientEvent.ShowUpdateDialog(
                            client = client
                        )
                    )
                })
            .background(
                color = AppTheme.colors.secondary, shape = RoundedCornerShape(16.dp)
            )
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = client.name,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically),
                color = AppTheme.colors.textColor
            )
            if (user.isModOrAdminOrSys()) {
                Image(contentDescription = "drop", painter = painterResource(id = R.drawable.list_item), modifier = modifier.size(40.dp))
                Image(
                    contentDescription = "delete client",
                    painter = painterResource(id = R.drawable.delete),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = {
                            event(
                                ClientEvent.ShowDeleteDialog(client = client)
                            )
                        })
                )
            }
        }
    }
}
