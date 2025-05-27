package ru.krymer.delivery.ui.screens.courier.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.components.BanDialog
import ru.krymer.delivery.ui.components.CommonAddDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonUpdateDialog
import ru.krymer.delivery.ui.screens.courier.models.CourierEvent
import ru.krymer.delivery.ui.screens.courier.models.CourierViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun CourierView(
    event: (CourierEvent) -> Unit,
    popBackStack: () -> Unit,
    state: CourierViewState
) {
    val users = state.listUser.collectAsState().value

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
                        popBackStack()
                    })
                    .size(40.dp)
            )
            Row {
                Image(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "settings",
                    modifier = Modifier
                        .clickable(onClick = {
                            event(CourierEvent.ShowUpdateSettingsDialog)
                        })
                        .size(40.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "add user",
                    modifier = Modifier
                        .clickable(onClick = {
                            event(CourierEvent.ShowAddDialog)
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
            LazyColumn {
                items(users) { courier ->
                    CourierItem(
                        courier = courier,
                        onItemClicked = {
                            event(CourierEvent.UserItemClicked(it))
                        },
                        onItemDelete = {
                            event(CourierEvent.ShowDeleteDialog(it))
                        }
                    )
                    Spacer(modifier = Modifier.padding(bottom = 10.dp))
                }
            }
        }
    }

    if (state.isDeleteDialog) {
        state.userDelete?.let {
            CommonDeleteDialog(itemName = it.name, onConfirm = {
                event(CourierEvent.DeleteUser)
            }, onDismiss = {
                event(CourierEvent.DismissDeleteDialog)
            }, isVisible = true)
        }
    }

    if (state.showBanDialog) {
        state.userBan?.let {
            BanDialog(
                itemName = it.name,
                isVisible = true,
                onDismiss = { event(CourierEvent.DismissBanDialog) },
                onConfirm = { event(CourierEvent.BanUser) },
                isBanned = it.isBan
            )
        }
    }

    if (state.showAddSheetDialog) {
        CommonAddDialog(isVisible = true, onDismiss = {
            event(CourierEvent.DismissAddDialog)
        }, onConfirm = {
            event(CourierEvent.UserSaveAction)
        }, content = {
            BottomSheetDialogAddUser(viewState = state, changeName = {
                event(CourierEvent.ChangeUsername(it))
            }, changeEmail = {
                event(CourierEvent.ChangedEmailUser(it))
            }, changePass = {
                event(CourierEvent.ChangedPassUser(it))
            })
        })
    }

    if (state.showUpdateSheetDialog) {
        CommonUpdateDialog(isVisible = true, dismiss = {
            event(CourierEvent.DismissUpdateUserDataDialog)
        }, confirm = {
            event(CourierEvent.UserUpdateAction)
        }, content = {
            UpdateCourierView(
                state = state,
                event = event,
            )
        })
    }

    if (state.showUpdateSettingsSheetDialog) {
        CommonUpdateDialog(isVisible = true, dismiss = {
            event(CourierEvent.DismissUpdateSettingsDataDialog)
        }, confirm = {
            event(CourierEvent.SettingsUpdateAction)
        }, content = {
            UpdateSettingsView(
                viewState = state,
                changeSalary = {
                    event(CourierEvent.ChangedSalary(it))
                },
                changePriceMillage = {
                    event(CourierEvent.ChangedPrice(it))
                }
            )
        })
    }
}

@Composable
fun CourierItem(
    courier: UserModel,
    onItemClicked: (UserModel) -> Unit,
    onItemDelete: (UserModel) -> Unit,
) {
    Box(modifier = Modifier
        .clickable {
            onItemClicked(courier)
        }
        .background(
            color = colorResource(id = R.color.back), shape = RoundedCornerShape(16.dp)
        )
        .padding(15.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = courier.name,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically),
                color = AppTheme.colors.textColor
            )
            Spacer(modifier = Modifier.width(5.dp))
            Image(
                contentDescription = "ban courier",
                painter = painterResource(R.drawable.delete),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = { onItemDelete(courier) })
            )
        }
    }
}