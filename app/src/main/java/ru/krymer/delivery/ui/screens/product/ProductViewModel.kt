package ru.krymer.delivery.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.ProductApi
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.request.CreateProductRequest
import ru.krymer.delivery.data.request.UpdateProductRequest
import ru.krymer.delivery.ui.screens.product.models.ProductEvent
import ru.krymer.delivery.ui.screens.product.models.ProductViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.isEmptyInput
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productApi: ProductApi, private val sharedViewModel: SharedViewModel
) : ViewModel(), EventHandler<ProductEvent> {

    private val _viewState = MutableStateFlow(ProductViewState())
    val viewState: StateFlow<ProductViewState> = _viewState

    private fun updateViewState(update: (ProductViewState) -> ProductViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }

    override fun obtainEvent(event: ProductEvent) {
        when (event) {
            is ProductEvent.ProductItemClicked -> showUpdateDialog(product = event.product)
            is ProductEvent.ProductSaveAction -> saveProduct()
            is ProductEvent.ShowAddDialog -> showAddDialog()
            is ProductEvent.ShowDeleteDialog -> showDeleteDialog(product = event.product
            )
            is ProductEvent.ProductUpdateAction -> updateProduct()
            is ProductEvent.ChangedNameProduct -> changeName(event.name)
            is ProductEvent.ChangedPriceProduct -> changePrice(event.price)
            is ProductEvent.DismissAddDialog -> dismissAddDialog()
            is ProductEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is ProductEvent.DismissUpdateDialog -> dismissUpdateDialog()
            is ProductEvent.ChangeIsActiveProduct -> changeStatusProduct()
            is ProductEvent.DownItemIndex -> changePosProductInListOnDown(fromIndex = event.index)
            is ProductEvent.UpItemIndex -> changePosProductInListOnUp(fromIndex = event.index)
            is ProductEvent.DeleteProduct -> deleteProduct()
        }
    }

    init {
        getDataProducts()
    }

    private fun changePosProductInListOnDown(fromIndex: Int) {
        launchCoroutine {
            val list = _viewState.value.listProduct.value.map { it.copy() }
            if (fromIndex in 0..list.size - 2) {
                val itemFrom = list[fromIndex].copy()
                val itemTo = list[fromIndex + 1].copy()
                val indexFrom = itemFrom.counter
                val indexTo = itemTo.counter
                val newItemFrom = itemTo.toRequestUpdateIndex(indexFrom)
                val newItemTo = itemFrom.toRequestUpdateIndex(indexTo)
                val responseFrom = productApi.update(product = newItemFrom)
                val responseTo = productApi.update(product = newItemTo)
                if (responseTo.success && responseFrom.success) {
                    val updatedList = list.map { it.copy() }.toMutableList()
                    updatedList[fromIndex] = itemTo.copy(counter = indexFrom)
                    updatedList[fromIndex + 1] = itemFrom.copy(counter = indexTo)
                    updateViewState { it.copy(listProduct = MutableStateFlow(updatedList)) }
                } else {
                    sharedViewModel.message(
                        Constants.ERROR.SERVER_ERROR_RESPONSE
                    )
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
        }
    }

    private fun ProductModel.toRequestUpdateIndex(index: Int): UpdateProductRequest {
        return UpdateProductRequest(
            id = id,
            name = name,
            idFactory = idFactory,
            counter = index,
            price = price,
            isActive = isActive,
            date = date,
            oldPrice = oldPrice
        )
    }

    private fun changePosProductInListOnUp(fromIndex: Int) {
        launchCoroutine {
            val list = _viewState.value.listProduct.value
            if (fromIndex in 1..<list.size) {
                val itemFrom = list[fromIndex].copy()
                val indexFrom = itemFrom.counter
                val itemTo = list[fromIndex - 1].copy()
                val indexTo = itemTo.counter
                val newItemFrom = itemTo.toRequestUpdateIndex(indexFrom)
                val newItemTo = itemFrom.toRequestUpdateIndex(indexTo)
                val responseFrom = productApi.update(product = newItemFrom)
                val responseTo = productApi.update(product = newItemTo)
                if (responseTo.success && responseFrom.success) {
                    val updatedList = list.map { it.copy() }.toMutableList()
                    updatedList[fromIndex] = itemTo.copy(counter = indexFrom)
                    updatedList[fromIndex - 1] = itemFrom.copy(counter = indexTo)
                    updateViewState { it.copy(listProduct = MutableStateFlow(updatedList)) }
                } else {
                    sharedViewModel.message(
                        Constants.ERROR.SERVER_ERROR_RESPONSE
                    )
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
        }
    }

    private fun changeStatusProduct() {
        val product = viewState.value.productUpdated
        product?.let { p ->
            updateViewState { it.copy(productUpdated = p.copy(isActive = !p.isActive)) }
        }
    }

    private fun getDataProducts() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user.value
            if (user != null) {
                val response = productApi.getProducts(idFactory = user.idFactory)
                if (response.success) {
                    val products = response.obj
                    if (products != null) {
                        updateViewState {
                            it.copy(
                                listProduct = MutableStateFlow(products)
                            )
                        }
                    } else {
                        delay(5000)
                        getDataProducts()
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun showAddDialog() {
        updateViewState { it.copy(showAddSheetDialog = true) }
    }

    private fun showUpdateDialog(product: ProductModel) {
        updateViewState {
            it.copy(
                productUpdated = product,
                showUpdateSheetDialog = true,
                isActiveProduct = product.isActive
            )
        }
    }

    private fun showDeleteDialog(product: ProductModel) {
        updateViewState {
            it.copy(
                showDeleteDialog = true,
                productDelete = product
            )
        }
    }

    fun deleteProduct() {
        launchCoroutine {
            val product = viewState.value.productDelete
            if (product != null) {
                val response = productApi.delete(id = product.id)
                if (response.success) {
                    val list =
                        viewState.value.listProduct.value.map { it.copy() }.toMutableList()
                    val item = list.first { it.id == product.id }
                    val listNew = list - item
                    updateViewState { it.copy(listProduct = MutableStateFlow(listNew.sortedBy { p -> p.price })) }
                    dismissDeleteDialog()
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun dismissDeleteDialog() {
        updateViewState {
            it.copy(
                showDeleteDialog = false,
                productDelete = null
            )
        }
    }

    private fun dismissAddDialog() {
        updateViewState {
            it.copy(
                showAddSheetDialog = false,
                itemPrice = Constants.EMPTY.EMPTY_STRING,
                itemName = Constants.EMPTY.EMPTY_STRING
            )
        }
    }

    private fun dismissUpdateDialog() {
        updateViewState {
            it.copy(
                showUpdateSheetDialog = false,
                productUpdated = null,
                isActiveProduct = null,
                itemPrice = Constants.EMPTY.EMPTY_STRING,
                itemName = Constants.EMPTY.EMPTY_STRING
            )
        }
    }

    private fun changeName(name: String) {
        updateViewState { it.copy(itemName = name) }
    }

    private fun changePrice(price: String) {
        updateViewState { it.copy(itemPrice = price) }
    }

    private fun updateProduct() {
        launchCoroutine {
            val product = viewState.value.productUpdated
            val name = viewState.value.itemName
            val price = if (viewState.value.itemPrice == "") 0.0 else viewState.value.itemPrice.toDouble()
            val isActive = viewState.value.isActiveProduct != false
            if (product != null) {
                val productRequest = UpdateProductRequest(
                    id = product.id,
                    name = name,
                    idFactory = product.idFactory,
                    counter = product.counter,
                    price = price,
                    isActive = isActive,
                    date = product.date,
                    oldPrice = product.price
                )
                val response = productApi.update(product = productRequest)
                if (response.success) {
                    val list =
                        viewState.value.listProduct.value.map { it.copy() }.toMutableList()
                    val index = list.indexOfFirst { it.id == product.id }
                    list[index] = product.copy(
                        name = name, isActive = isActive, price = price
                    )
                    updateViewState { it.copy(listProduct = MutableStateFlow(list)) }
                    dismissUpdateDialog()
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun saveProduct() {
        launchCoroutine {
            val name = viewState.value.itemName
            val price = if (viewState.value.itemPrice == "") 0.0 else viewState.value.itemPrice.toDouble()
            val user = sharedViewModel.viewState.value.user.value
            if (user != null) {
                val productRequest = CreateProductRequest(
                    name = name,
                    idFactory = user.idFactory,
                    counter = viewState.value.listProduct.value.size,
                    price = price.toDouble(),
                    isActive = true,
                    date = System.currentTimeMillis(),
                    oldPrice = price.toDouble(),
                )
                val response = productApi.add(product = productRequest)
                if (response.success) {
                    val product = response.obj
                    if (product != null) {
                        val list =
                            viewState.value.listProduct.value.map { it.copy() }.toMutableList()
                        list.add(product)
                        updateViewState { it.copy(listProduct = MutableStateFlow(list.sortedBy { p -> p.price })) }
                        dismissAddDialog()
                    }
                } else {
                    sharedViewModel.message(response.message)
                }

            }
        }
    }
}