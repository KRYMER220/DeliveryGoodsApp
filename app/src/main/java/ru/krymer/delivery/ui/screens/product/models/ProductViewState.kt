package ru.krymer.delivery.ui.screens.product.models

import ru.krymer.delivery.data.model.ProductModel

data class ProductViewState(
    val products: List<ProductModel> = emptyList(),
    val toggleAddDialog: Boolean = false,
    val toggleUpdateDialog: Boolean = false,
    val isLoading: Boolean = false,
    val itemName: String = "",
    val itemPrice: String = "",
    val itemStatus: Boolean = true,
    val product: ProductModel? = null,
    val toggleDeleteDialog: Boolean = false,
)