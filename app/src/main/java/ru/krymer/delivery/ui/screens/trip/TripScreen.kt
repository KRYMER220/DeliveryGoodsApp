package ru.krymer.delivery.ui.screens.trip

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonAlertAddDialog
import ru.krymer.delivery.ui.components.CommonShowDeleteDialog
import ru.krymer.delivery.ui.navigation.NavigationTree
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.trip.models.TripAction
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.ui.screens.trip.views.TripView
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripScreen(
    viewModel: TripViewModel, navController: NavController
) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState = viewModel.viewState.collectAsState().value
    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.obtainEvent(TripEvent.TripActionInvoked)
        }
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
                        viewModel.obtainEvent(TripEvent.TripActionInvoked)
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
                            viewModel.obtainEvent(TripEvent.ShowAddDialog)
                        })
                        .size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        if (!viewState.isLoadDataTrip) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        } else {
            TripView(viewState = viewState, onItemLongClicked = {
                viewModel.obtainEvent(TripEvent.ShowChangeCourierDialog(it))
            }, onItemDelete = {
                viewModel.obtainEvent(
                    TripEvent.ShowDeleteDialog(
                        itemID = it.id, itemName = it.nameRoute
                    )
                )
            }, onItemClick = {
                viewModel.obtainEvent(TripEvent.TripItemClicked(it))
            }, sharedViewModel = sharedViewModel)

        }
    }

    if (viewState.showDeleteDialog) {
        CommonShowDeleteDialog(itemName = viewState.itemNameToDelete,
            isVisible = true,
            onDismiss = { viewModel.obtainEvent(TripEvent.DismissDeleteDialog) },
            onConfirm = { viewModel.obtainEvent(TripEvent.DeleteTrip) })
    }



    if (viewState.showAddSheetDialog) {
        CommonAlertAddDialog(
            onConfirmation = { viewModel.obtainEvent(TripEvent.TripSaveAction) },
            onDismissRequest = {
                viewModel.obtainEvent(TripEvent.DismissAddDialog)
            },
            onConfirm = {},
            content = {
                BottomSheetDialogAddTrip(
                    viewState = viewState, viewModel = viewModel
                )
            })
    }

    if (viewState.showUpdateSheetDialog) {
        CommonAlertAddDialog(
            onConfirmation = { viewModel.obtainEvent(TripEvent.TripUpdateAction) },
            onDismissRequest = {
                viewModel.obtainEvent(TripEvent.DismissUpdateDialog)
            },
            onConfirm = {},
            content = {
                BottomSheetDialogUpdateTrip(
                    viewState = viewState, viewModel = viewModel
                )
            })
    }

    LaunchedEffect(key1 = viewState.tripAction) {
        when (viewState.tripAction) {
            TripAction.None -> {}
            is TripAction.OpenShops -> {
                navController.navigate(NavigationTree.Shop.name)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetDialogUpdateTrip(
    viewState: TripViewState, viewModel: TripViewModel
) {
    if (viewState.isLoadDataDropMenuCourier && viewState.isLoadDataDropMenuRoute) {
        Column {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(60.dp)
                .background(
                    color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                    viewModel.obtainEvent(TripEvent.ChangeDropDownStateDatePicker(true))
                }) {
                Text(
                    text = convertToTextDate(viewState.currentDate),
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
                if (viewState.dropDownStateDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(onDismissRequest = {
                        viewModel.obtainEvent(
                            TripEvent.ChangeDropDownStateDatePicker(
                                false
                            )
                        )
                    }, confirmButton = {
                        TextButton(onClick = {
                            viewModel.obtainEvent(TripEvent.ChangeDate(datePickerState.selectedDateMillis!!))
                            viewModel.obtainEvent(
                                TripEvent.ChangeDropDownStateDatePicker(
                                    false
                                )
                            )
                        }) {
                            Text(stringResource(id = R.string.ok))
                        }

                    }, dismissButton = {
                        TextButton(onClick = {
                            viewModel.obtainEvent(
                                TripEvent.ChangeDropDownStateDatePicker(
                                    false
                                )
                            )
                        }) {
                            Text(stringResource(id = R.string.close))
                        }
                    }) {
                        DatePicker(state = datePickerState)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(
                        color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            viewModel.obtainEvent(TripEvent.ChangeDropDownStateTrip(true))
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewState.currentRoute!!.name,
                        modifier = Modifier.padding(start = 15.dp),
                        color = AppTheme.colors.onSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    DropdownMenu(expanded = viewState.dropDownStateTrips, onDismissRequest = {
                        viewModel.obtainEvent(TripEvent.ChangeDropDownStateTrip(false))
                    }) {
                        val list = viewState.listRoute.collectAsState().value
                        list.forEach {
                            DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                viewModel.obtainEvent(
                                    TripEvent.SelectDropDownRoute(it)
                                )
                                viewModel.obtainEvent(TripEvent.ChangeDropDownStateTrip(false))
                            })
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(
                        color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            viewModel.obtainEvent(TripEvent.ChangeDropDownStateCourier(true))
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewState.currentCourier!!.name,
                        modifier = Modifier.padding(start = 15.dp),
                        color = AppTheme.colors.onSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    DropdownMenu(expanded = viewState.dropDownStateCourier,
                        onDismissRequest = {
                            viewModel.obtainEvent(TripEvent.ChangeDropDownStateCourier(false))
                        }) {
                        val list = viewState.listCourier.collectAsState().value
                        list.forEach {
                            DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                viewModel.obtainEvent(
                                    TripEvent.SelectDropDownCourier(it)
                                )
                                viewModel.obtainEvent(TripEvent.ChangeDropDownStateCourier(false))
                            })
                        }
                    }
                }
            }

        }
    } else {
        Box(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center),
                strokeWidth = 2.dp,
                color = AppTheme.colors.onSecondary
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetDialogAddTrip(
    viewState: TripViewState, viewModel: TripViewModel
) {
    Column {

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .height(60.dp)
            .background(
                color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
            )
            .clickable {
                viewModel.obtainEvent(TripEvent.ChangeDropDownStateDatePicker(true))
            }) {
            Text(
                text = convertToTextDate(viewState.currentDate),
                modifier = Modifier
                    .padding(start = 15.dp)
                    .align(Alignment.Center),
                color = AppTheme.colors.onSecondary
            )

            if (viewState.dropDownStateDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(onDismissRequest = {
                    viewModel.obtainEvent(
                        TripEvent.ChangeDropDownStateDatePicker(
                            false
                        )
                    )
                }, confirmButton = {
                    TextButton(onClick = {
                        if (datePickerState.selectedDateMillis != null) {
                            viewModel.obtainEvent(TripEvent.ChangeDate(datePickerState.selectedDateMillis!!))
                            viewModel.obtainEvent(
                                TripEvent.ChangeDropDownStateDatePicker(
                                    false
                                )
                            )
                        }
                    }) {
                        Text(stringResource(id = R.string.ok))
                    }
                }, dismissButton = {
                    TextButton(onClick = {
                        viewModel.obtainEvent(
                            TripEvent.ChangeDropDownStateDatePicker(
                                false
                            )
                        )
                    }) {
                        Text(stringResource(id = R.string.close))
                    }
                }) {
                    DatePicker(state = datePickerState)
                }
            }
        }
        if (viewState.isLoadDataDropMenuRoute) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(
                        color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            viewModel.obtainEvent(TripEvent.ChangeDropDownStateTrip(true))
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewState.currentRoute!!.name,
                        modifier = Modifier.padding(start = 15.dp),
                        color = AppTheme.colors.onSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    DropdownMenu(expanded = viewState.dropDownStateTrips, onDismissRequest = {
                        viewModel.obtainEvent(TripEvent.ChangeDropDownStateTrip(false))
                    }) {
                        val list = viewState.listRoute.collectAsState().value
                        list.forEach {
                            DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                viewModel.obtainEvent(
                                    TripEvent.SelectDropDownRoute(it)
                                )
                                viewModel.obtainEvent(TripEvent.ChangeDropDownStateTrip(false))
                            })
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(
                        color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                    )
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        }
        if (viewState.isLoadDataDropMenuCourier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(
                        color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            viewModel.obtainEvent(TripEvent.ChangeDropDownStateCourier(true))
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewState.currentCourier!!.name,
                        modifier = Modifier.padding(start = 15.dp),
                        color = AppTheme.colors.onSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    DropdownMenu(expanded = viewState.dropDownStateCourier,
                        onDismissRequest = {
                            viewModel.obtainEvent(TripEvent.ChangeDropDownStateCourier(false))
                        }) {
                        val list = viewState.listCourier.collectAsState().value
                        list.forEach {
                            DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                viewModel.obtainEvent(
                                    TripEvent.SelectDropDownCourier(it)
                                )
                                viewModel.obtainEvent(TripEvent.ChangeDropDownStateCourier(false))
                            })
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(
                        color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                    )
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        }
    }
}