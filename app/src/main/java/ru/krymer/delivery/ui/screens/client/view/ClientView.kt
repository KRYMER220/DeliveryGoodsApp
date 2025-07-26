package ru.krymer.delivery.ui.screens.client.view

import android.util.Log
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.Loader
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonSaveDialog
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.models.ClientViewState
import ru.krymer.delivery.ui.theme.AppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ClientView(
    event: (ClientEvent) -> Unit, state: ClientViewState, popBackStack: () -> Unit, user: UserModel
) {
    val list = state.listClient.collectAsState().value
    val isLoad = state.isLoadData.collectAsState().value
    var loader by remember { mutableStateOf(Loader.LOADING) }
    var clients = remember { mutableStateListOf<ClientModel>() }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        clients = clients.apply {
            add(to.index, removeAt(from.index))
        }
    }

    LaunchedEffect(key1 = isLoad, key2 = list.size) {
        delay(500)
        loader = if (list.isNotEmpty()) {
            Loader.LOAD
        } else {
            Loader.EMPTY
        }
    }

    LaunchedEffect(isLoad) {
        clients.clear()
        clients.addAll(list)
    }

    Column(modifier = Modifier.padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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
            Text(
                style = AppTheme.typography.titleMedium,
                text = "${clients.sumOf { it.arrears.toInt() }} руб",
                fontSize = 20.sp,
                color = AppTheme.colors.textColor,
                textAlign = TextAlign.Center
            )
            if (user.isModOrAdminOrSys()) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "add route",
                    modifier = Modifier
                        .clickable(onClick = {
                            event(ClientEvent.ShowAddDialog)
                        })
                        .size(60.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when(loader) {
                Loader.LOAD -> {
                    LazyColumn(state = lazyListState, verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxSize()) {
                        items(clients, key = { client -> client.id }) { client ->
                            ReorderableItem(reorderableLazyListState, key = client.id) { isDragging ->
                                ClientItem(
                                    modifier = Modifier.draggableHandle(
                                        onDragStopped = {
                                            event(ClientEvent.ReorderClients(list = clients))
                                        }), client = client, user = user, event = event
                                )
                            }
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
        CommonSaveDialog(dismiss = {
            event(ClientEvent.DismissAddDialog)
        }, confirm = {
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
        CommonSaveDialog(dismiss = {
            event(ClientEvent.DismissUpdateDialog)
        }, confirm = {
            if (user.isModOrAdminOrSys()) {
                event(ClientEvent.ClientUpdateAction)
            }
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
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (user.isModOrAdminOrSys()) {
                Image(contentDescription = "drop", painter = painterResource(id = R.drawable.list_item), modifier = modifier.size(40.dp))
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    style = AppTheme.typography.titleMedium,
                    text = client.name,
                    fontSize = 20.sp,
                    color = AppTheme.colors.textColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    style = AppTheme.typography.titleMedium,
                    text = "${client.arrears.toInt()} руб",
                    fontSize = 16.sp,
                    color = AppTheme.colors.textColor,
                    textAlign = TextAlign.Center
                )
            }
            if (user.isModOrAdminOrSys()) {
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
