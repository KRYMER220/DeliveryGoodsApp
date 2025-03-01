package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun TripShopView(
    viewState: ShopViewState,
    onItemClicked: (ShopModel) -> Unit,
    onItemDelete: (ShopModel) -> Unit,
    onItemLongClicked: (ShopModel) -> Unit,
    onItemDoubleClicked: (ShopModel) -> Unit,
    sharedViewModel: SharedViewModel
) {
    LazyColumn {
        itemsIndexed(viewState.listShop.value, key = { _, item -> item.id }) { index, client ->
            ShopsItem(
                shop = client,
                onItemClicked = onItemClicked,
                onItemDelete = onItemDelete,
                onItemLongClicked = onItemLongClicked,
                onItemDoubleClicked = onItemDoubleClicked,
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                    placementSpec = tween(durationMillis = 400)
                ),
                index = index + 1,
                sharedViewModel = sharedViewModel
            )
            Spacer(modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopsItem(
    shop: ShopModel,
    onItemClicked: (ShopModel) -> Unit,
    onItemDelete: (ShopModel) -> Unit,
    onItemLongClicked: (ShopModel) -> Unit,
    onItemDoubleClicked: (ShopModel) -> Unit,
    modifier: Modifier,
    index: Int,
    sharedViewModel: SharedViewModel
) {
    Box(modifier = modifier
        .combinedClickable(onLongClick = { onItemLongClicked(shop) },
            onClick = { onItemClicked(shop) }, onDoubleClick = { onItemDoubleClicked(shop) })
        .background(
            if (shop.status) colorResource(id = R.color.ready) else colorResource(id = R.color.back),
            shape = RoundedCornerShape(16.dp)
        )
        .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = shop.nameShop,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically),
                color = AppTheme.colors.onSecondary
            )
            Text(
                style = MaterialTheme.typography.labelLarge,
                text = "$index",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 5.dp, end = 5.dp)
                    .align(Alignment.CenterVertically),
                color = AppTheme.colors.onSecondary
            )
            if (sharedViewModel.initSysAdm()) {
                Image(
                    contentDescription = "delete shop",
                    painter = painterResource(id = R.drawable.delete),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = { onItemDelete(shop) })
                )
            }
        }
    }
}
