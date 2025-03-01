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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonAddBottomSheetDialog
import ru.krymer.delivery.ui.components.CommonShowDeleteDialog
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.components.CommonUpdateBottomSheetDialog
import ru.krymer.delivery.ui.screens.product.models.ProductEvent
import ru.krymer.delivery.ui.screens.product.models.ProductViewState
import ru.krymer.delivery.ui.screens.product.view.ProductView
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants

@Composable
fun ProductScreen(
    viewModel: ProductViewModel, navController: NavController
) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState = viewModel.viewState.collectAsState().value
    val sharedViewState = sharedViewModel.viewState.collectAsState().value

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
        if (!viewState.isLoadDataProduct) {
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
            ProductView(viewState = viewState, onItemClicked = {
                viewModel.obtainEvent(ProductEvent.ProductItemClicked(it))
            }, onItemDelete = {
                viewModel.obtainEvent(
                    ProductEvent.ShowDeleteDialog(
                        itemID = it.id,
                        itemName = it.name
                    )
                )
            }, sharedViewState = sharedViewState)
        }
    }

    if (viewState.showAddSheetDialog) {
        CommonAddBottomSheetDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(ProductEvent.DismissAddDialog)
        }, onConfirm = {
            viewModel.obtainEvent(ProductEvent.ProductSaveAction)
        }, content = {
            BottomSheetDialogAddProduct(viewState = viewState, onValueNameChange = {
                viewModel.obtainEvent(ProductEvent.ChangedNameProduct(it))
            }, onValuePriceChange = {
                viewModel.obtainEvent(ProductEvent.ChangedPriceProduct(it))
            })
        })
    }

    if (viewState.showUpdateSheetDialog) {
        CommonUpdateBottomSheetDialog(isVisible = true, onDismiss = {
            viewModel.obtainEvent(ProductEvent.DismissUpdateDialog)
        }, onConfirm = {
            viewModel.obtainEvent(ProductEvent.ProductUpdateAction)
        }, content = {
            BottomSheetDialogUpdateProduct(viewState = viewState, onValueNameChange = {
                viewModel.obtainEvent(ProductEvent.ChangedNameProduct(it))
            }, onValuePriceChange = {
                viewModel.obtainEvent(ProductEvent.ChangedPriceProduct(it))
            }, onCheckActive = {
                viewModel.obtainEvent(ProductEvent.ChangeIsActiveProduct)
            })
        })
    }

    if (viewState.showDeleteDialog) {
        CommonShowDeleteDialog(itemName = viewState.itemNameToDelete,
            isVisible = true,
            onDismiss = { viewModel.obtainEvent(ProductEvent.DismissDeleteDialog) },
            onConfirm = { viewModel.deleteItemConfirmed() })
    }
}

@Composable
private fun BottomSheetDialogAddProduct(
    viewState: ProductViewState,
    onValueNameChange: (String) -> Unit,
    onValuePriceChange: (String) -> Unit
) {
    Column {
        CommonTextField(
            isError = viewState.isErrorName,
            errorValue = viewState.errorName,
            value = viewState.itemName,
            placeholder = stringResource(
                id = R.string.name
            ),
            onVC = onValueNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)

        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            isError = viewState.isErrorPrice,
            errorValue = viewState.errorPrice,
            value = viewState.itemPrice,
            placeholder = stringResource(
                id = R.string.price
            ),
            onVC = onValuePriceChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun BottomSheetDialogUpdateProduct(
    viewState: ProductViewState,
    onValueNameChange: (String) -> Unit,
    onValuePriceChange: (String) -> Unit,
    onCheckActive: (Boolean) -> Unit
) {
    Column {
        CommonTextField(
            isError = viewState.isErrorName,
            errorValue = viewState.errorValue,
            value = viewState.itemName,
            placeholder = stringResource(
                id = R.string.name
            ),
            onVC = onValueNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            isError = viewState.isErrorPrice,
            errorValue = viewState.errorValue,
            value = viewState.itemPrice,
            placeholder = stringResource(
                id = R.string.price
            ),
            onVC = onValuePriceChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            val status = viewState.isActiveProduct ?: true
            Text(
                text = if (status) Constants.ACTIONS.HIDE else Constants.ACTIONS.SHOW,
                color = AppTheme.colors.onSecondary
            )
            Checkbox(
                checked = status,
                onCheckedChange = onCheckActive
            )
        }
    }
}