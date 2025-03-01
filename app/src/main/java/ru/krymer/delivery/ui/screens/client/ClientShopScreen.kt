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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.models.ClientShopViewState
import ru.krymer.delivery.ui.screens.client.view.ClientView
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.SignedNumberWithComma

@Composable
fun ClientShopScreen(
    viewModel: ClientViewModel,
    navController: NavController
) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState by viewModel.viewState.collectAsState()
    val sharedViewState by sharedViewModel.viewState.collectAsState()

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
        Spacer(modifier = Modifier.height(15.dp))
        if (!viewState.isLoadClientData) {
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
                        ClientEvent.ShowDeleteDialog(
                            itemId = it.id, itemName = it.name
                        )
                    )
                }, viewModel = viewModel, sharedViewModel = sharedViewModel
            )
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.obtainEvent(ClientEvent.ClientActionInvoked)
        }
    }

    if (viewState.isDialogDelete) {
        CommonShowDeleteDialog(
            itemName = viewState.itemNameToDelete,
            isVisible = true,
            onDismiss = { viewModel.obtainEvent(ClientEvent.DismissDeleteDialog) },
            onConfirm = { viewModel.deleteItemConfirmed() })
    }

    if (viewState.isDialogAdd) {
        CommonAddBottomSheetDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(ClientEvent.DismissAddDialog)
        }, onConfirm = {
            viewModel.obtainEvent(ClientEvent.ClientAddAction)
        }, content = {
            BottomSheetDialogAddClient(viewState = viewState, onNameChange = {
                viewModel.obtainEvent(ClientEvent.ChangeNameClient(name = it))
            }, onCordsChange = {
                viewModel.obtainEvent(ClientEvent.ChangeCordClient(cords = it))
            }, onPhoneChange = {
                viewModel.obtainEvent(ClientEvent.ChangePhoneClient(phone = it))
            }, onArrearsChange = {
                viewModel.obtainEvent(ClientEvent.ChangeArrearsClient(arrears = it))
            })
        })
    }


    if (viewState.isDialogUpdate) {
        CommonUpdateBottomSheetDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(ClientEvent.DismissUpdateDialog)
        }, onConfirm = {
            viewModel.obtainEvent(ClientEvent.ClientUpdateAction)
        }, content = {
            BottomSheetDialogUpdateClient(
                viewState = viewState,
                viewModelClient = viewModel,
                onCordsChange = {
                    viewModel.obtainEvent(ClientEvent.ChangeCordClient(cords = it))
                },
                onPhoneChange = {
                    viewModel.obtainEvent(ClientEvent.ChangePhoneClient(phone = it))
                },
                onArrearsChange = {
                    viewModel.obtainEvent(ClientEvent.ChangeArrearsClient(arrears = it))
                },
                onNameChange = {
                    viewModel.obtainEvent(ClientEvent.ChangeNameClient(name = it))
                },
                sharedViewModel = sharedViewModel, sharedViewState = sharedViewState
            )
        })
    }
}

@Composable
private fun BottomSheetDialogAddClient(
    viewState: ClientShopViewState,
    onNameChange: (String) -> Unit,
    onArrearsChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCordsChange: (String) -> Unit,
) {
    var nameState by remember { mutableStateOf("") }
    var arrearsState by remember { mutableStateOf("") }
    var phoneState by remember { mutableStateOf("") }
    var cordsState by remember { mutableStateOf("") }

    Column {
        CommonTextField(
            value = nameState,
            placeholder = stringResource(
                id = R.string.name
            ),
            onVC = { str ->
                nameState = str
                onNameChange(str)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = viewState.isErrorName,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = arrearsState,
            placeholder = stringResource(
                id = R.string.arrears
            ),
            onVC = { str ->
                arrearsState = str
                onArrearsChange(str)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = viewState.isErrorArrears,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = phoneState,
            placeholder = stringResource(
                id = R.string.phone
            ),
            onVC = { str ->
                phoneState = str
                onPhoneChange(str)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = viewState.isErrorPhone,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = cordsState,
            placeholder = stringResource(
                id = R.string.cords
            ),
            onVC = { str ->
                cordsState = str
                onCordsChange(str)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = SignedNumberWithComma(),
            isError = viewState.isErrorCords,
            errorValue = viewState.errorValue
        )
    }
}

@Composable
private fun BottomSheetDialogUpdateClient(
    viewState: ClientShopViewState,
    onNameChange: (String) -> Unit,
    onArrearsChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCordsChange: (String) -> Unit,
    viewModelClient: ClientViewModel,
    sharedViewModel: SharedViewModel,
    sharedViewState: SharedViewState
) {
    var nameState by remember { mutableStateOf(viewState.name) }
    var arrearsState by remember { mutableStateOf(viewState.arrears) }
    var phoneState by remember { mutableStateOf(viewState.phone) }
    var cordsState by remember { mutableStateOf(viewState.cords) }

    Column {
        CommonTextField(
            value = nameState,
            placeholder = stringResource(
                id = R.string.name
            ),
            onVC = { str ->
                nameState = str
                onNameChange(str)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = viewState.isErrorName,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = arrearsState ?: "",
            placeholder = stringResource(
                id = R.string.arrears
            ),
            onVC = { str ->
                arrearsState = str
                onArrearsChange(str)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = viewState.isErrorArrears,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = phoneState ?: "",
            placeholder = stringResource(
                id = R.string.phone
            ),
            onVC = { str ->
                phoneState = str
                onPhoneChange(str)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = viewState.isErrorPhone,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = cordsState ?: "",
            placeholder = stringResource(
                id = R.string.cords
            ),
            onVC = { str ->
                cordsState = str
                onCordsChange(str)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = SignedNumberWithComma(),
            isError = viewState.isErrorCords,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (sharedViewModel.initSysAdmMod()) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            viewModelClient.obtainEvent(ClientEvent.DropDownMenuState(true))
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewState.selectedRoute!!.name,
                        modifier = Modifier.padding(start = 15.dp),
                        color = AppTheme.colors.onSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    DropdownMenu(expanded = viewState.dropDownState, onDismissRequest = {
                        viewModelClient.obtainEvent(ClientEvent.DropDownMenuState(false))
                    }) {
                        val list = viewState.listRoute.collectAsState().value
                        list.forEach {
                            DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                viewModelClient.obtainEvent(
                                    ClientEvent.SelectedItemMenu(
                                        it
                                    )
                                )
                                viewModelClient.obtainEvent(
                                    ClientEvent.DropDownMenuState(
                                        false
                                    )
                                )
                            })
                        }
                    }

                }
            }
        }
    }
}
