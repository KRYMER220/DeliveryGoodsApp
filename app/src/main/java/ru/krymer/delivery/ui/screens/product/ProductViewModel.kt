package ru.krymer.delivery.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productApi: ProductApi, private val sharedViewModel: SharedViewModel
) : ViewModel(), EventHandler<ProductEvent> {

    private val _viewState = MutableStateFlow(ProductViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateViewState(update: (ProductViewState) -> ProductViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                sharedViewModel.message(Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR)
            } catch (e: Exception) {
                sharedViewModel.message(e.message, type = TypeMessageModel.ERROR)
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
            is ProductEvent.DeleteProduct -> deleteProduct()
            is ProductEvent.ReorderProducts -> reorderProducts(list = event.list)
        }
    }

    init {
        getDataProducts()
    }

    private fun reorderProducts(list: List<ProductModel>) {
        launchCoroutine {
            val requests = list.map { product -> UpdateProductRequest(
                id = product.id,
                idFactory = product.idFactory,
                name = product.name,
                counter = product.counter,
                date = product.date,
                price = product.price,
                isActive = product.isActive,
                oldPrice = product.oldPrice
            ) }
            productApi.moves(products = requests)
        }
    }



    private fun changeStatusProduct() {
        val product = viewState.value.productUpdated.value
        if (product != null)
        updateViewState { it.copy(productUpdated = MutableStateFlow(product.copy(isActive = !product.isActive))) }
    }

    private fun getDataProducts() {
        launchCoroutine {
            updateViewState { it.copy(isLoadData = MutableStateFlow(false)) }
            val user = sharedViewModel.viewState.value.user.value
            if (user != null) {
                val response = productApi.getProducts(idFactory = user.idFactory)
                if (response.success) {
                    val products = response.obj
                    if (!products.isNullOrEmpty()) {
                        updateViewState {
                            it.copy(
                                listProduct = MutableStateFlow(products),
                                isLoadData = MutableStateFlow(true)
                            )
                        }
                    } else {
                        updateViewState {
                            it.copy(
                                listProduct = MutableStateFlow(emptyList()),
                                isLoadData = MutableStateFlow(true)
                            )
                        }
                    }
                } else {
                    sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun showAddDialog() {
        updateViewState { it.copy(showAddSheetDialog = true) }
    }

    private fun showUpdateDialog(product: ProductModel) {
        updateViewState {
            it.copy(
                productUpdated = MutableStateFlow(product),
                showUpdateSheetDialog = true,
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
                    getDataProducts()
                    dismissDeleteDialog()
                } else {
                    sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
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
                productUpdated = MutableStateFlow(null),
                itemPrice = Constants.EMPTY.EMPTY_STRING,
                itemName = Constants.EMPTY.EMPTY_STRING
            )
        }
    }

    private fun changeName(name: String) {
        updateViewState { it.copy(productUpdated = MutableStateFlow(viewState.value.productUpdated.value?.copy(name = name))) }
    }

    private fun changePrice(price: String) {
        updateViewState {
            it.copy(
                productUpdated = MutableStateFlow(
                    viewState.value.productUpdated.value?.copy(
                        price = price.toDouble()
                    )
                )
            )
        }
    }

    private fun updateProduct() {
        launchCoroutine {
            val product = viewState.value.productUpdated.value
            if (product != null) {
                val productRequest = UpdateProductRequest(
                    id = product.id,
                    name = product.name,
                    idFactory = product.idFactory,
                    counter = product.counter,
                    price = product.price,
                    isActive = product.isActive,
                    date = product.date,
                    oldPrice = product.oldPrice
                )
                val response = productApi.update(product = productRequest)
                if (response.success) {
                    getDataProducts()
                    dismissUpdateDialog()
                } else {
                    sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
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
                        getDataProducts()
                        dismissAddDialog()
                    }
                } else {
                    sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
                }

            } else {
                sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            }
        }
    }
}