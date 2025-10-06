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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.StatusModel
import ru.krymer.delivery.data.model.utilModel.toStatusModel
import ru.krymer.delivery.ui.components.CommonAlertAddDialog
import ru.krymer.delivery.ui.components.CommonConfirmDialog
import ru.krymer.delivery.ui.components.CommonInfoAlertDialog
import ru.krymer.delivery.ui.components.CustomCircularProgressIndicator
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopView(
    state: ShopViewState,
    event: (ShopEvent) -> Unit,
    user: UserModel,
    routeToRequest: (ShopModel) -> Unit
) {

    val context = LocalContext.current
    val shops = state.shops
    val lazyListState = rememberLazyListState()
    state.currentTrip?.let { trip ->

        val showStickyHeader by remember {
            derivedStateOf {
                lazyListState.firstVisibleItemIndex > 1 ||
                        (lazyListState.firstVisibleItemIndex == 1 && lazyListState.firstVisibleItemScrollOffset > 0)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
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
                            .fillParentMaxHeight(0.5f)
                            .fillMaxWidth()
                    )
                }

                item {
                    HeaderContentShop(
                        user = user,
                        event = event,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        trip = trip
                    )
                }

                if (shops.isEmpty()) {
                    item {
                        CustomCircularProgressIndicator()
                    }
                } else {
                    itemsIndexed(shops, key = { _, item -> item.id }) { index, shop ->

                        ShopsItem(
                            shop = shop,
                            openShop = {
                                routeToRequest(it)
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
                                event(ShopEvent.ToggleInfoCurrentShopDialog)
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
            if (showStickyHeader) {
                HeaderContentShop(
                    user = user,
                    event = event,
                    modifier = Modifier
                        .background(color = AppTheme.colors.onPrimary)
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(10.dp)
                        .pointerInput(Unit) {},
                    trip = trip
                )
            }
        }
    }

    if (state.toggleLogShop) {
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
                                text = "${log.log} \n${stringResource(R.string.user)}: ${log.nameCourier}",
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
                        CustomCircularProgressIndicator()
                    }
                }
            }
        })
    }

    if (state.toggleMillageDialog) {
        CommonInfoAlertDialog(onDismissRequest = {
            event(ShopEvent.ToggleMillageDialog)
        }, content = {
            val count = state.allCountRequestsInfo
            val exchange = state.allExchangeRequestsInfo
            val requests = state.listInfoRequests
            val isBonus = requests.any { it.bonus > 0 }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                item {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            style = AppTheme.typography.titleSmall,
                            text = "${stringResource(R.string.count_all)}: $count",
                            color = AppTheme.colors.onSecondary
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            style = AppTheme.typography.titleSmall,
                            text = "${stringResource(R.string.exchange)}: $exchange",
                            color = AppTheme.colors.onSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Spacer(
                            modifier = Modifier.weight(0.4f),
                        )
                        Text(
                            style = AppTheme.typography.bodySmall,
                            text = stringResource(R.string.price),
                            color = AppTheme.colors.onSecondary,
                            modifier = Modifier.weight(0.15f),
                            textAlign = TextAlign.Center,
                        )
                        if (isBonus) {
                            Text(
                                style = AppTheme.typography.bodySmall,
                                text = stringResource(R.string.bonus),
                                color = AppTheme.colors.onSecondary,
                                modifier = Modifier.weight(0.15f),
                                textAlign = TextAlign.Center,
                            )
                        }
                        Text(
                            style = AppTheme.typography.bodySmall,
                            text = stringResource(R.string.request),
                            color = AppTheme.colors.onSecondary,
                            modifier = Modifier.weight(0.15f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            style = AppTheme.typography.bodySmall,
                            text = stringResource(R.string.exchange),
                            color = AppTheme.colors.onSecondary,
                            modifier = Modifier.weight(0.15f),
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
                        CustomCircularProgressIndicator()
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
                                contentDescription = null,
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
                    MillageView(state = state, onMillageTFC = {
                        event(ShopEvent.ValueChangeMillage(millage = if (it.isEmpty()) 0.0 else it.toDouble()))
                    }, event = event)
                }
            }
        })
    }

    if (state.toggleAnaliticOfTrip) {
        CommonInfoAlertDialog(
            content = {
                InfoShopContent(shops = state.shopsAnalitic)
            },
            onDismissRequest = {
                event(ShopEvent.ToggleAnaliticShopsCurrentTrip)
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    if (state.toggleAddDialog) {
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

    if (state.isCopyAndSave) {
        CommonConfirmDialog(
            onDismiss = {
                event(ShopEvent.ToggleConfirmCopyAndSave)
            }, onConfirm = {
                event(ShopEvent.CopyAndSaveShop)
            },
            text = stringResource(R.string.copy_confirm)
        )
    }

    if (state.toggleCurrentShopInfo) {
        CommonInfoAlertDialog(
            onDismissRequest = { event(ShopEvent.ToggleInfoCurrentShopDialog) },
            content = {
                InfoShopContent(shops = state.listCurrentShopInfo)
            })
    }
}

@Composable
fun HeaderContentShop(
    user: UserModel,
    event: (ShopEvent) -> Unit,
    modifier: Modifier = Modifier,
    trip: TripModel
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.car_info),
            contentDescription = "courier millage",
            modifier = Modifier
                .combinedClickable(onClick = {
                    event(ShopEvent.ToggleMillageDialog)
                    event(ShopEvent.GetDataRequestsByTrip)
                }, onLongClick = {
                    event(ShopEvent.ToggleAnaliticShopsCurrentTrip)
                })
                .size(60.dp)
        )
        Text(
            style = AppTheme.typography.titleSmall,
            text = convertToTextDate(trip.date),
            color = AppTheme.colors.onSecondary
        )
        if (user.isModOrAdminOrSys()) {
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = null,
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
            onClick = { openShop(shop) },
            onDoubleClick = { openInfoCurrentShop(shop) })
        .background(
            color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
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
            if (user.isModOrAdminOrSys() && shop.isChanged) {
                Box(modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = {
                        openInfoShop(shop)
                    }), contentAlignment = Alignment.Center) {
                    Image(
                        contentDescription = null,
                        painter = painterResource(R.drawable.info_shop),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Image(
                contentDescription = null,
                painter = when {
                    !shop.status && shop.statusServer.toStatusModel() == StatusModel.NOT_CHANGE -> painterResource(
                        id = R.drawable.inactive_circle
                    )

                    shop.statusServer.toStatusModel() == StatusModel.UN_SYNC -> painterResource(id = R.drawable.unsync_circle)
                    else -> painterResource(id = R.drawable.active_circle)
                },
                modifier = Modifier
                    .size(15.dp)
            )
        }
    }
}
