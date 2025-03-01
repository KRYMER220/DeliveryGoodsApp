package ru.krymer.delivery.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    override fun obtainEvent(event: ProductEvent) {
        when (event) {
            is ProductEvent.ProductItemClicked -> showUpdateDialog(product = event.product)
            is ProductEvent.ProductSaveAction -> saveProduct()
            is ProductEvent.ShowAddDialog -> showAddDialog()
            is ProductEvent.ShowDeleteDialog -> showDeleteDialog(
                itemId = event.itemID, itemName = event.itemName
            )

            ProductEvent.ProductUpdateAction -> updateProduct()
            is ProductEvent.ChangedNameProduct -> changeName(event.name)
            is ProductEvent.ChangedPriceProduct -> changePrice(event.price)
            ProductEvent.DismissAddDialog -> dismissAddDialog()
            ProductEvent.DismissDeleteDialog -> dismissDeleteDialog()
            ProductEvent.DismissUpdateDialog -> dismissUpdateDialog()
            ProductEvent.ChangeIsActiveProduct -> changeStatusProduct()
        }
    }

    private fun changeStatusProduct() {
        val status = viewState.value.isActiveProduct ?: true
        updateViewState { it.copy(isActiveProduct = !status) }
    }

    init {
        getDataProducts()
    }

    private fun getDataProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = sharedViewModel.viewState.value.user
                if (user != null) {
                    val response = productApi.getCurrentListProduct(idFactory = user.idFactory)
                    if (response.success) {
                        val products = response.obj?.sortedBy { it.price }
                        if (products != null) {
                            updateViewState {
                                it.copy(
                                    listProduct = MutableStateFlow(products),
                                    isLoadDataProduct = true
                                )
                            }
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }

    private fun showAddDialog() {
        changeName()
        changePrice()
        updateViewState { it.copy(showAddSheetDialog = true) }
    }

    private fun showUpdateDialog(product: ProductModel) {
        updateViewState {
            it.copy(
                showUpdateSheetDialog = true,
                itemName = product.name,
                itemPrice = "${product.price}",
                productUpdated = product,
                isActiveProduct = product.isActive
            )
        }
    }

    private fun showDeleteDialog(itemId: Long, itemName: String) {
        updateViewState {
            it.copy(
                showDeleteDialog = true, itemIdToDelete = itemId, itemNameToDelete = itemName
            )
        }
    }

    fun deleteItemConfirmed() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val itemId = viewState.value.itemIdToDelete
                if (itemId != null) {
                    val response = productApi.deleteProduct(idProduct = itemId)
                    if (response.success) {
                        val list =
                            viewState.value.listProduct.value.map { it.copy() }.toMutableList()
                        val item = list.first { it.id == itemId }
                        val listNew = list - item
                        updateViewState { it.copy(listProduct = MutableStateFlow(listNew.sortedBy { p -> p.price })) }
                        sharedViewModel.message(
                            response.message,
                            typeMessageModel = TypeMessageModel.SUCCEED
                        )
                    } else {
                        sharedViewModel.message(response.message)
                    }
                }

            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            } finally {
                dismissDeleteDialog()
            }
        }
    }

    private fun dismissDeleteDialog() {
        updateViewState {
            it.copy(
                showDeleteDialog = false,
                itemIdToDelete = null,
                itemNameToDelete = Constants.EMPTY.EMPTY_STRING
            )
        }
    }

    private fun dismissAddDialog() {
        updateViewState {
            it.copy(
                showAddSheetDialog = false,
                isErrorName = false,
                isErrorPrice = false,
                itemPrice = Constants.EMPTY.EMPTY_STRING,
                itemName = Constants.EMPTY.EMPTY_STRING
            )
        }
    }

    private fun dismissUpdateDialog() {
        updateViewState {
            it.copy(
                showUpdateSheetDialog = false,
                isErrorName = false,
                isErrorPrice = false,
                productUpdated = null,
                isActiveProduct = null,
                itemPrice = Constants.EMPTY.EMPTY_STRING,
                itemName = Constants.EMPTY.EMPTY_STRING
            )
        }
    }

    private fun changeName(name: String = Constants.EMPTY.EMPTY_STRING) {
        if (isEmptyInput(name)) {
            updateViewState { it.copy(itemName = name, isErrorName = false) }
        } else {
            updateViewState {
                it.copy(
                    isErrorName = true,
                    errorName = "${Constants.EMPTY.FIELD_IMPORTANT} ${Constants.EMPTY.EMPTY_NAME}",
                    itemName = name
                )
            }
        }
    }

    private fun changePrice(price: String = Constants.EMPTY.EMPTY_STRING) {
        if (isEmptyInput(price)) {
            updateViewState { it.copy(itemPrice = price, isErrorPrice = false) }
        } else {
            updateViewState {
                it.copy(
                    isErrorPrice = true,
                    errorPrice = "${Constants.EMPTY.FIELD_IMPORTANT} ${Constants.EMPTY.EMPTY_PRICE}",
                    itemPrice = price
                )
            }
        }
    }

    private fun updateProduct() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val product = viewState.value.productUpdated
                val name = viewState.value.itemName
                val price = viewState.value.itemPrice.toDouble()
                val isActive = viewState.value.isActiveProduct ?: true
                if (!viewState.value.isErrorName && !viewState.value.isErrorPrice && product != null) {
                    val productRequest = UpdateProductRequest(
                        id = product.id,
                        name = name,
                        idFactory = product.idFactory,
                        counter = product.counter,
                        price = price ?: 0.0,
                        isActive = isActive,
                        date = product.date,
                        oldPrice = product.price
                    )
                    val response = productApi.updateProduct(product = productRequest)
                    if (response.success) {
                        val list =
                            viewState.value.listProduct.value.map { it.copy() }.toMutableList()
                        val index = list.indexOfFirst { it.id == product.id }
                        list[index] = product.copy(
                            name = name, isActive = isActive, price = price ?: 0.0
                        )
                        updateViewState { it.copy(listProduct = MutableStateFlow(list)) }
                        sharedViewModel.message(
                            response.message,
                            typeMessageModel = TypeMessageModel.SUCCEED
                        )
                    } else {
                        sharedViewModel.message(response.message)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            } finally {
                dismissUpdateDialog()
            }
        }
    }

    private fun saveProduct() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val name = viewState.value.itemName
                val price = viewState.value.itemPrice
                val user = sharedViewModel.viewState.value.user

                if (price.isNotEmpty() && name.isNotEmpty() && user != null) {
                    val productRequest = CreateProductRequest(
                        name = name,
                        idFactory = user.idFactory,
                        counter = viewState.value.listProduct.value.size,
                        price = price.toDouble(),
                        isActive = true,
                        date = System.currentTimeMillis(),
                        oldPrice = price.toDouble(),
                    )

                    val response = productApi.addProduct(product = productRequest)
                    if (response.success) {
                        val product = response.obj
                        if (product != null) {
                            val list =
                                viewState.value.listProduct.value.map { it.copy() }.toMutableList()
                            list.add(product)
                            updateViewState { it.copy(listProduct = MutableStateFlow(list.sortedBy { p -> p.price })) }
                            sharedViewModel.message(
                                response.message,
                                typeMessageModel = TypeMessageModel.SUCCEED
                            )
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }

                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            } finally {
                dismissAddDialog()
            }
        }
    }
}