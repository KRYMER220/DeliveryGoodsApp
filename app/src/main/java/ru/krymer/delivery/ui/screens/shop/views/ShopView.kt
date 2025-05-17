package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopsItem(
    shop: ShopModel,
    openShop: (ShopModel) -> Unit,
    openLocate: (ShopModel) -> Unit,
    openCurrentShopInfo: (ShopModel) -> Unit,
    modifier: Modifier,
    index: Int
) {
    Box(modifier = modifier
        .combinedClickable(
            onLongClick = { openLocate(shop) },
            onClick = { openShop(shop) }, onDoubleClick = { openCurrentShopInfo(shop) })
        .background(
            color = AppTheme.colors.secondary,
            shape = RoundedCornerShape(10.dp)
        )
        .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                style = AppTheme.typography.labelMedium,
                text = "$index",
                color = AppTheme.colors.onSecondary
            )
            Text(
                style = AppTheme.typography.labelMedium,
                text = shop.nameShop,
                modifier = Modifier
                    .weight(1f),
                color = AppTheme.colors.onSecondary,
                textAlign = TextAlign.Center
            )
            Image(
                contentDescription = "status",
                painter = if (shop.status) painterResource(id = R.drawable.active_circle) else painterResource(id = R.drawable.inactive_circle),
                modifier = Modifier
                    .size(20.dp)
            )
        }
    }
}
