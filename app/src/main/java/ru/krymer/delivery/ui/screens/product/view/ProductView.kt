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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.ui.components.CommonAddDialog
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
    popBackStack: () -> Unit,
    event: (ProductEvent) -> Unit
) {
    var isFirstLoad by remember { mutableStateOf(true) }
    val list = state.listProduct.collectAsState().value
    var products = remember { mutableStateListOf<ProductModel>() }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        products = products.apply {
            add(to.index, removeAt(from.index))
        }
        event(ProductEvent.ReorderProducts(fromIndex = from.index, toIndex = to.index))
    }

    LaunchedEffect(list) {
        if (list.isNotEmpty() && isFirstLoad) {
            isFirstLoad = false
            products.clear()
            products.addAll(list)
        }
    }

    LaunchedEffect(list.size) {
        products.clear()
        products.addAll(list)
    }

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
                    .size(60.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "add product",
                modifier = Modifier
                    .clickable(onClick = {
                        event(ProductEvent.ShowAddDialog)
                    })
                    .size(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        } else {
            LazyColumn(state = lazyListState, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(products, key = { product -> product.id }) { product ->
                    ReorderableItem(reorderableLazyListState, key = product.id) { isDragging ->
                        ProductItem(
                            modifier = Modifier.draggableHandle(), product = product, event = event
                        )
                    }
                }
            }
        }
    }

    if (state.showAddSheetDialog) {
        CommonSaveDialog(dismiss = {
            event(ProductEvent.DismissAddDialog)
        }, confirm = {
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
        CommonSaveDialog(dismiss = {
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
    modifier: Modifier
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
                    .clickable(onClick = { event(ProductEvent.ShowDeleteDialog(product = product)) })
            )
        }
    }
}