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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.utilModel.Loader
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonSaveDialog
import ru.krymer.delivery.ui.screens.product.models.ProductEvent
import ru.krymer.delivery.ui.screens.product.models.ProductViewState
import ru.krymer.delivery.ui.theme.AppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ProductView(
    state: ProductViewState,
    event: (ProductEvent) -> Unit
) {

    val list = state.products
    val loader = when {
        state.isLoading -> Loader.LOADING
        state.products.isEmpty() -> Loader.EMPTY
        else -> Loader.LOAD
    }
    var products = remember { mutableStateListOf<ProductModel>() }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        products = products.apply {
            add(to.index, removeAt(from.index))
        }
    }

    LaunchedEffect(state.isLoading) {
        products.clear()
        products.addAll(list)
    }

    Column(modifier = Modifier.padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier)
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "add product",
                modifier = Modifier
                    .clickable(onClick = {
                        event(ProductEvent.ToggleAddDialog)
                    })
                    .size(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        when(loader) {
            Loader.LOAD -> {
                LazyColumn(state = lazyListState, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(products, key = { product -> product.id }) { product ->
                        ReorderableItem(reorderableLazyListState, key = product.id) { isDragging ->
                            ProductItem(
                                modifier = Modifier.draggableHandle(
                                    onDragStopped = {
                                        event(ProductEvent.ReorderProducts(list = products))
                                    }), product = product, event = event
                            )
                        }
                    }
                }
            }
            Loader.EMPTY -> {
                Text(text = stringResource(R.string.empty_data), color = AppTheme.colors.onSecondary, fontSize = 18.sp)
            }
            Loader.LOADING -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(60.dp),
                    strokeWidth = 2.dp,
                    color = AppTheme.colors.onSecondary
                )
            }
        }
    }

    if (state.toggleAddDialog) {
        CommonSaveDialog(dismiss = {
            event(ProductEvent.ToggleAddDialog)
        }, confirm = {
            event(ProductEvent.CreateProduct)
        }, content = {
            AddProductView(changeName = {
                event(ProductEvent.ChangedNameProduct(it))
            }, changePrice = {
                event(ProductEvent.ChangedPriceProduct(it))
            })
        })
    }


    if (state.toggleUpdateDialog) {
        CommonSaveDialog(dismiss = {
            event(ProductEvent.ToggleUpdateDialog(null))
        }, confirm = {
            event(ProductEvent.UpdateProduct)
        }, content = {
            UpdateProductView(state = state, changeName = {
                event(ProductEvent.ChangedNameProduct(it))
            }, changePrice = {
                event(ProductEvent.ChangedPriceProduct(it))
            }, productAction = {
                event(ProductEvent.ChangeStatusProduct)
            })
        })
    }

    if (state.toggleDeleteDialog) {
        state.product?.let {
            CommonDeleteDialog(
                itemName = it.name,
                isVisible = true,
                onDismiss = { event(ProductEvent.ToggleDeleteDialog(null)) },
                onConfirm = { event(ProductEvent.DeleteProduct) })
        }
    }
}

@Composable
fun ProductItem(
    product: ProductModel,
    event: (ProductEvent) -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = Modifier
            .clickable { event(ProductEvent.ToggleUpdateDialog(product = product)) }
            .background(
                color = if (product.isActive) AppTheme.colors.secondary else colorResource(R.color.changed),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Image(contentDescription = "drop", painter = painterResource(id = R.drawable.list_item), modifier = modifier.size(40.dp))
            Text(
                style = AppTheme.typography.titleMedium,
                text = product.name,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 5.dp),
                textAlign = TextAlign.Center,
                color = AppTheme.colors.textColor
            )
            Text(
                style = AppTheme.typography.titleMedium,
                text = "${product.price.toInt()}",
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
                    .clickable(onClick = { event(ProductEvent.ToggleDeleteDialog(product = product)) })
            )
        }
    }
}