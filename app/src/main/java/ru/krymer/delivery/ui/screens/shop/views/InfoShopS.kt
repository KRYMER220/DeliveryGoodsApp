package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.ui.components.CustomCircularProgressIndicator
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@Composable
fun InfoShopContent(shops: List<ShopServerModel>) {
    if (shops.isNotEmpty()) {
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(shops) { shop ->
                val orderMoney =
                    shop.listRequest.sumOf { (it.price * it.count) - (it.price * it.exchange) }
                        .toInt()
                var cash by remember { mutableStateOf(shop.cash.toInt().toString()) }
                var noCash by remember { mutableStateOf(shop.noCash.toInt().toString()) }
                val typePayState = shop.typePay

                Column(
                    modifier = Modifier.background(
                        colorResource(id = R.color.tint),
                        shape = RoundedCornerShape(15.dp)
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        style = AppTheme.typography.titleSmall,
                        text = convertToTextDate(shop.date),
                        color = AppTheme.colors.onSecondary,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        style = AppTheme.typography.titleMedium,
                        text = shop.nameShop,
                        textAlign = TextAlign.Center,
                        color = AppTheme.colors.onSecondary,
                        modifier = Modifier
                            .padding(5.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 3.dp, bottom = 3.dp, start = 15.dp, end = 3.dp)) {
                        Spacer(modifier = Modifier.weight(0.4f))
                        if (shop.isBonus) {
                            Text(
                                style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                text = stringResource(R.string.bonus),
                                modifier = Modifier.weight(0.2f),
                                color = AppTheme.colors.onSecondary,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(0.2f))
                        }
                        Text(
                            style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                            text = stringResource(R.string.request),
                            modifier = Modifier.weight(0.2f),
                            color = AppTheme.colors.onSecondary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                            text = stringResource(R.string.exchange),
                            modifier = Modifier.weight(0.2f),
                            color = AppTheme.colors.onSecondary,
                            textAlign = TextAlign.Center
                        )
                    }


                    if (shop.listRequest.isNotEmpty()) {
                        shop.listRequest.forEach { request ->
                            ProductRequestItemInfo(request = request, shop = shop)
                        }
                    } else {
                        CustomCircularProgressIndicator()
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(0.333f)
                        ) {
                            Text(
                                style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 4).sp),
                                text = stringResource(R.string.arrears),
                                color = AppTheme.colors.onSecondary
                            )
                            Text(
                                style = AppTheme.typography.labelSmall,
                                text = "${shop.arrears.toInt()}",
                                color = AppTheme.colors.onSecondary
                            )
                        }
                        if (shop.addSum > 0.0) {
                            Column(
                                verticalArrangement = Arrangement.SpaceAround,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(0.333f)
                            ) {
                                Text(
                                    style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                    text = stringResource(R.string.add_sum_short),
                                    color = AppTheme.colors.onSecondary
                                )
                                Text(
                                    style = AppTheme.typography.labelSmall,
                                    text = "${shop.addSum.toInt()}",
                                    color = AppTheme.colors.onSecondary
                                )
                            }
                        }
                        Column(
                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(0.333f)
                        ) {
                            Text(
                                style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                text = stringResource(R.string.request),
                                color = AppTheme.colors.onSecondary
                            )
                            Text(
                                style = AppTheme.typography.labelSmall,
                                text = orderMoney.toString(),
                                color = AppTheme.colors.onSecondary
                            )
                        }
                    }

                    when (typePayState) {
                        TypePayModel.ANOTHER -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 2.dp,
                                        color = Color.Green,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.SpaceAround,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(0.333f)
                                    ) {
                                        Text(
                                            style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                            text = stringResource(R.string.cash),
                                            color = AppTheme.colors.onSecondary
                                        )
                                        Text(
                                            style = AppTheme.typography.labelSmall,
                                            text = cash,
                                            color = AppTheme.colors.onSecondary
                                        )
                                    }
                                    Column(
                                        verticalArrangement = Arrangement.SpaceAround,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(0.333f)
                                    ) {
                                        Text(
                                            style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                            text = stringResource(R.string.noCash),
                                            color = AppTheme.colors.onSecondary
                                        )
                                        Text(
                                            style = AppTheme.typography.labelSmall,
                                            text = noCash,
                                            color = AppTheme.colors.onSecondary
                                        )
                                    }
                                }
                            }

                        }

                        TypePayModel.CASH -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.SpaceAround,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(0.333f)
                                ) {
                                    Text(
                                        style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                        text = stringResource(R.string.cash),
                                        color = AppTheme.colors.onSecondary
                                    )
                                    Text(
                                        style = AppTheme.typography.labelSmall,
                                        text = cash,
                                        color = AppTheme.colors.onSecondary
                                    )
                                }
                            }

                        }

                        TypePayModel.NO_CASH -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.SpaceAround,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .border(
                                            width = 2.dp,
                                            color = Color.Magenta,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .weight(0.333f)
                                ) {
                                    Text(
                                        style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                        text = stringResource(R.string.noCash),
                                        color = AppTheme.colors.onSecondary
                                    )
                                    Text(
                                        style = AppTheme.typography.labelSmall,
                                        text = noCash,
                                        color = AppTheme.colors.onSecondary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Spacer(modifier = Modifier.weight(0.333f))
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(0.333f)
                        ) {
                            val newArrear = ((shop.addSum + orderMoney + shop.arrears) - (shop.cash + shop.noCash)).toInt()
                            Text(
                                style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                text = stringResource(R.string.new_arrears),
                                color = AppTheme.colors.onSecondary
                            )
                            Text(
                                style = AppTheme.typography.labelSmall,
                                text = newArrear
                                    .toString(),
                                color = AppTheme.colors.onSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    } else {
        CustomCircularProgressIndicator()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductRequestItemInfo(
    request: RequestModel, shop: ShopServerModel
) {
    var count by remember { mutableStateOf(request.count.toString()) }
    var countBonus by remember { mutableStateOf(request.bonus.toString()) }
    var countExchange by remember { mutableStateOf(request.exchange.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp))
            .padding(top = 3.dp, bottom = 3.dp, start = 15.dp, end = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            style = AppTheme.typography.titleSmall,
            text = request.name,
            modifier = Modifier
                .weight(0.4f)
                .padding(end = 5.dp),
            color = AppTheme.colors.onSecondary
        )
        if (shop.isBonus) {
            Text(
                style = AppTheme.typography.titleSmall,
                text = if (countBonus == "0") "" else countBonus,
                modifier = Modifier.weight(0.2f),
                color = AppTheme.colors.onSecondary,
                textAlign = TextAlign.Center
            )
        } else {
            Spacer(modifier = Modifier.weight(0.2f))
        }
        Text(
            style = AppTheme.typography.titleSmall,
            text = if (count == "0") "" else count,
            modifier = Modifier.weight(0.2f),
            color = AppTheme.colors.onSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            style = AppTheme.typography.titleSmall,
            text = if (countExchange == "0") "" else countExchange,
            modifier = Modifier.weight(0.2f),
            color = AppTheme.colors.onSecondary,
            textAlign = TextAlign.Center
        )
    }
}