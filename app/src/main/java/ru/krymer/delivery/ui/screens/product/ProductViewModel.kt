package ru.krymer.delivery.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.repositoryImpl.ProductRepositoryImpl
import ru.krymer.delivery.data.request.ProductRequest
import ru.krymer.delivery.ui.screens.product.models.ProductEvent
import ru.krymer.delivery.ui.screens.product.models.ProductViewState
import ru.krymer.delivery.ui.screens.route.models.RouteEvent
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepositoryImpl,
    private val sharedViewModel: SharedViewModel
) : ViewModel() {

    private val _events = MutableSharedFlow<ProductEvent>(extraBufferCapacity = 64)

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
                _events.emit(ProductEvent.Error(Constants.ERROR.CANCEL_OPERATION))
            } catch (e: TimeoutCancellationException) {
                throw e
                _events.emit(ProductEvent.Error(Constants.ERROR.TIMEOUT))
            } catch (e: Exception) {
                throw e
                _events.emit(ProductEvent.Error(e.message))
            }
        }
    }

    val viewState: StateFlow<ProductViewState> = _events
        .onStart {
            emit(ProductEvent.RefreshProducts)
        }
        .runningFold(ProductViewState()) { state, event ->
            when (event) {
                ProductEvent.ChangeStatusProduct -> {
                    val product = state.product
                    if (product != null) state.copy(product = product.copy(isActive = !product.isActive)) else state
                }

                ProductEvent.CreateProduct -> {
                    launchCoroutine { createProduct() }
                    state
                }

                ProductEvent.DeleteProduct -> {
                    launchCoroutine { deleteProduct() }
                    state
                }

                ProductEvent.RefreshProducts -> {
                    launchCoroutine { loadProducts() }
                    state.copy(isLoading = true)
                }

                ProductEvent.UpdateProduct -> {
                    launchCoroutine { updateProduct() }
                    state
                }

                is ProductEvent.ChangedNameProduct -> state.copy(itemName = event.name)
                is ProductEvent.ChangedPriceProduct -> state.copy(itemPrice = event.price)
                is ProductEvent.ProductsLoaded -> state.copy(products = event.products, isLoading = false)
                is ProductEvent.ReorderProducts -> {
                    launchCoroutine { reorderProducts(event.list) }
                    state
                }

                ProductEvent.ToggleAddDialog -> state.copy(toggleAddDialog = !state.toggleAddDialog)
                is ProductEvent.ToggleDeleteDialog -> state.copy(product = event.product, toggleDeleteDialog = !state.toggleDeleteDialog)
                is ProductEvent.ToggleUpdateDialog -> state.copy(product = event.product, toggleUpdateDialog = !state.toggleUpdateDialog)

                is ProductEvent.Error -> {
                    sharedViewModel.message(event.message, type = TypeMessageModel.ERROR)
                    state.copy(isLoading = false)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductViewState())

    fun obtainEvent(event: ProductEvent) {
        _events.tryEmit(event)
    }

    private suspend fun loadProducts() {
        val user = sharedViewModel.viewState.value.user ?: run {
            _events.emit(ProductEvent.Error(Constants.ERROR.AGAIN))
            return
        }

        when (val res = repository.getProducts(idFactory = user.idFactory)) {
            is MyResult.Success -> _events.emit(ProductEvent.ProductsLoaded(products = res.data))
            is MyResult.Error -> _events.emit(ProductEvent.Error(message = res.message))
        }
    }

    private suspend fun deleteProduct() {
        val product = viewState.value.product ?: return

        when (val res = repository.deleteProduct(product.id)) {
            is MyResult.Success -> {
                _events.emit(ProductEvent.RefreshProducts)
                _events.emit(ProductEvent.ToggleDeleteDialog(product = null))
            }

            is MyResult.Error -> _events.emit(ProductEvent.Error(message = res.message))
        }
    }

    private suspend fun updateProduct() {
        val product = viewState.value.product ?: return
        val name = viewState.value.itemName.ifBlank { product.name }
        val price = viewState.value.itemPrice.ifBlank { product.price.toString() }.toDouble()

        val request = ProductRequest(
            id = product.id,
            name = name,
            idFactory = product.idFactory,
            counter = product.counter,
            price = price,
            isActive = product.isActive,
            date = product.date,
            oldPrice = product.oldPrice
        )

        when (val res = repository.updateProduct(product = request)) {
            is MyResult.Success -> {
                _events.emit(ProductEvent.RefreshProducts)
                _events.emit(ProductEvent.ToggleUpdateDialog(product = null))
            }

            is MyResult.Error -> _events.emit(ProductEvent.Error(message = res.message))
        }
    }

    private suspend fun createProduct() {
        val user = sharedViewModel.viewState.value.user ?: run {
            _events.emit(ProductEvent.Error(Constants.ERROR.AGAIN))
            return
        }

        val name = viewState.value.itemName.ifBlank { "Продукт #${viewState.value.products.lastOrNull()?.counter?.plus(1) ?: 0}" }
        val price = viewState.value.itemPrice.ifBlank { "0" }.toDouble()

        val request = ProductRequest(
            name = name,
            idFactory = user.idFactory,
            counter = viewState.value.products.size,
            price = price,
            isActive = true,
            date = System.currentTimeMillis(),
            oldPrice = 0.0,
        )

        when (val res = repository.addProduct(product = request)) {
            is MyResult.Success -> {
                _events.emit(ProductEvent.RefreshProducts)
                _events.emit(ProductEvent.ToggleAddDialog)
            }

            is MyResult.Error -> _events.emit(ProductEvent.Error(message = res.message))
        }
    }

    private suspend fun reorderProducts(products: List<ProductModel>) {
        val requests = products.map { product ->
            ProductRequest(
                id = product.id,
                idFactory = product.idFactory,
                name = product.name,
                counter = product.counter,
                date = product.date,
                price = product.price,
                isActive = product.isActive,
                oldPrice = product.oldPrice
            )
        }

        when (val res = repository.moves(requests = requests)) {
            is MyResult.Success -> Unit
            is MyResult.Error -> _events.emit(ProductEvent.Error(message = res.message))
        }
    }
}