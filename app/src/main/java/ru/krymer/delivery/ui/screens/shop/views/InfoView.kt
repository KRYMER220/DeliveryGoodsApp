package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@Composable
fun InfoContentProductItem(product: RequestModel, state: ShopViewState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            style = AppTheme.typography.bodySmall,
            text = product.name,
            modifier = Modifier
                .padding(5.dp)
                .weight(0.4f),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "${product.price.toInt()}",
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.weight(0.15f)
        )
        if (!state.lightVersion) {
            Text(
                style = AppTheme.typography.bodySmall,
                text = "${product.bonus}",
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(0.15f)
            )
        }
        Text(
            style = AppTheme.typography.bodySmall,
            text = "${product.count}",
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.weight(0.15f)
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "${product.exchange}",
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.weight(0.15f)
        )
    }
}




@Composable
fun ItemInfoShop(shop: ShopModel, copyInfoData: (ShopModel) -> Unit = {}) {
    Column(
        modifier = Modifier.
            combinedClickable(onClick = {}, onLongClick = { copyInfoData(shop) })
            .background(colorResource(id = R.color.tint), shape = RoundedCornerShape(15.dp))
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val sumDept = shop.listRequest.sumOf { it.price * it.count - it.price * it.exchange }
        Text(
            style = AppTheme.typography.bodySmall,
            text = convertToTextDate(shop.date),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = shop.nameShop,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "Заявка: " + sumDept.toInt(),
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "Получено: " + (shop.cash + shop.noCash).toInt(),
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "Долг: " + shop.arrears.toInt() + "  Новый долг: " + (shop.arrears - (shop.cash + shop.noCash) + sumDept + shop.addSum).toInt(),
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "Новый долг: " + (shop.arrears - (shop.cash + shop.noCash) + sumDept + shop.addSum).toInt(),
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "Нал: " + shop.cash.toInt(),
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "Без/Нал: ${shop.noCash.toInt()}",
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "Доп. сумма: " + shop.addSum.toInt(),
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
                        style = AppTheme.typography.bodySmall,
                        text = "Бонус",
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
                    style = AppTheme.typography.bodySmall,
                    text = "Заявка",
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
                    style = AppTheme.typography.bodySmall,
                    text = "Обмен",
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
            }
        }
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
                    style = AppTheme.typography.bodySmall,
                    text = requestModel.name,
                    color = AppTheme.colors.onSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.20f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style = AppTheme.typography.bodySmall,
                    text = if (requestModel.bonus != 0) "${requestModel.bonus}" else "",
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
                    style = AppTheme.typography.bodySmall,
                    text = if (requestModel.count != 0) "${requestModel.count}" else "",
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
                    style = AppTheme.typography.bodySmall,
                    text = if (requestModel.exchange != 0) "${requestModel.exchange}" else "",
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
            }
        }
    }
}


@Composable
fun InfoShopContent(state: ShopViewState) {
    val listShop = state.listInfoShop
    if (listShop.isNotEmpty()) {
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(listShop) { shop ->
                AlertDialogRequestShopInfo(shop = shop)
                Spacer(modifier = Modifier.height(15.dp))
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
