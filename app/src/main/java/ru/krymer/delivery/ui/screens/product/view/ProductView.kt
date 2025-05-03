package ru.krymer.delivery.ui.screens.product.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.ui.screens.product.models.ProductViewState
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun ProductView(
    viewState: ProductViewState,
    onItemClicked: (ProductModel) -> Unit,
    onItemDelete: (ProductModel) -> Unit,
    sharedViewState: SharedViewState,
    onItemUpIndex: (Int) -> Unit,
    onItemDownIndex: (Int) -> Unit,
    products: List<ProductModel>
) {
    LazyColumn {
        itemsIndexed(products) { index, product ->
            ProductItem(
                product = product,
                onItemClicked = onItemClicked,
                onItemDelete = onItemDelete,
                viewState = viewState,
                sharedViewState = sharedViewState,
                index = index,
                onItemUpIndex = onItemUpIndex,
                onItemDownIndex = onItemDownIndex
            )
            Spacer(modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}

@Composable
fun ProductItem(
    product: ProductModel,
    onItemClicked: (ProductModel) -> Unit,
    onItemDelete: (ProductModel) -> Unit,
    viewState: ProductViewState,
    sharedViewState: SharedViewState,
    index: Int,
    onItemUpIndex: (Int) -> Unit,
    onItemDownIndex: (Int) -> Unit,
) {
    val list = viewState.listProduct.collectAsState().value.size
    Box(
        modifier = Modifier
            .clickable { onItemClicked(product) }
            .background(
                color = if (product.isActive) AppTheme.colors.secondary else colorResource(R.color.changed),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween
        ) {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = product.name,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 5.dp)
                    .align(Alignment.CenterVertically),
                color = AppTheme.colors.textColor
            )
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = "${product.price}",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(end = 5.dp)
                    .align(Alignment.CenterVertically),
                color = AppTheme.colors.textColor
            )
            Image(
                contentDescription = "delete product",
                painter = painterResource(id = R.drawable.delete),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = { onItemDelete(product) })
            )
            Column(Modifier.padding(start = 10.dp)) {
                if (index != 0) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = null,
                        tint = AppTheme.colors.onSecondary,
                        modifier = Modifier.clickable(onClick = { onItemUpIndex(index) })
                    )
                }
                if (index != list - 1) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            onItemDownIndex(index)
                        }), tint = AppTheme.colors.onSecondary
                    )
                }
            }
        }
    }
}