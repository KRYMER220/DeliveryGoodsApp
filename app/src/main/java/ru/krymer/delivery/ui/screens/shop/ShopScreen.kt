package ru.krymer.delivery.ui.screens.shop

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonAlertAddDialog
import ru.krymer.delivery.ui.components.CommonInfoAlertDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonUpdateDialog
import ru.krymer.delivery.ui.components.ConfirmView
import ru.krymer.delivery.ui.components.InfoDialog
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.views.AddRequestView
import ru.krymer.delivery.ui.screens.shop.views.AddShopAndRequestView
import ru.krymer.delivery.ui.screens.shop.views.ChangeAddSumView
import ru.krymer.delivery.ui.screens.shop.views.ChangeArrearsView
import ru.krymer.delivery.ui.screens.shop.views.ChangeTypePayView
import ru.krymer.delivery.ui.screens.shop.views.AlertDialogRequestShop
import ru.krymer.delivery.ui.screens.shop.views.AnaliticView
import ru.krymer.delivery.ui.screens.shop.views.MillageAndInfoView
import ru.krymer.delivery.ui.screens.shop.views.InfoContent
import ru.krymer.delivery.ui.screens.shop.views.InfoShopContent
import ru.krymer.delivery.ui.screens.shop.views.MessageTextView
import ru.krymer.delivery.ui.screens.shop.views.ShopsItem
import ru.krymer.delivery.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripShopScreen(
    viewModel: ShopViewModel, navController: NavController
) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState = viewModel.viewState.collectAsState().value
    val context = LocalContext.current
    val shops = viewState.listShop.collectAsState().value

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = shops) {
        coroutineScope.launch {
            delay(300)
            lazyListState.animateScrollToItem(1)
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        item {
            Spacer(
                modifier = Modifier
                    .fillParentMaxHeight(0.7f)
                    .fillMaxWidth()
            )
        }

        item {
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
                        .size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.car_info),
                    contentDescription = "courier millage",
                    modifier = Modifier
                        .clickable(onClick = {
                            viewModel.obtainEvent(ShopEvent.OpenMillageDialog)
                        })
                        .size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.count),
                    contentDescription = "product quantity",
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .combinedClickable(onClick = {
                            viewModel.obtainEvent(ShopEvent.ShowRequestsInfoDialog)
                        }, onLongClick = {
                            viewModel.obtainEvent(ShopEvent.ShowHideDialogAnalitic)
                        })
                        .size(50.dp)
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
                            .size(50.dp)
                    )
                }
            }
        }

        if (shops.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                }
            }
        } else {
            itemsIndexed(shops, key = { _, item -> item.id }) { index, shop ->
                ShopsItem(
                    shop = shop,
                    openShop = {
                        viewModel.obtainEvent(ShopEvent.OpenRequest(shop = it))
                    },
                    openLocate = {
                        viewModel.obtainEvent(
                            ShopEvent.OpenGeoPoint(
                                context = context,
                                cord = it.cord
                            )
                        )
                    },
                    openCurrentShopInfo = {
                        viewModel.obtainEvent(ShopEvent.OpenInfoShopDialog(shop = it))
                    },
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                        placementSpec = tween(durationMillis = 400)
                    ),
                    index = index + 1
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.obtainEvent(ShopEvent.ShopActionInvoked)
        }
    }

    if (viewState.isShowMillageDialog) {
        CommonUpdateDialog(isVisible = true, dismiss = {
            viewModel.obtainEvent(ShopEvent.DismissMillageDialog)
        }, confirm = {
            viewModel.obtainEvent(ShopEvent.MillageSaveAction)
        }, content = {
            MillageAndInfoView(viewState = viewState, onMillageTFC = {
                viewModel.obtainEvent(ShopEvent.ValueChangeMillage(millage = it))
            })
        })
    }

    if (viewState.isShowAnaliticTrip) {
        CommonInfoAlertDialog(
            content = {
                AnaliticView(viewState = viewState)
            },
            onDismissRequest = {
                viewModel.obtainEvent(ShopEvent.ShowHideDialogAnalitic)
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    if (viewState.stateAddDialog) {
        CommonAlertAddDialog(onDismiss = {
            viewModel.obtainEvent(ShopEvent.DismissAddDialog)
        }, confirm = {
            viewModel.obtainEvent(ShopEvent.ShopAddAction)
        }, content = {
            AddShopAndRequestView(
                viewState = viewState, viewModel = viewModel
            )
        })
    }

    if (viewState.showDialogAddRequest) {
        CommonAlertAddDialog(onDismiss = {
            viewModel.obtainEvent(ShopEvent.DismissDialogAddRequest)
        }, confirm = {
            viewModel.obtainEvent(ShopEvent.RequestAddAction)
        }, content = {
            AddRequestView(
                viewState = viewState, viewModel = viewModel
            )
        })
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
                    viewModel = viewModel, viewState = viewState
                )
            }
        }
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
            onDismiss = { viewModel.obtainEvent(ShopEvent.DismissAddSumDialog) },
            confirm = { viewModel.obtainEvent(ShopEvent.SaveAddSum) },
            content = {
                ChangeAddSumView(changeAddSum = {
                    viewModel.obtainEvent(ShopEvent.ChangeAddSum(it))
                })
            })
    }

    if (viewState.isShowMessageDialog) {
        CommonUpdateDialog(
            dismiss = {
                viewModel.obtainEvent(ShopEvent.DismissMessageAddDialog)
            },
            confirm = {
                viewModel.obtainEvent(ShopEvent.SendMessage)
            },
            content = {
                MessageTextView(changeTextMessage = {
                    viewModel.obtainEvent(ShopEvent.ChangeMessage(it))
                })
            }, isVisible = true
        )
    }

    if (viewState.isShowTypePayChangeDialog) {
        CommonInfoAlertDialog(
            onDismissRequest = { viewModel.obtainEvent(ShopEvent.DismissChangeTypePayDialog) },
            content = {
                ChangeTypePayView(
                    viewState = viewState,
                    changeTypePay = { viewModel.obtainEvent(ShopEvent.ChangeTypePay(it)) },
                    changeStateChangerTypePay = {
                        viewModel.obtainEvent(
                            ShopEvent.ChangeDropDownStateTypePayChanger(
                                it
                            )
                        )
                    }
                )
            }
        )
    }

    if (viewState.isShowDialogArrears) {
        CommonAlertAddDialog(
            onDismiss = { viewModel.obtainEvent(ShopEvent.DismissDialogChangeArrears) },
            confirm = { viewModel.obtainEvent(ShopEvent.SaveArrears) },
            content = {
                ChangeArrearsView(changeArrears = {
                    viewModel.obtainEvent(ShopEvent.ChangeArrears(it))
                })
            },
            otherFun = {})
    }

    if (viewState.showDeleteDialog) {
        viewState.currentShop?.let {
            CommonDeleteDialog(
                itemName = it.nameShop,
                isVisible = true,
                onDismiss = { viewModel.obtainEvent(ShopEvent.DismissDeleteDialog) },
                onConfirm = { viewModel.obtainEvent(ShopEvent.DeleteShop) })
        }
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

















