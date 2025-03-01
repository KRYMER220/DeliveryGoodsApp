package ru.krymer.delivery.ui.screens.product.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.ProductModel

sealed class ProductAction {
    data object None : ProductAction()
}


data class ProductViewState(
    val isLoadDataProduct: Boolean = false,
    val isError: Boolean = false,
    val errorValue: String = "",
    val listProduct: MutableStateFlow<List<ProductModel>> = MutableStateFlow(listOf()),

    val showAddSheetDialog: Boolean = false,
    val showUpdateSheetDialog: Boolean = false,

    val itemName: String = "",
    val itemPrice: String = "",
    val isActiveProduct: Boolean? = null,
    val productUpdated: ProductModel? = null,

    val itemIdToDelete: Long? = null,
    val itemNameToDelete: String = "",
    val showDeleteDialog: Boolean = false,

    val isErrorPrice: Boolean = false,
    val errorPrice: String = "",
    val isErrorName: Boolean = false,
    val errorName: String = "",
)