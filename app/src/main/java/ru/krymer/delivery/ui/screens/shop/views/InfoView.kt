package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopServerModel
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
        if (!state.lightVersion && product.bonus > 0) {
            Text(
                style = AppTheme.typography.bodySmall,
                text = "${product.bonus}",
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(0.15f)
            )
        } else {
            Spacer(
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
        Text(
            style = AppTheme.typography.bodySmall,
            text = "${product.countRemain}",
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary,
            modifier = Modifier.weight(0.15f)
        )
    }
}

@Composable
fun InfoAboutShopForCreate(shop: ShopServerModel, copyInfoData: (ShopServerModel) -> Unit = {}) {
    Column(
        modifier = Modifier
            .combinedClickable(onClick = {}, onLongClick = { copyInfoData(shop) })
            .background(colorResource(id = R.color.tint), shape = RoundedCornerShape(15.dp))
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val sumDept =
            shop.listRequest.sumOf { it.price * it.count - it.price * it.exchange }.toInt()
        val sumGet = (shop.cash + shop.noCash).toInt()
        val newArrears = (shop.arrears - (shop.cash + shop.noCash) + sumDept + shop.addSum).toInt()
        val arrear = shop.arrears.toInt()

        Text(
            style = AppTheme.typography.bodySmall,
            text = convertToTextDate(shop.date),
            modifier = Modifier.fillMaxWidth(),
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
            text = "${stringResource(R.string.request)}: $sumDept",
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "${stringResource(R.string.get_money)}: $sumGet",
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "${stringResource(R.string.arrears)}: $arrear",
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "${stringResource(R.string.new_arrears)}: $newArrears",
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "${stringResource(R.string.cash)}: ${shop.cash.toInt()}",
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "${stringResource(R.string.noCash)}: ${shop.noCash.toInt()}",
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.bodySmall,
            text = "${stringResource(R.string.add_sum)}: " + shop.addSum.toInt(),
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.onSecondary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(0.40f))
            if (shop.isBonus) {
                Text(
                    style = AppTheme.typography.bodySmall,
                    text = stringResource(R.string.bonus),
                    textAlign = TextAlign.Center,
                    color = AppTheme.colors.onSecondary,
                    modifier = Modifier.weight(0.20f)
                )
            } else {
                Spacer(modifier = Modifier.weight(0.20f))
            }
            Text(
                style = AppTheme.typography.bodySmall,
                text = stringResource(R.string.arrears),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.20f),
                color = AppTheme.colors.onSecondary
            )
            Text(
                style = AppTheme.typography.bodySmall,
                text = stringResource(R.string.exchange),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.20f),
                color = AppTheme.colors.onSecondary
            )
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
            Text(
                style = AppTheme.typography.bodySmall,
                text = requestModel.name,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier
                    .weight(0.40f)
            )
            Text(
                style = AppTheme.typography.bodySmall,
                text = if (requestModel.bonus != 0) "${requestModel.bonus}" else "",
                modifier = Modifier.weight(0.20f),
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary
            )
            Text(
                style = AppTheme.typography.bodySmall,
                text = if (requestModel.count != 0) "${requestModel.count}" else "",
                modifier = Modifier.weight(0.20f),
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary
            )
            Text(
                style = AppTheme.typography.bodySmall,
                text = if (requestModel.exchange != 0) "${requestModel.exchange}" else "",
                modifier = Modifier.weight(0.20f),
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary
            )
        }
    }
}



