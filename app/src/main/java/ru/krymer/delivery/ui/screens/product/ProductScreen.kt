package ru.krymer.delivery.ui.screens.product

import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonAddDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonUpdateDialog
import ru.krymer.delivery.ui.screens.product.models.ProductEvent
import ru.krymer.delivery.ui.screens.product.view.AddProductView
import ru.krymer.delivery.ui.screens.product.view.UpdateProductView
import ru.krymer.delivery.ui.screens.product.view.ProductView
import ru.krymer.delivery.ui.screens.shared.SharedViewModel

@Composable
fun ProductScreen(
    viewModel: ProductViewModel, navController: NavController
) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState = viewModel.viewState.collectAsState().value
    val sharedViewState = sharedViewModel.viewState.collectAsState().value
    val products = viewState.listProduct.collectAsState().value

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
                        navController.popBackStack()
                    })
                    .size(40.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "add product",
                modifier = Modifier
                    .clickable(onClick = {
                        viewModel.obtainEvent(ProductEvent.ShowAddDialog)
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
            ProductView(
                viewState = viewState, onItemClicked = {
                viewModel.obtainEvent(ProductEvent.ProductItemClicked(it))
            }, onItemDelete = {
                viewModel.obtainEvent(
                    ProductEvent.ShowDeleteDialog(product = it)
                )
                }, sharedViewState = sharedViewState,
                onItemUpIndex = { viewModel.obtainEvent(ProductEvent.UpItemIndex(index = it)) },
                onItemDownIndex = { viewModel.obtainEvent(ProductEvent.DownItemIndex(index = it)) },
                products = products
            )
        }
    }

    if (viewState.showAddSheetDialog) {
        CommonAddDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(ProductEvent.DismissAddDialog)
        }, onConfirm = {
            viewModel.obtainEvent(ProductEvent.ProductSaveAction)
        }, content = {
            AddProductView(changeName = {
                viewModel.obtainEvent(ProductEvent.ChangedNameProduct(it))
            }, changePrice = {
                viewModel.obtainEvent(ProductEvent.ChangedPriceProduct(it))
            })
        })
    }

    if (viewState.showUpdateSheetDialog) {
        CommonUpdateDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(ProductEvent.DismissUpdateDialog)
        }, onConfirm = {
            viewModel.obtainEvent(ProductEvent.ProductUpdateAction)
        }, content = {
            UpdateProductView(viewState = viewState, changeName = {
                viewModel.obtainEvent(ProductEvent.ChangedNameProduct(it))
            }, changePrice = {
                viewModel.obtainEvent(ProductEvent.ChangedPriceProduct(it))
            }, productAction = {
                viewModel.obtainEvent(ProductEvent.ChangeIsActiveProduct)
            })
        })
    }

    if (viewState.showDeleteDialog) {
        viewState.productDelete?.let {
            CommonDeleteDialog(itemName = it.name,
                isVisible = true,
                onDismiss = { viewModel.obtainEvent(ProductEvent.DismissDeleteDialog) },
                onConfirm = { viewModel.obtainEvent(ProductEvent.DeleteProduct) })
        }
    }
}
