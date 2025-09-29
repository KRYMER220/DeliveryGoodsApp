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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.Loader
import ru.krymer.delivery.ui.components.BanDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonSaveDialog
import ru.krymer.delivery.ui.screens.courier.models.CourierEvent
import ru.krymer.delivery.ui.screens.courier.models.CourierViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun CourierView(
    event: (CourierEvent) -> Unit,
    state: CourierViewState
) {
    val users = state.couriers

    val loader = when {
        state.isLoading -> Loader.LOADING
        state.couriers.isEmpty() -> Loader.EMPTY
        else -> Loader.LOAD
    }

    Column(modifier = Modifier.padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.settings),
                contentDescription = "settings",
                modifier = Modifier
                    .clickable(onClick = {
                        event(CourierEvent.ToggleUpdateSettingsDialog)
                    })
                    .size(60.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "add user",
                modifier = Modifier
                    .clickable(onClick = {
                        event(CourierEvent.ToggleAddDialog)
                    })
                    .size(60.dp)
            )

        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (loader) {
                Loader.LOAD -> {
                    LazyColumn {
                        items(users) { courier ->
                            CourierItem(
                                courier = courier,
                                onItemClicked = {
                                    event(CourierEvent.ToggleUpdateDialog(it))
                                },
                                onItemDelete = {
                                    event(CourierEvent.ToggleDeleteDialog(it))
                                }
                            )
                            Spacer(modifier = Modifier.padding(bottom = 10.dp))
                        }
                    }
                }

                Loader.EMPTY -> {
                    Text(
                        text = stringResource(R.string.empty_data),
                        color = AppTheme.colors.onSecondary,
                        fontSize = 18.sp
                    )
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

    if (state.toggleDeleteCourier) {
        state.user?.let {
            CommonDeleteDialog(itemName = it.name, onConfirm = {
                event(CourierEvent.DeleteUser)
            }, onDismiss = {
                event(CourierEvent.ToggleDeleteDialog(null))
            }, isVisible = true)
        }
    }

    if (state.toggleBanDialog) {
        state.user?.let {
            BanDialog(
                itemName = it.name,
                onDismiss = { event(CourierEvent.ToggleBanUser(it)) },
                onConfirm = { event(CourierEvent.BanUser) },
                isBanned = it.isBan
            )
        }
    }

    if (state.toggleAddDialog) {
        CommonSaveDialog(dismiss = {
            event(CourierEvent.ToggleAddDialog)
        }, confirm = {
            event(CourierEvent.CreateUser)
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

    if (state.toggleUpdateCourier) {
        CommonSaveDialog(dismiss = {
            event(CourierEvent.ToggleUpdateDialog(null))
        }, confirm = {
            event(CourierEvent.UpdateUser)
        }, content = {
            UpdateCourierView(
                state = state,
                event = event,
            )
        })
    }

    if (state.toggleUpdateSettings) {
        CommonSaveDialog(dismiss = {
            event(CourierEvent.ToggleUpdateSettingsDialog)
        }, confirm = {
            event(CourierEvent.UpdateSettings)
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
                style = AppTheme.typography.titleMedium,
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
                contentDescription = "del courier",
                painter = painterResource(R.drawable.delete),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = { onItemDelete(courier) })
            )
        }
    }
}