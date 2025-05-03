package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
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
fun InfoContent(viewState: ShopViewState, onUpdate: () -> Unit) {
    if (viewState.isLoadDataRequestsInfoDialog) {
        val count = viewState.allCountRequestsInfo.collectAsState().value
        val exchange = viewState.allExchangeRequestsInfo.collectAsState().value
        val requests = viewState.listInfoRequests.collectAsState().value

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
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Продукт",
                fontSize = 12.sp,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(0.2f),
            )
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Цена",
                fontSize = 12.sp,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(0.2f),
                textAlign = TextAlign.Center,
            )
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Бонус",
                fontSize = 12.sp,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(0.2f),
                textAlign = TextAlign.Center,
            )
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Заявка",
                fontSize = 12.sp,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(0.2f),
                textAlign = TextAlign.Center,
            )
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Возврат",
                fontSize = 12.sp,
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
                style = MaterialTheme.typography.labelSmall,
                text = "Общее: $count",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Обмены: $exchange",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
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
    Box(
        modifier = Modifier
            .padding(5.dp)
            .height(30.dp)
            .wrapContentHeight()
            .background(color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = product.name,
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(0.2f)
                    .padding(start = 3.dp),
                color = AppTheme.colors.onSecondary
            )
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "${product.price}",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(0.2f)
            )
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "${product.bonus}",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(0.2f)
            )
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "${product.count}",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(0.2f)
            )
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "${product.exchange}",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(0.2f)
            )
        }
    }
}


@Composable
fun ItemInfoShop(shop: ShopModel) {
    Column(
        Modifier
            .background(colorResource(id = R.color.tint), shape = RoundedCornerShape(15.dp))
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        val sumDept = shop.listRequest.sumOf { it.price * it.count - it.price * it.exchange }
        Text(
            style = MaterialTheme.typography.labelSmall,
            text = convertToTextDate(shop.date),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = MaterialTheme.typography.labelSmall,
            text = shop.nameShop,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = MaterialTheme.typography.labelSmall,
            text = "Пред. реал: " + shop.arrears,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = MaterialTheme.typography.labelSmall,
            text = "Заявка: " + sumDept,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = MaterialTheme.typography.labelSmall,
            text = "Доп. сумма: " + shop.addSum,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = MaterialTheme.typography.labelSmall,
            text = "Нал: " + shop.cash,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = MaterialTheme.typography.labelSmall,
            text = "Без/Нал: " + shop.noCash,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(0.40f)
            ) {
                Text(
                    style = MaterialTheme.typography.labelSmall,
                    text = "Товар",
                    fontSize = 12.sp,
                    color = AppTheme.colors.onSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.20f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style = MaterialTheme.typography.labelSmall,
                    text = "Бонус",
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
                    style = MaterialTheme.typography.labelSmall,
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
                    style = MaterialTheme.typography.labelSmall,
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
                .heightIn(max = 200.dp)
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
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(0.40f)
            ) {
                Text(
                    style = MaterialTheme.typography.labelSmall,
                    text = requestModel.name,
                    fontSize = 12.sp,
                    color = AppTheme.colors.onSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.20f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style = MaterialTheme.typography.labelSmall,
                    text = "${requestModel.bonus}",
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
                    style = MaterialTheme.typography.labelSmall,
                    text = "${requestModel.count}",
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
                    style = MaterialTheme.typography.labelSmall,
                    text = "${requestModel.exchange}",
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
            }
        }
    }
}


@Composable
fun InfoShopContent(viewState: ShopViewState) {
    if (viewState.stateInfoShopIsDataLoad) {
        val listShop = viewState.listInfoShop.collectAsState().value
        LazyColumn(
            modifier =
            Modifier
                .fillMaxWidth()
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
