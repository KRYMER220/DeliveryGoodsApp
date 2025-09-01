package ru.krymer.delivery.ui.screens.product.models

import ru.krymer.delivery.data.model.ProductModel

sealed class ProductEvent {
    data class ToggleUpdateDialog(val product: ProductModel?) : ProductEvent()
    data class ToggleDeleteDialog(val product: ProductModel?) : ProductEvent()
    data object ToggleAddDialog : ProductEvent()
    data object UpdateProduct : ProductEvent()
    data object DeleteProduct: ProductEvent()
    data class ChangedNameProduct(val name: String) : ProductEvent()
    data class ChangedPriceProduct(val price: String) : ProductEvent()
    data object CreateProduct : ProductEvent()
    data object ChangeStatusProduct : ProductEvent()
    data class ReorderProducts(val list: List<ProductModel>) : ProductEvent()
}