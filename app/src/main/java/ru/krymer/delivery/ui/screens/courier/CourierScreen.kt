package ru.krymer.delivery.ui.screens.courier

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.user.RoleModel
import ru.krymer.delivery.ui.components.CommonAddBottomSheetDialog
import ru.krymer.delivery.ui.components.CommonShowBanDialog
import ru.krymer.delivery.ui.components.CommonShowDeleteDialog
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.components.CommonUpdateBottomSheetDialog
import ru.krymer.delivery.ui.screens.courier.models.CourierEvent
import ru.krymer.delivery.ui.screens.courier.models.CourierViewState
import ru.krymer.delivery.ui.screens.courier.view.CourierView
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants

@Composable
fun CourierScreen(
    navController: NavController, viewModel: CourierViewModel
) {
    val viewState = viewModel.viewState.collectAsState().value
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
                        navController.popBackStack()
                    })
                    .size(40.dp)
            )
            Row {
                Image(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "settings",
                    modifier = Modifier
                        .clickable(onClick = {
                            viewModel.obtainEvent(CourierEvent.ShowUpdateSettingsDialog)
                        })
                        .size(40.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "add user",
                    modifier = Modifier
                        .clickable(onClick = {
                            viewModel.obtainEvent(CourierEvent.ShowAddDialog)
                        })
                        .size(40.dp)
                )
            }

        }
        Spacer(modifier = Modifier.height(15.dp))
        if (!viewState.isLoadUserData) {
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
            CourierView(viewState = viewState, onItemClicked = {
                viewModel.obtainEvent(CourierEvent.UserItemClicked(it))
            }, onItemBan = {
                viewModel.obtainEvent(CourierEvent.ShowBanDialog(it))
            }, onItemDelete = {
                viewModel.obtainEvent(CourierEvent.ShowDeleteDialog(it))
            })
        }
    }

    if (viewState.isDeleteDialog) {
        CommonShowDeleteDialog(itemName = viewState.userDelete!!.name, onConfirm = {
            viewModel.obtainEvent(CourierEvent.DeleteUser)
        }, onDismiss = {
            viewModel.obtainEvent(CourierEvent.DismissDeleteDialog)
        }, isVisible = true)
    }

    if (viewState.showBanDialog) {
        val user = viewState.userBan
        if (user != null) {
            CommonShowBanDialog(
                itemName = user.name,
                isVisible = true,
                onDismiss = { viewModel.obtainEvent(CourierEvent.DismissBanDialog) },
                onConfirm = { viewModel.obtainEvent(CourierEvent.BanUser) },
                isBanned = user.isBanned
            )
        }
    }

    if (viewState.showAddSheetDialog) {
        CommonAddBottomSheetDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(CourierEvent.DismissAddDialog)
        }, onConfirm = {
            viewModel.obtainEvent(CourierEvent.UserSaveAction)
        }, content = {
            BottomSheetDialogAddUser(viewState = viewState, onValueNameChange = {
                viewModel.obtainEvent(CourierEvent.ChangedNameUser(it))
            }, onValueEmailChange = {
                viewModel.obtainEvent(CourierEvent.ChangedEmailUser(it))
            }, onValuePassChange = {
                viewModel.obtainEvent(CourierEvent.ChangedPassUser(it))
            })
        })
    }

    if (viewState.showUpdateSheetDialog) {
        CommonUpdateBottomSheetDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(CourierEvent.DismissUpdateUserDataDialog)
        }, onConfirm = {
            viewModel.obtainEvent(CourierEvent.UserUpdateAction)
        }, content = {
            BottomSheetDialogUpdateCourier(
                viewState = viewState,
                onPercentChange = {
                    viewModel.obtainEvent(CourierEvent.ChangedPercentUser(it))
                },
                onNameChange = {
                    viewModel.obtainEvent(CourierEvent.ChangedNameUser(it))
                }, viewModel = viewModel
            )
        })
    }

    if (viewState.showUpdateSettingsSheetDialog) {
        CommonUpdateBottomSheetDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(CourierEvent.DismissUpdateSettingsDataDialog)
        }, onConfirm = {
            viewModel.obtainEvent(CourierEvent.SettingsUpdateAction)
        }, content = {
            BottomSheetDialogUpdateSettings(viewState = viewState,
                onSalaryChange = {
                    viewModel.obtainEvent(CourierEvent.ChangedSalary(it))
                },
                onPriceChange = {
                    viewModel.obtainEvent(CourierEvent.ChangedPrice(it))
                }
            )
        })
    }
}

@Composable
private fun BottomSheetDialogUpdateCourier(
    viewState: CourierViewState,
    onNameChange: (String) -> Unit,
    onPercentChange: (String) -> Unit, viewModel: CourierViewModel
) {
    Column {
        CommonTextField(
            value = viewState.userName ?: Constants.EMPTY.EMPTY_STRING,
            placeholder = stringResource(
                id = R.string.name
            ),
            onVC = onNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = viewState.isErrorName,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = viewState.userPercent ?: Constants.EMPTY.EMPTY_STRING,
            placeholder = stringResource(
                id = R.string.arrears
            ),
            onVC = onPercentChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = viewState.isErrorPercent,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))
        val list = RoleModel.entries.toList()
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clickable {
                        viewModel.obtainEvent(CourierEvent.DropDownMenuState(true))
                    }, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewState.userRole!!.name,
                    modifier = Modifier.padding(start = 15.dp),
                    color = AppTheme.colors.onSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 15.dp)
                )
                DropdownMenu(expanded = viewState.stateDropMenu, onDismissRequest = {
                    viewModel.obtainEvent(CourierEvent.DropDownMenuState(false))
                }) {

                    list.forEach {
                        DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                            viewModel.obtainEvent(
                                CourierEvent.SelectedItemMenu(
                                    it
                                )
                            )
                            viewModel.obtainEvent(
                                CourierEvent.DropDownMenuState(
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

@Composable
private fun BottomSheetDialogUpdateSettings(
    viewState: CourierViewState,
    onSalaryChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
) {
    Column {
        CommonTextField(
            value = viewState.salaryChange ?: Constants.EMPTY.EMPTY_STRING,
            placeholder = stringResource(
                id = R.string.salary
            ),
            onVC = onSalaryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = viewState.isErrorSalary,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = viewState.priceChange ?: Constants.EMPTY.EMPTY_STRING,
            placeholder = stringResource(
                id = R.string.km_price
            ),
            onVC = onPriceChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = viewState.isErrorPrice,
            errorValue = viewState.errorValue
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun BottomSheetDialogAddUser(
    viewState: CourierViewState,
    onValueNameChange: (String) -> Unit,
    onValueEmailChange: (String) -> Unit,
    onValuePassChange: (String) -> Unit
) {
    Column {
        CommonTextField(
            isError = viewState.isErrorName,
            errorValue = viewState.errorNameValue,
            value = viewState.userName ?: Constants.EMPTY.EMPTY_STRING,
            placeholder = stringResource(
                id = R.string.name_user
            ),
            onVC = onValueNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)

        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            isError = viewState.isErrorEmail,
            errorValue = viewState.errorEmailValue,
            value = viewState.userEmail ?: Constants.EMPTY.EMPTY_STRING,
            placeholder = stringResource(
                id = R.string.email_hint
            ),
            onVC = onValueEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            isError = viewState.isErrorPass,
            errorValue = viewState.errorPassValue,
            value = viewState.userPass ?: Constants.EMPTY.EMPTY_STRING,
            placeholder = stringResource(
                id = R.string.pass_hint
            ),
            onVC = onValuePassChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
    }
}