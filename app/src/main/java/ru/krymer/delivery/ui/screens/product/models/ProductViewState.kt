package ru.krymer.delivery.ui.screens.product.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.ProductModel

sealed class ProductAction {
    data object None : ProductAction()
}


data class ProductViewState(
    val listProduct: MutableStateFlow<List<ProductModel>> = MutableStateFlow(listOf()),
    val showAddSheetDialog: Boolean = false,
    val showUpdateSheetDialog: Boolean = false,
    val isLoadData: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val itemName: String = "",
    val itemPrice: String = "",
    val productUpdated: MutableStateFlow<ProductModel?> = MutableStateFlow(null),
    val productDelete: ProductModel? = null,
    val showDeleteDialog: Boolean = false,
)