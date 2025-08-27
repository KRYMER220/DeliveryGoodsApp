package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.components.CommonAlertAddDialog
import ru.krymer.delivery.ui.components.CommonAlertDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonInfoAlertDialog
import ru.krymer.delivery.ui.components.CommonSaveDialog
import ru.krymer.delivery.ui.components.ConfirmView
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopView(
    state: ShopViewState, event: (ShopEvent) -> Unit, popBackStack: () -> Unit, user: UserModel
) {

    val context = LocalContext.current
    val shops = state.listUIShop
    var isFirstLoad by remember { mutableStateOf(true) }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(shops) {
        if (shops.isNotEmpty() && isFirstLoad) {
            isFirstLoad = false
            coroutineScope.launch {
                delay(500)
                lazyListState.animateScrollToItem(1)
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
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
                    painter = painterResource(id = R.drawable.car_info),
                    contentDescription = "courier millage",
                    modifier = Modifier
                        .combinedClickable(onClick = {
                            event(ShopEvent.OpenMillageDialog)
                        }, onLongClick = {
                            if (state.lightVersion) {
                                event(ShopEvent.ShowHideDialogAnalitic)
                            }
                        })
                        .size(60.dp)
                )
                if (!state.lightVersion) {
                    Image(
                        painter = painterResource(id = R.drawable.count),
                        contentDescription = "product quantity",
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .combinedClickable(onClick = {
                                event(ShopEvent.ShowRequestsInfoDialog)
                            }, onLongClick = {
                                event(ShopEvent.ShowHideDialogAnalitic)
                            })
                            .size(60.dp)
                    )
                }
                if (user.isModOrAdminOrSys()) {
                    Image(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "add shop",
                        modifier = Modifier
                            .combinedClickable(onClick = {
                                event(ShopEvent.ShowAddDialogShopCurrentRoute)
                            }, onLongClick = {
                                event(ShopEvent.ShowAddDialogShopAllRoutes)
                            })
                            .size(60.dp)
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
                        event(ShopEvent.OpenRequest(shop = it))
                    },
                    openLocate = {
                        event(
                            ShopEvent.OpenGeoPoint(
                                context = context,
                                cord = it.cord
                            )
                        )
                    },
                    openInfoCurrentShop = {
                        event(ShopEvent.OpenInfoShopDialog(shop = it))
                    },
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                        placementSpec = tween(durationMillis = 400)
                    ),
                    index = index + 1,
                    user = user,
                    openInfoShop = {
                        event(ShopEvent.ToggleLogsShopDialog(it))
                    }
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }

    if (state.isShowInfoShop) {
        CommonInfoAlertDialog(onDismissRequest = {
            event(ShopEvent.ToggleLogsShopDialog(null))
        }, content = {
            val logs = state.logShop
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                if (logs.isNotEmpty()) {
                    itemsIndexed(logs) { i, log ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                style = AppTheme.typography.titleMedium,
                                text = convertToTextDate(log.date, pattern = Constants.PatternDate.FULL),
                                color = AppTheme.colors.onSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                style = AppTheme.typography.titleMedium,
                                text = "${log.log} \nПользователь: ${log.nameCourier}",
                                color = AppTheme.colors.onSecondary,
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Spacer(modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 5.dp, end = 5.dp)
                                .background(
                                    AppTheme.colors.onSecondary
                                )
                                .height(1.dp))
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                } else {
                    item {
                        Box(modifier = Modifier.fillMaxSize()) {
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
            }
        })
    }

    if (state.isShowMillageDialog) {
        CommonInfoAlertDialog(onDismissRequest = {
            event(ShopEvent.DismissMillageDialog)
        }, content = {
            val count = state.allCountRequestsInfo
            val exchange = state.allExchangeRequestsInfo
            val requests = state.listInfoRequests
            LazyColumn {
                if (state.lightVersion) {
                    item {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                style = AppTheme.typography.titleSmall,
                                text = "Общее: $count",
                                color = AppTheme.colors.onSecondary
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                style = AppTheme.typography.titleSmall,
                                text = "Обмены: $exchange",
                                color = AppTheme.colors.onSecondary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ) {
                            Spacer(
                                modifier = Modifier.weight(0.2f),
                            )
                            Text(
                                style = AppTheme.typography.bodySmall,
                                text = "Цена",
                                color = AppTheme.colors.onSecondary,
                                modifier = Modifier.weight(0.2f),
                                textAlign = TextAlign.Center,
                            )
                            if (!state.lightVersion) {
                                Text(
                                    style = AppTheme.typography.bodySmall,
                                    text = "Бонус",
                                    color = AppTheme.colors.onSecondary,
                                    modifier = Modifier.weight(0.2f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                            Text(
                                style = AppTheme.typography.bodySmall,
                                text = "Заявка",
                                color = AppTheme.colors.onSecondary,
                                modifier = Modifier.weight(0.2f),
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                style = AppTheme.typography.bodySmall,
                                text = "Возврат",
                                color = AppTheme.colors.onSecondary,
                                modifier = Modifier.weight(0.2f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    if (requests.isNotEmpty()) {
                        items(requests) { product ->
                            InfoContentProductItem(product = product, state = state)
                        }
                    } else {
                        item {
                            Box(modifier = Modifier.fillMaxSize()) {
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
                }
                item {
                    if (user.isSysOrAdmin() && !state.lightVersion) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.folow),
                                contentDescription = "clip",
                                modifier = Modifier
                                    .clickable(onClick = {
                                        event(ShopEvent.CopyInfoData(context = context))
                                    })
                                    .size(40.dp)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp))
                }
                item {
                    MillageAndInfoView(state = state, onMillageTFC = {
                        event(ShopEvent.ValueChangeMillage(millage = it))
                    }, event = event)
                }
            }
        })
    }

    if (state.isShowAnaliticTrip) {
        CommonInfoAlertDialog(
            content = {
                AnaliticView(state = state)
            },
            onDismissRequest = {
                event(ShopEvent.ShowHideDialogAnalitic)
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    if (state.stateAddDialog) {
        CommonAlertAddDialog(onDismiss = {
            event(ShopEvent.DismissAddDialog)
        }, confirm = {
            event(ShopEvent.ShopAddAction)
        }, content = {
            AddShopAndRequestView(
                state = state, event = event
            )
        })
    }

    if (state.showDialogAddRequest) {
        CommonAlertAddDialog(onDismiss = {
            event(ShopEvent.DismissDialogAddRequest)
        }, confirm = {
            event(ShopEvent.RequestAddAction)
        }, content = {
            AddRequestView(
                state = state, event = event
            )
        })
    }

    if (state.isCopyAndSave) {
        CommonAlertDialog(
            isVisible = true,
            onDismiss = {
                event(ShopEvent.ChangeStateIsCopyDialog)
            }, onConfirm = {
                event(ShopEvent.CopyAndSaveShop)
            }
        )
    }

    if (state.showRequestDialog) {
        Dialog(onDismissRequest = { event(ShopEvent.DismissRequestDialog) }, properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(), colors = CardColors(
                    containerColor = AppTheme.colors.onPrimary,
                    contentColor = AppTheme.colors.onPrimary,
                    disabledContentColor = AppTheme.colors.onPrimary,
                    disabledContainerColor = AppTheme.colors.onPrimary
                )
            ) {
                AlertDialogRequestShop(
                    event = event, state = state, user = user
                )
            }
        }
    }

    if (state.stateInfoDialog) {
        CommonInfoAlertDialog(
            onDismissRequest = { event(ShopEvent.DismissRequestInfoDialog) },
            content = {
                InfoContent(
                    state = state,
                    onUpdate = { event(ShopEvent.CopyInfoData(context = context)) })
            })
    }

    if (state.stateInfoShopDialog) {
        CommonInfoAlertDialog(
            onDismissRequest = { event(ShopEvent.DismissInfoShopDialog) },
            content = { InfoShopContent(state = state) })
    }

    if (state.isShowAddSumDialog) {
        CommonAlertAddDialog(
            onDismiss = { event(ShopEvent.DismissAddSumDialog) },
            confirm = { event(ShopEvent.SaveAddSum) },
            content = {
                ChangeAddSumView(changeAddSum = {
                    event(ShopEvent.ChangeAddSum(it))
                })
            })
    }

    if (state.isShowMessageDialog) {
        CommonSaveDialog(
            dismiss = {
                event(ShopEvent.ToggleMessageDialog)
            },
            confirm = {
                event(ShopEvent.SendMessage)
            },
            content = {
                MessageTextView(
                    changeTextMessage = {
                        event(ShopEvent.ChangeMessage(it))
                    },
                    deleteMessage = {
                        event(ShopEvent.DeleteMessage(it))
                    },
                    state = state,
                )
            }
        )
    }


    if (state.isShowDialogArrears) {
        CommonAlertAddDialog(
            onDismiss = { event(ShopEvent.DismissDialogChangeArrears) },
            confirm = { event(ShopEvent.SaveArrears) },
            content = {
                ChangeArrearsView(changeArrears = {
                    event(ShopEvent.ChangeArrears(it))
                })
            },
            otherFun = {})
    }

    if (state.showDeleteDialog) {
        state.currentShop?.let {
            CommonDeleteDialog(
                itemName = it.nameShop,
                isVisible = true,
                onDismiss = { event(ShopEvent.DismissDeleteDialog) },
                onConfirm = { event(ShopEvent.DeleteShop) })
        }
    }

    if (state.stateConfirmRequestDialog) {
        CommonInfoAlertDialog(
            onDismissRequest = { event(ShopEvent.DismissConfirmRequestDialog) },
            content = {
                ConfirmView(
                    onSubmit = { event(ShopEvent.RequestSaveAction) },
                    onDismiss = { event(ShopEvent.DismissConfirmRequestDialog) }
                )
            }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopsItem(
    shop: ShopModel,
    openShop: (ShopModel) -> Unit,
    openLocate: (ShopModel) -> Unit,
    openInfoCurrentShop: (ShopModel) -> Unit,
    modifier: Modifier,
    index: Int,
    user: UserModel,
    openInfoShop: (ShopModel) -> Unit
) {
    Box(modifier = modifier
        .heightIn(min = 60.dp, max = Dp.Unspecified)
        .combinedClickable(
            onLongClick = { openLocate(shop) },
            onClick = { openShop(shop) }, onDoubleClick = { openInfoCurrentShop(shop) })
        .background(
            color = AppTheme.colors.secondary,
            shape = RoundedCornerShape(10.dp)
        )
        .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                style = AppTheme.typography.labelMedium,
                text = "$index",
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                style = AppTheme.typography.titleSmall,
                text = shop.nameShop,
                modifier = Modifier
                    .weight(1f),
                color = AppTheme.colors.onSecondary,
            )
            if (user.isSysOrAdmin() && shop.isChanged) {
                Image(
                    contentDescription = "info",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = {
                            openInfoShop(shop)
                        }),
                    painter = painterResource(R.drawable.info_shop)
                )
            }
            Image(
                contentDescription = "status",
                painter = if (shop.status) painterResource(id = R.drawable.active_circle) else painterResource(id = R.drawable.inactive_circle),
                modifier = Modifier
                    .size(15.dp)
            )
        }
    }
}
