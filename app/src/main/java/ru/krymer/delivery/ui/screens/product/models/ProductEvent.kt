package ru.krymer.delivery.ui.screens.product.models

import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.ui.screens.client.models.ClientEvent

sealed class ProductEvent {
    data class ProductItemClicked(val product: ProductModel) : ProductEvent()
    data class ShowDeleteDialog(val product: ProductModel) : ProductEvent()
    data object ShowAddDialog : ProductEvent()
    data object ProductUpdateAction : ProductEvent()
    data object DeleteProduct: ProductEvent()
    data class ChangedNameProduct(val name: String) : ProductEvent()
    data class ChangedPriceProduct(val price: String) : ProductEvent()
    data object ProductSaveAction : ProductEvent()
    data object DismissDeleteDialog : ProductEvent()
    data object DismissAddDialog : ProductEvent()
    data object DismissUpdateDialog : ProductEvent()
    data object ChangeIsActiveProduct : ProductEvent()
    data class ReorderProducts(val fromIndex: Int, val toIndex: Int) : ProductEvent()

}