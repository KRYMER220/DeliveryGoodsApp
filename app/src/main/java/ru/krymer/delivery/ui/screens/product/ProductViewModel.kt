package ru.krymer.delivery.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.repositoryImpl.ProductRepositoryImpl
import ru.krymer.delivery.data.request.ProductRequest
import ru.krymer.delivery.ui.screens.product.models.ProductEvent
import ru.krymer.delivery.ui.screens.product.models.ProductViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepositoryImpl,
    private val sharedViewModel: SharedViewModel
) : ViewModel(), EventHandler<ProductEvent> {

    private val _viewState = MutableStateFlow(ProductViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateState(update: (ProductViewState) -> ProductViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
                sharedViewModel.message(
                    Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR
                )
            } catch (e: TimeoutCancellationException) {
                throw e
                sharedViewModel.message(Constants.ERROR.TIMEOUT, type = TypeMessageModel.ERROR)
            } catch (e: Exception) {
                throw e
                sharedViewModel.message(e.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    override fun obtainEvent(event: ProductEvent) {
        when (event) {
            is ProductEvent.ToggleUpdateDialog -> setState(product = event.product, update = true)
            is ProductEvent.CreateProduct -> createProduct()
            is ProductEvent.ToggleAddDialog -> setState(add = true)
            is ProductEvent.ToggleDeleteDialog -> setState(delete = true, product = event.product)
            is ProductEvent.UpdateProduct -> updateProduct()
            is ProductEvent.ChangedNameProduct -> setValue(name = event.name)
            is ProductEvent.ChangedPriceProduct -> setValue(price = event.price)
            is ProductEvent.ChangeStatusProduct -> {
                viewState.value.product?.let { p ->
                    updateState { it.copy(product = p.copy(isActive = !p.isActive)) }
                }
            }
            is ProductEvent.DeleteProduct -> deleteProduct()
            is ProductEvent.ReorderProducts -> reorderProducts(list = event.list)
        }
    }

    init {
        getProducts()
    }

    private fun setState(
        product: ProductModel? = viewState.value.product,
        add: Boolean = viewState.value.toggleAddDialog,
        update: Boolean = viewState.value.toggleUpdateDialog,
        delete: Boolean = viewState.value.toggleDeleteDialog,

    ) {
        updateState { it.copy(
            product = product,
            toggleAddDialog = add,
            toggleUpdateDialog = update,
            toggleDeleteDialog = delete,

        ) }
    }

    private fun reorderProducts(list: List<ProductModel>) = launchCoroutine {
        val requests = list.map { product -> ProductRequest(
            id = product.id,
            idFactory = product.idFactory,
            name = product.name,
            counter = product.counter,
            date = product.date,
            price = product.price,
            isActive = product.isActive,
            oldPrice = product.oldPrice
        ) }
        when (val res = repository.moves(requests = requests)) {
            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
            is MyResult.Success -> {}
        }
    }

    private fun getProducts() = launchCoroutine {
        updateState { it.copy(isLoading = true) }
        val user = sharedViewModel.viewState.value.user
        if (user == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }
        when(val res = repository.getProducts(idFactory = user.idFactory)) {
            is MyResult.Success -> updateState { it.copy(products = res.data, isLoading = false) }
            is MyResult.Error -> {
                updateState { it.copy(isLoading = false) }
                sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    fun deleteProduct() = launchCoroutine {
        val product = viewState.value.product
        if (product == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        when (val res = repository.deleteProduct(product.id)) {
            is MyResult.Success -> {
                getProducts()
                setState(delete = false, product = null)
            }

            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }

    private fun setValue(
        name: String = viewState.value.itemName,
        price: String = viewState.value.itemPrice,
        product: ProductModel? = viewState.value.product
    ) {
        updateState { it.copy(
            itemName = name,
            itemPrice = price,
        ) }

        if (product != null) {
            updateState { it.copy(product = product.copy(price = if (price == "") product.price else price.toDouble(), name = name.ifBlank { product.name })) }
        }
    }

    private fun updateProduct() = launchCoroutine {
        val product = viewState.value.product
        if (product == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }
        val request = ProductRequest(
            id = product.id,
            name = product.name,
            idFactory = product.idFactory,
            counter = product.counter,
            price = product.price,
            isActive = product.isActive,
            date = product.date,
            oldPrice = product.oldPrice
        )

        when (val res = repository.updateProduct(product = request)) {
            is MyResult.Success -> {
                getProducts()
                setState(update = false, product = null)
                setValue(name = "", price = "")
            }
            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }

    private fun createProduct() = launchCoroutine {
        val name = viewState.value.itemName
        val price = viewState.value.itemPrice
        val user = sharedViewModel.viewState.value.user

        if (user == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        val request = ProductRequest(
            name = name,
            idFactory = user.idFactory,
            counter = viewState.value.products.size,
            price = if (price == "") 0.0 else price.toDouble(),
            isActive = true,
            date = System.currentTimeMillis(),
            oldPrice = 0.0,
        )

        when(val res = repository.addProduct(product = request)) {
            is MyResult.Success -> {
                getProducts()
                setState(add = false)
                setValue(name = "", price = "")
            }
            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }
}