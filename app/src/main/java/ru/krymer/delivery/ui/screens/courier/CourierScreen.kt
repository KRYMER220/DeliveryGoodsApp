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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonAddDialog
import ru.krymer.delivery.ui.components.BanDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonUpdateDialog
import ru.krymer.delivery.ui.screens.courier.models.CourierEvent
import ru.krymer.delivery.ui.screens.courier.view.BottomSheetDialogAddUser
import ru.krymer.delivery.ui.screens.courier.view.UpdateCourierView
import ru.krymer.delivery.ui.screens.courier.view.UpdateSettingsView
import ru.krymer.delivery.ui.screens.courier.view.CourierView

@Composable
fun CourierScreen(
    navController: NavController, viewModel: CourierViewModel
) {
    val viewState = viewModel.viewState.collectAsState().value
    val users = viewState.listUser.collectAsState().value

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
        if (users.isEmpty()) {
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
            CourierView(chooseUser = {
                viewModel.obtainEvent(CourierEvent.UserItemClicked(it))
            }, banUser = {
                viewModel.obtainEvent(CourierEvent.ShowBanDialog(it))
            }, deleteUser = {
                viewModel.obtainEvent(CourierEvent.ShowDeleteDialog(it))
            }, couriers = users)
        }
    }

    if (viewState.isDeleteDialog) {
        viewState.userDelete?.let {
            CommonDeleteDialog(itemName = it.name , onConfirm = {
                viewModel.obtainEvent(CourierEvent.DeleteUser)
            }, onDismiss = {
                viewModel.obtainEvent(CourierEvent.DismissDeleteDialog)
            }, isVisible = true)
        }
    }

    if (viewState.showBanDialog) {
        viewState.userBan?.let {
            BanDialog(
                itemName = it.name,
                isVisible = true,
                onDismiss = { viewModel.obtainEvent(CourierEvent.DismissBanDialog) },
                onConfirm = { viewModel.obtainEvent(CourierEvent.BanUser) },
                isBanned = it.isBan
            )
        }
    }

    if (viewState.showAddSheetDialog) {
        CommonAddDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(CourierEvent.DismissAddDialog)
        }, onConfirm = {
            viewModel.obtainEvent(CourierEvent.UserSaveAction)
        }, content = {
            BottomSheetDialogAddUser(viewState = viewState, changeName = {
                viewModel.obtainEvent(CourierEvent.ChangeUsername(it))
            }, changeEmail = {
                viewModel.obtainEvent(CourierEvent.ChangedEmailUser(it))
            }, changePass = {
                viewModel.obtainEvent(CourierEvent.ChangedPassUser(it))
            })
        })
    }

    if (viewState.showUpdateSheetDialog) {
        CommonUpdateDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(CourierEvent.DismissUpdateUserDataDialog)
        }, onConfirm = {
            viewModel.obtainEvent(CourierEvent.UserUpdateAction)
        }, content = {
            UpdateCourierView(
                viewState = viewState,
                onPercentChange = {
                    viewModel.obtainEvent(CourierEvent.ChangeUserPercent(it))
                },
                onNameChange = {
                    viewModel.obtainEvent(CourierEvent.ChangeUsername(it))
                }, onSalaryChange = {
                    viewModel.obtainEvent(CourierEvent.ChangeUserSalary(it))
                }, viewModel = viewModel
            )
        })
    }

    if (viewState.showUpdateSettingsSheetDialog) {
        CommonUpdateDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(CourierEvent.DismissUpdateSettingsDataDialog)
        }, onConfirm = {
            viewModel.obtainEvent(CourierEvent.SettingsUpdateAction)
        }, content = {
            UpdateSettingsView(viewState = viewState,
                changeSalary = {
                    viewModel.obtainEvent(CourierEvent.ChangedSalary(it))
                },
                changePriceMillage = {
                    viewModel.obtainEvent(CourierEvent.ChangedPrice(it))
                }
            )
        })
    }
}


