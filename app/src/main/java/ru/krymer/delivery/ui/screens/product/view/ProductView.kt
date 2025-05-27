package ru.krymer.delivery.ui.screens.product.view

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.ui.components.CommonAddDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonUpdateDialog
import ru.krymer.delivery.ui.screens.product.models.ProductEvent
import ru.krymer.delivery.ui.screens.product.models.ProductViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun ProductView(
    state: ProductViewState,
    popBackStack: () -> Unit,
    event: (ProductEvent) -> Unit
) {
    val products = state.listProduct.collectAsState().value
    Column(modifier = Modifier.padding(15.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_stack),
                contentDescription = "exit",
                modifier = Modifier
                    .clickable(onClick = {
                        popBackStack()
                    })
                    .size(40.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "add product",
                modifier = Modifier
                    .clickable(onClick = {
                        event(ProductEvent.ShowAddDialog)
                    })
                    .size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        } else {
            LazyColumn {
                itemsIndexed(products) { index, product ->
                    ProductItem(
                        product = product,
                        event = event,
                        index = index,
                        sizeList = products.size
                    )
                    Spacer(modifier = Modifier.padding(bottom = 10.dp))
                }
            }
        }
    }

    if (state.showAddSheetDialog) {
        CommonAddDialog(isVisible = true, onDismiss = {
            event(ProductEvent.DismissAddDialog)
        }, onConfirm = {
            event(ProductEvent.ProductSaveAction)
        }, content = {
            AddProductView(changeName = {
                event(ProductEvent.ChangedNameProduct(it))
            }, changePrice = {
                event(ProductEvent.ChangedPriceProduct(it))
            })
        })
    }

    if (state.showUpdateSheetDialog) {
        CommonUpdateDialog(isVisible = true, dismiss = {
            event(ProductEvent.DismissUpdateDialog)
        }, confirm = {
            event(ProductEvent.ProductUpdateAction)
        }, content = {
            UpdateProductView(viewState = state, changeName = {
                event(ProductEvent.ChangedNameProduct(it))
            }, changePrice = {
                event(ProductEvent.ChangedPriceProduct(it))
            }, productAction = {
                event(ProductEvent.ChangeIsActiveProduct)
            })
        })
    }

    if (state.showDeleteDialog) {
        state.productDelete?.let {
            CommonDeleteDialog(
                itemName = it.name,
                isVisible = true,
                onDismiss = { event(ProductEvent.DismissDeleteDialog) },
                onConfirm = { event(ProductEvent.DeleteProduct) })
        }
    }
}

@Composable
fun ProductItem(
    product: ProductModel,
    event: (ProductEvent) -> Unit,
    index: Int,
    sizeList: Int,
) {
    Box(
        modifier = Modifier
            .clickable { event(ProductEvent.ProductItemClicked(product = product)) }
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
                    .clickable(onClick = { event(ProductEvent.ShowDeleteDialog(product = product)) })
            )
            Column(Modifier.padding(start = 10.dp)) {
                if (index != 0) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = null,
                        tint = AppTheme.colors.onSecondary,
                        modifier = Modifier.clickable(onClick = {
                            event(
                                ProductEvent.UpItemIndex(
                                    index = index
                                )
                            )
                        })
                    )
                }
                if (index != sizeList - 1) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            event(ProductEvent.DownItemIndex(index = index))
                        }), tint = AppTheme.colors.onSecondary
                    )
                }
            }
        }
    }
}