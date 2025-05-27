package ru.krymer.delivery.ui.screens.shop.views

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@Composable
fun InfoContent(state: ShopViewState, onUpdate: () -> Unit) {
    if (state.isLoadDataRequestsInfoDialog) {
        val count = state.allCountRequestsInfo.collectAsState().value
        val exchange = state.allExchangeRequestsInfo.collectAsState().value
        val requests = state.listInfoRequests.collectAsState().value
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.folow),
                    contentDescription = "clip",
                    modifier = Modifier
                        .clickable(onClick = {
                            onUpdate()
                        })
                        .size(40.dp)
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
                    style = AppTheme.typography.titleMedium,
                    text = "Цена",
                    fontSize = 14.sp,
                    color = AppTheme.colors.onSecondary,
                    modifier = Modifier.weight(0.2f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    style = AppTheme.typography.titleMedium,
                    text = "Бонус",
                    fontSize = 14.sp,
                    color = AppTheme.colors.onSecondary,
                    modifier = Modifier.weight(0.2f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    style = AppTheme.typography.titleMedium,
                    text = "Заявка",
                    fontSize = 14.sp,
                    color = AppTheme.colors.onSecondary,
                    modifier = Modifier.weight(0.2f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    style = AppTheme.typography.titleMedium,
                    text = "Возврат",
                    fontSize = 14.sp,
                    color = AppTheme.colors.onSecondary,
                    modifier = Modifier.weight(0.2f),
                    textAlign = TextAlign.Center,
                )
            }

            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                items(requests) { product ->
                    InfoContentProductItem(product = product)
                }
            }

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    style = AppTheme.typography.titleMedium,
                    text = "Общее: $count",
                    fontSize = 18.sp,
                    color = AppTheme.colors.onSecondary
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    style = AppTheme.typography.titleMedium,
                    text = "Обмены: $exchange",
                    fontSize = 18.sp,
                    color = AppTheme.colors.onSecondary
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    } else {
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

@Composable
fun InfoContentProductItem(product: RequestModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            style = AppTheme.typography.titleSmall,
            text = product.name,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(5.dp)
                .weight(0.2f),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.titleSmall,
            text = "${product.price.toInt()}",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.weight(0.2f)
        )
        Text(
            style = AppTheme.typography.titleSmall,
            text = "${product.bonus}",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.weight(0.2f)
        )
        Text(
            style = AppTheme.typography.titleSmall,
            text = "${product.count}",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.weight(0.2f)
        )
        Text(
            style = AppTheme.typography.titleSmall,
            text = "${product.exchange}",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.weight(0.2f)
        )
    }
}


@Composable
fun ItemInfoShop(shop: ShopModel) {
    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.tint), shape = RoundedCornerShape(15.dp))
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val sumDept = shop.listRequest.sumOf { it.price * it.count - it.price * it.exchange }
        Text(
            style = AppTheme.typography.titleMedium,
            text = convertToTextDate(shop.date),
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary
        )

        Text(
            style = AppTheme.typography.titleMedium,
            text = shop.nameShop,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )

        Text(
            style = AppTheme.typography.titleMedium,
            text = "Пред. реал: " + shop.arrears.toInt(),
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )

        Text(
            style = AppTheme.typography.titleMedium,
            text = "Заявка: " + sumDept.toInt(),
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )

        Text(
            style = AppTheme.typography.titleMedium,
            text = "Доп. сумма: " + shop.addSum.toInt(),
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )

        Text(
            style = AppTheme.typography.titleMedium,
            text = "Нал: " + shop.cash.toInt(),
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )

        Text(
            style = AppTheme.typography.titleMedium,
            text = "Без/Нал: " + shop.noCash.toInt(),
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )

        Text(
            style = AppTheme.typography.titleSmall,
            text = "Получено: " + (shop.cash + shop.noCash).toInt(),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )

        Text(
            style = AppTheme.typography.titleSmall,
            text = "Новый долг: " + (shop.arrears - (shop.cash + shop.noCash) + sumDept).toInt(),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(0.40f)
            ) {}
            if (shop.isBonus) {
                Box(
                    modifier = Modifier
                        .weight(0.20f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        style = AppTheme.typography.titleSmall,
                        text = "Бонус",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.onSecondary
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(0.20f)
                ) {}
            }
            Box(
                modifier = Modifier
                    .weight(0.20f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = "Заявка",
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.20f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = "Обмен",
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        LazyColumn(
            modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = 1000.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(shop.listRequest) { request ->
                RequestInfoItem(request)
            }
        }
    }
}

@Composable
fun RequestInfoItem(requestModel: RequestModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(AppTheme.colors.secondary, shape = RoundedCornerShape(5.dp))
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(0.40f)
            ) {
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = requestModel.name,
                    fontSize = 14.sp,
                    color = AppTheme.colors.onSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.20f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = if (requestModel.bonus != 0) "${requestModel.bonus}" else "",
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.20f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = if (requestModel.count != 0) "${requestModel.count}" else "",
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.20f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = if (requestModel.exchange != 0) "${requestModel.exchange}" else "",
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
            }
        }
    }
}


@Composable
fun InfoShopContent(state: ShopViewState, modifier: Modifier = Modifier.fillMaxWidth()) {
    val listShop = state.listInfoShop.collectAsState().value
    if (listShop.isNotEmpty()) {
        LazyColumn(
            modifier =
            modifier
        ) {
            items(listShop) { shop ->
                ItemInfoShop(shop)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    } else {
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
