package ru.krymer.delivery.ui.screens.shop

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonAlertAddDialog
import ru.krymer.delivery.ui.components.CommonInfoAlertDialog
import ru.krymer.delivery.ui.components.CommonShowDeleteDialog
import ru.krymer.delivery.ui.components.CommonUpdateBottomSheetDialog
import ru.krymer.delivery.ui.components.InfoDialog
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.views.AlertDialogAddRequest
import ru.krymer.delivery.ui.screens.shop.views.AlertDialogAddShop
import ru.krymer.delivery.ui.screens.shop.views.AlertDialogAddSum
import ru.krymer.delivery.ui.screens.shop.views.AlertDialogChangeArrears
import ru.krymer.delivery.ui.screens.shop.views.AlertDialogChangeTypePay
import ru.krymer.delivery.ui.screens.shop.views.AlertDialogRequestShop
import ru.krymer.delivery.ui.screens.shop.views.BottomSheetDialogMillageSave
import ru.krymer.delivery.ui.screens.shop.views.ConfirmView
import ru.krymer.delivery.ui.screens.shop.views.InfoContent
import ru.krymer.delivery.ui.screens.shop.views.InfoShopContent
import ru.krymer.delivery.ui.screens.shop.views.TripShopView
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripShopScreen(
    viewModel: ShopViewModel, navController: NavController
) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState = viewModel.viewState.collectAsState().value
    val sharedViewState = sharedViewModel.viewState.collectAsState().value
    val context = LocalContext.current

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
                        viewModel.obtainEvent(ShopEvent.ShopActionInvoked)
                        navController.popBackStack()
                    })
                    .size(40.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.car_info),
                contentDescription = "courier millage",
                modifier = Modifier
                    .clickable(onClick = {
                        viewModel.obtainEvent(ShopEvent.OpenMillageDialog)
                    })
                    .size(40.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.count),
                contentDescription = "product quantity",
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .clickable(onClick = {
                        viewModel.obtainEvent(ShopEvent.ShowRequestsInfoDialog)
                    })
                    .size(40.dp)
            )
            if (sharedViewModel.initSysAdmMod()) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "add shop",
                    modifier = Modifier
                        .combinedClickable(onClick = {
                            viewModel.obtainEvent(ShopEvent.ShowAddDialogShopCurrentRoute)
                        }, onLongClick = {
                            viewModel.obtainEvent(ShopEvent.ShowAddDialogShopAllRoutes)
                        })
                        .size(40.dp)
                )
            }
        }


        Text(
            style = MaterialTheme.typography.bodyLarge,
            text = sharedViewState.currentTrip!!.nameRoute,
            fontSize = 20.sp,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Text(
            style = MaterialTheme.typography.bodyLarge,
            text = convertToTextDate(sharedViewState.currentTrip.date),
            fontSize = 14.sp,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )


        Spacer(modifier = Modifier.height(15.dp))

        if (!viewState.isLoadShopsData) {
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
            TripShopView(viewState = viewState, onItemClicked = {
                viewModel.obtainEvent(ShopEvent.OpenRequest(shop = it))
            }, onItemLongClicked = {
                viewModel.obtainEvent(
                    ShopEvent.OpenGeoPoint(
                        context = context,
                        cord = it.cord
                    )
                )
            }, onItemDelete = {
                viewModel.obtainEvent(
                    ShopEvent.ShowDeleteDialog(shop = it)
                )
            }, onItemDoubleClicked = {
                viewModel.obtainEvent(ShopEvent.OpenInfoShopDialog(shop = it))
            }, sharedViewModel = sharedViewModel)
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.obtainEvent(ShopEvent.ShopActionInvoked)
        }
    }

    if (viewState.isShowMillageDialog) {
        CommonUpdateBottomSheetDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(ShopEvent.DismissMillageDialog)
        }, onConfirm = {
            viewModel.obtainEvent(ShopEvent.MillageSaveAction)
        }, content = {
            BottomSheetDialogMillageSave(viewState = viewState, onMillageTFC = {
                viewModel.obtainEvent(ShopEvent.ValueChangeMillage(millage = it))
            })
        })
    }

    if (viewState.stateAddDialog) {
        CommonAlertAddDialog(onDismissRequest = {
            viewModel.obtainEvent(ShopEvent.DismissAddDialog)
        }, onConfirmation = {
            viewModel.obtainEvent(ShopEvent.ShopAddAction)
        }, content = {
            AlertDialogAddShop(
                viewState = viewState, viewModel = viewModel
            )
        }, onConfirm = {})
    }

    if (viewState.showDialogAddRequest) {
        CommonAlertAddDialog(onDismissRequest = {
            viewModel.obtainEvent(ShopEvent.DismissDialogAddRequest)
        }, onConfirmation = {
            viewModel.obtainEvent(ShopEvent.RequestAddAction)
        }, content = {
            AlertDialogAddRequest(
                viewState = viewState, viewModel = viewModel
            )
        }, onConfirm = {})
    }

    if (viewState.showRequestDialog) {
        Dialog(onDismissRequest = { viewModel.obtainEvent(ShopEvent.DismissRequestDialog) }) {
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardColors(
                    containerColor = AppTheme.colors.onPrimary,
                    contentColor = AppTheme.colors.onPrimary,
                    disabledContentColor = AppTheme.colors.onPrimary,
                    disabledContainerColor = AppTheme.colors.onPrimary
                )
            ) {
                AlertDialogRequestShop(
                    viewModel = viewModel, viewState = viewState, sharedViewModel = sharedViewModel
                )
            }
        }
    }

    if (viewState.stateIsBlocked) {
        CommonInfoAlertDialog(
            onDismissRequest = { viewModel.obtainEvent(ShopEvent.DismissInfoBlockDialog) },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Text(
                        text = "Доступ ограничен!",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.onSecondary
                    )
                }
            })
    }

    if (viewState.stateInfoDialog) {
        InfoDialog(
            isVisible = true,
            onDismiss = { viewModel.obtainEvent(ShopEvent.DismissRequestInfoDialog) },
            content = {
                InfoContent(
                    viewState,
                    onUpdate = { viewModel.obtainEvent(ShopEvent.CopyInfoData(context = context)) })
            })
    }

    if (viewState.stateInfoShopDialog) {
        CommonInfoAlertDialog(onDismissRequest = { viewModel.obtainEvent(ShopEvent.DismissInfoShopDialog) },
            content = { InfoShopContent(viewState) })
    }

    if (viewState.isShowAddSumDialog) {
        CommonAlertAddDialog(
            onDismissRequest = { viewModel.obtainEvent(ShopEvent.DismissAddSumDialog) },
            onConfirmation = { viewModel.obtainEvent(ShopEvent.SaveAddSum) },
            content = {
                AlertDialogAddSum(onAddSumTFC = {
                    viewModel.obtainEvent(ShopEvent.ChangeAddSum(it))
                }, viewModel = viewModel)
            },
            onConfirm = {})
    }

    if (viewState.isShowTypePayChangeDialog) {
        CommonAlertAddDialog(
            onDismissRequest = { viewModel.obtainEvent(ShopEvent.DismissChangeTypePayDialog) },
            onConfirmation = { viewModel.obtainEvent(ShopEvent.DismissChangeTypePayDialog) },
            content = {
                AlertDialogChangeTypePay(
                    viewState = viewState,
                    onChangeType = { viewModel.obtainEvent(ShopEvent.ChangeTypePay(it)) },
                    onChangeStateTypePay = {
                        viewModel.obtainEvent(
                            ShopEvent.ChangeDropDownStateTypePayChanger(
                                it
                            )
                        )
                    }
                )
            }, onConfirm = {}, modifier = Modifier.background(AppTheme.colors.onSecondary)
        )
    }

    if (viewState.isShowDialogArrears) {
        CommonAlertAddDialog(
            onDismissRequest = { viewModel.obtainEvent(ShopEvent.DismissDialogChangeArrears) },
            onConfirmation = { viewModel.obtainEvent(ShopEvent.SaveArrears) },
            content = {
                AlertDialogChangeArrears(onChangeArrearsTFC = {
                    viewModel.obtainEvent(ShopEvent.ChangeArrears(it))
                })
            },
            onConfirm = {})
    }

    if (viewState.showDeleteDialog) {
        CommonShowDeleteDialog(
            itemName = viewState.itemNameToDelete,
            isVisible = true,
            onDismiss = { viewModel.obtainEvent(ShopEvent.DismissDeleteDialog) },
            onConfirm = { viewModel.obtainEvent(ShopEvent.DeleteAction) })
    }

    if (viewState.stateConfirmRequestDialog) {
        CommonInfoAlertDialog(
            onDismissRequest = { viewModel.obtainEvent(ShopEvent.DismissConfirmRequestDialog) },
            content = {
                ConfirmView(
                    onSubmit = { viewModel.obtainEvent(ShopEvent.RequestSaveAction) },
                    onDismiss = { viewModel.obtainEvent(ShopEvent.DismissConfirmRequestDialog) }
                )
            }
        )
    }
}

















