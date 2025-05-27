package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun AnaliticView(state: ShopViewState) {
    val shops = state.listShop.collectAsState().value
    LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(shops, key = { _, item -> item.id }) { index, shop ->
            ShopAnaliticItem(shop)
        }
    }
}

@Composable
fun ShopAnaliticItem(shop: ShopModel) {
    Column(modifier = Modifier
        .background(AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp))
        .padding(5.dp)) {
        val sumDept = shop.listRequest.sumOf { it.price * it.count - it.price * it.exchange }
        Text(
            style = AppTheme.typography.titleMedium,
            text = shop.nameShop,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = AppTheme.typography.titleSmall,
            text = "Пред. реал: " + shop.arrears.toInt(),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = AppTheme.typography.titleSmall,
            text = "Заявка: " + sumDept.toInt(),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = AppTheme.typography.titleSmall,
            text = "Доп. сумма: " + shop.addSum.toInt(),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = AppTheme.typography.titleSmall,
            text = "Нал: " + shop.cash.toInt(),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = AppTheme.typography.titleSmall,
            text = "Без/Нал: " + shop.noCash.toInt(),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = AppTheme.typography.titleSmall,
            text = "Получено: " + (shop.cash + shop.noCash).toInt(),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            style = AppTheme.typography.titleSmall,
            text = "Новый долг: " + (shop.arrears - (shop.cash + shop.noCash) + sumDept).toInt(),
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
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(max = 1000.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(shop.listRequest) { request ->
                RequestInfoItemAnalitic(request)
            }
        }
    }
}

@Composable
fun RequestInfoItemAnalitic(requestModel: RequestModel) {
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
                    style = AppTheme.typography.titleSmall,
                    text = if (requestModel.bonus != 0) "${requestModel.bonus}" else "",
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
                    text = if (requestModel.count != 0) "${requestModel.count}" else "",
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
                    text = if (requestModel.exchange != 0) "${requestModel.exchange}" else "",
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
            }
        }
    }
}