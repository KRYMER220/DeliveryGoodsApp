package ru.krymer.delivery.ui.screens.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.api.ProductApi
import ru.krymer.delivery.data.api.RequestApi
import ru.krymer.delivery.data.api.RouteApi
import ru.krymer.delivery.data.api.ShopApi
import ru.krymer.delivery.data.api.TripApi
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.oldModel.ShopOld
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.getStringByTypePay
import ru.krymer.delivery.data.request.CreateTripRequest
import ru.krymer.delivery.data.request.UpdateRequestShopRequest
import ru.krymer.delivery.data.request.UpdateShopRequest
import ru.krymer.delivery.data.request.UpdateTripRequest
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.trip.models.TripAction
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.utills.Constants
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripApi: TripApi,
    private val userApi: UserApi,
    private val clientApi: ClientApi,
    private val routeApi: RouteApi,
    private val sharedViewModel: SharedViewModel,
    private val productApi: ProductApi,
    private val shopApi: ShopApi,
    private val requestApi: RequestApi
) : ViewModel(), EventHandler<TripEvent> {


    private val _viewState = MutableStateFlow(TripViewState())
    val viewState: StateFlow<TripViewState> = _viewState

    private fun updateViewState(update: (TripViewState) -> TripViewState) {
        _viewState.update { update(it) }
    }


    override fun obtainEvent(event: TripEvent) {
        when (event) {
            is TripEvent.ShowAddDialog -> showAddDialog()
            is TripEvent.ShowChangeCourierDialog -> showUpdateDialog(event.trip)
            is TripEvent.ShowDeleteDialog -> showDeleteDialog(event.itemID, event.itemName)
            is TripEvent.TripSaveAction -> saveTrip()
            is TripEvent.ChangeDropDownStateCourier -> changeStateDropMenuCourier(event.state)
            is TripEvent.ChangeDropDownStateTrip -> changeStateDropMenuTrip(event.state)
            is TripEvent.SelectDropDownCourier -> changeCurrentCourier(event.courier)
            is TripEvent.SelectDropDownRoute -> changeCurrentRoute(event.route)
            is TripEvent.ChangeDropDownStateDatePicker -> changeStateDropMenuDatePicker(event.state)
            is TripEvent.ChangeDate -> changeDate(event.date)
            is TripEvent.TripUpdateAction -> updateTrip()
            is TripEvent.TripItemClicked -> shopsItemClicked(event.trip)
            TripEvent.TripActionInvoked -> tripActionInvoked()
            TripEvent.DismissAddDialog -> dismissAddDialog()
            TripEvent.DismissDeleteDialog -> dismissDeleteDialog()
            TripEvent.DismissUpdateDialog -> dismissUpdateDialog()
            TripEvent.DeleteTrip -> deleteTrip()
        }
    }

    init {
        getDataTrips()
        loadListDropMenuRoutes()
        loadListDropMenuCouriers()
       // initData()
    }


    private fun initData() {
        launchCoroutine {
//            tripApi.getCurrentTrips(1L).obj?.forEach { trip ->
//                val request = UpdateTripRequest(
//                    id = trip.id,
//                    factoryId = trip.idFactory,
//                    date = trip.date,
//                    courierId = trip.idCourier,
//                    routeId = trip.idRoute,
//                    salary = trip.salary,
//                    percentCourier = trip.percentCourier,
//                    priceMillage = trip.priceMillage,
//                    millage = trip.millage,
//                    nameCourier = trip.nameCourier,
//                    nameRoute = trip.nameRoute.substringAfter(" "),
//                    salaryCourier = trip.salaryCourier
//                )
//                tripApi.updateTrip(request)
//            }
//            requestApi.getAllRequests(idFactory = 1).obj?.forEach { req ->
//                req.apply {
//                    val update = UpdateRequestShopRequest(
//                        id = id,
//                        idShop = idShop,
//                        idTrip = idTrip,
//                        idFactory = idFactory,
//                        count = count,
//                        exchange = exchange,
//                        bonus = bonus,
//                        status = status,
//                        price = price,
//                        oldPrice = oldPrice,
//                        name = name,
//                        counter = when(id) {
//                            8L -> 2
//                            9L -> 6
//                            10L -> 1
//                            11L -> 0
//                            12L -> 3
//                            13L -> 5
//                            14L -> 4
//                            else -> {7}
//                        }
//                    )
//                    requestApi.updateRequest(update)
//                }
//            }
        }
//        val auth = FirebaseAuth.getInstance()
//        auth.signInWithEmailAndPassword("krymer440@gmail.com", "Gemas2001")
//        if (auth.currentUser != null) {
//            launchCoroutine {
//                val listTrip = tripApi.getCurrentTrips(1L).obj
//                val listRoute = routeApi.getCurrentListRoute(1L).obj
//                if (listTrip != null && listRoute != null) getData(
//                    listTrip = listTrip,
//                    listRoute = listRoute
//                )
//            }
//        }
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

    private fun getData(listTrip: List<TripModel>, listRoute: List<RouteModel>?) {
        val db = FirebaseFirestore.getInstance()
        listTrip.forEachIndexed { i, trip ->

//            launchCoroutine {
//                var sum = 0.0
//                shopApi.getCurrentShops(trip.id).obj?.forEach { shop ->
//                    sum += shop.cash + shop.noCash
//                }
//                val request = UpdateTripRequest(
//                    id = trip.id,
//                    factoryId = 1,
//                    date = trip.date,
//                    courierId = trip.idCourier,
//                    routeId = trip.idRoute,
//                    salary = trip.salary,
//                    percentCourier = trip.percentCourier,
//                    priceMillage = trip.priceMillage,
//                    millage = if (trip.salary != 0.0) (trip.salaryCourier - trip.salary - (sum * trip.percentCourier)) / trip.priceMillage else 0.0,
//                    nameCourier = trip.nameCourier,
//                    nameRoute = trip.nameRoute,
//                    salaryCourier = trip.salaryCourier
//                )
//                tripApi.updateTrip(request)
//            }
//            db.collection("Trip").document("001").collection("List").get().addOnSuccessListener {
//                val trips = it.toObjects<TripOld>()
//                launchCoroutine {
//                    trips.forEach { oldTrip ->
//                        if (trip.nameRoute.substringBefore(" ") == oldTrip.id) {
//                            val request = UpdateTripRequest(
//                                id = trip.id,
//                                factoryId = 1,
//                                date = trip.date,
//                                courierId = trip.idCourier,
//                                routeId = trip.idRoute,
//                                salary = trip.salary,
//                                percentCourier = trip.percentCourier,
//                                priceMillage = trip.priceMillage,
//                                millage = oldTrip.salary - (oldTrip.priceSalary) ,
//                                nameCourier = trip.nameCourier,
//                                nameRoute = trip.nameRoute,
//                                salaryCourier = trip.salaryCourier
//                            )
//                            tripApi.updateTrip(request)
//                        }
//                    }
//                }
//            }


//            launchCoroutine {
//                shopApi.getCurrentShops(trip.id).obj?.forEach { shop ->
//                    db.collection("Trip").document("001").collection("List")
//                        .document(trip.nameRoute.substringBefore(" ")).collection("Shop")
//                        .document(shop.nameShop.substringBefore(" ")).collection("Product").get()
//                        .addOnSuccessListener { snap ->
//                            val requestShop = snap.toObjects<ProductOld>()
//                            requestShop.forEach { req ->
//                                launchCoroutine {
//                                    val request = CreateRequestShopRequest(
//                                        id = when(req.id) {
//                                            "kpyEidUTIljkXvWWgVqH" -> 10L
//                                            "6v35ASpUt9K8DuTfu6ni" -> 13L
//                                            "uZaKG8wwv0gwQYe8oUzq" -> 11L
//                                            "1OMZFY0Fkjeqe7oGZ3CC" -> 8L
//                                            "lvR6gtYwtQb1KOWpGTob" -> 12L
//                                            "iwH4ud1087eZCy3T5zdm" -> 14L
//                                            "3cen0q5cZe8HGB45JUKg" -> 9L
//                                            else -> 0L
//                                        },
//                                        idShop = shop.id,
//                                        idTrip = trip.id,
//                                        idFactory = 1,
//                                        count = req.count,
//                                        exchange = req.exchange,
//                                        bonus = 0,
//                                        status = req.status,
//                                        price = req.price.toDouble(),
//                                        oldPrice = req.priceOld.toDouble(),
//                                        name = req.name
//                                    )
//                                    requestApi.addRequest(request)
//                                }
//                            }
//                        }
//                }
//            }
//            db.collection("Trip").document("001").collection("List")
//                .document(trip.nameRoute.substringBefore(" ")).collection("Shop").get()
//                .addOnSuccessListener { snap ->
//                    val listShop = snap.toObjects<ShopOld>()
//                    launchCoroutine {
//                        listShop.forEachIndexed { i, oldShop ->
//                            val idShopOld = oldShop.id.hashCode().toLong() and 0x7FFFFFFFFFFFFFFFL
//                            val request = UpdateShopRequest(
//                                id = idShopOld,
//                                idTrip = trip.id,
//                                idFactory = 1,
//                                arrears = oldShop.arrears.toDouble(),
//                                addSum = oldShop.addSum.toDouble(),
//                                status = oldShop.ready,
//                                date = oldShop.date,
//                                typePay = if (oldShop.typePay) TypePayModel.NO_CASH.getStringByTypePay() else TypePayModel.CASH.getStringByTypePay(),
//                                cash = oldShop.cash.toDouble(),
//                                counter = oldShop.i.toInt(),
//                                noCash = oldShop.nocash.toDouble(),
//                                isOldPrice = false,
//                                nameShop = oldShop.id + " " + oldShop.name,
//                                cord = oldShop.cords
//                            )
//                            shopApi.updateShop(request)
//                        }
//                    }
//                }

    }

//        db.collection("Shop").document("001").collection("List").get().addOnSuccessListener { snap ->
//            val clients = snap.toObjects<ShopOld>()
//            launchCoroutine {
//                clients.forEachIndexed {i, shop ->
//                    val idShopOld = shop.id.hashCode().toLong() and 0x7FFFFFFFFFFFFFFFL
//                    val idRoute = shop.idRoute.sumOf { it.code }.toLong()
//                    val request = ClientRequest(
//                        id = idShopOld,
//                        idRoute = idRoute,
//                        idFactory = 1,
//                        name = shop.name,
//                        phone = shop.phone,
//                        cord = shop.cords,
//                        counter = shop.i.toInt(),
//                        arrears = shop.arrears.toDouble(),
//                        date = shop.date
//                    )
//                    clientApi.addClient(request)
//                }
//            }
//        }


    }

    private fun getDataTrips() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = sharedViewModel.viewState.value.user
                if (user != null) {
                    val response =
                        tripApi.getCurrentTrips(idFactory = user.idFactory)
                    if (response.success) {
                        val trips = response.obj?.sortedByDescending { it.date }
                        if (trips != null) {
                            updateViewState {
                                it.copy(
                                    listTrip = MutableStateFlow(trips),
                                    isLoadDataTrip = true,
                                    currentDate = System.currentTimeMillis() + 86000000
                                )
                            }
                        } else {
                            sharedViewModel.message(Constants.EMPTY.EMPTY_LIST)
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


    private fun tripActionInvoked() {
        updateViewState { it.copy(tripAction = TripAction.None) }
    }

    private fun updateTrip() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = viewState.value.currentTrip
                val route = viewState.value.currentRoute
                val courier = viewState.value.currentCourier
                val date = viewState.value.currentDate
                if (route != null && courier != null && trip != null) {
                    val tripRequest = UpdateTripRequest(
                        id = trip.id,
                        factoryId = trip.idFactory,
                        date = date,
                        courierId = courier.id,
                        routeId = route.id,
                        salary = trip.salary,
                        percentCourier = courier.percentSalary,
                        priceMillage = trip.priceMillage,
                        millage = trip.millage,
                        nameCourier = courier.name,
                        nameRoute = route.name,
                    )
                    val response = tripApi.updateTrip(trip = tripRequest)
                    if (response.success) {
                        val list = viewState.value.listTrip.value.map { it.copy() }.toMutableList()
                        val index = list.indexOfFirst { it.id == trip.id }
                        list[index] = trip.copy(
                            nameRoute = route.name,
                            nameCourier = courier.name,
                            date = date
                        )
                        updateViewState { it.copy(listTrip = MutableStateFlow(list.sortedBy { l -> l.date })) }
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

    private fun showUpdateDialog(trip: TripModel) {
        val routes = viewState.value.listRoute
        val couriers = viewState.value.listCourier
        updateViewState {
            it.copy(showUpdateSheetDialog = true,
                currentTrip = trip,
                currentDate = trip.date,
                currentCourier = couriers.value.first { c -> c.id == trip.idCourier },
                currentRoute = routes.value.first { r -> r.id == trip.idRoute })
        }
    }

    private fun changeDate(date: Long) {
        updateViewState { it.copy(currentDate = date) }
    }

    private fun changeStateDropMenuDatePicker(state: Boolean) {
        updateViewState { it.copy(dropDownStateDatePicker = state) }
    }

    private fun changeCurrentRoute(route: RouteModel) {
        updateViewState { it.copy(currentRoute = route) }
    }

    private fun changeCurrentCourier(courier: UserModel) {
        updateViewState { it.copy(currentCourier = courier) }
    }

    private fun saveTrip() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val curRoute = viewState.value.currentRoute
                val curCourier = viewState.value.currentCourier
                val factory = sharedViewModel.viewState.value.factory
                val date = viewState.value.currentDate
                if (curRoute != null && curCourier != null && factory != null) {
                    val routeRequest = CreateTripRequest(
                        factoryId = factory.id,
                        date = date,
                        courierId = curCourier.id,
                        routeId = curRoute.id,
                        salary = factory.salary,
                        percentCourier = curCourier.percentSalary,
                        priceMillage = factory.priceMillage,
                        nameRoute = curRoute.name,
                        nameCourier = curCourier.name
                    )
                    val response = tripApi.addTrip(trip = routeRequest)
                    if (response.success) {
                        val trip = response.obj
                        if (trip != null) {
                            val list =
                                viewState.value.listTrip.value.map { it.copy() }.toMutableList()
                            list.add(trip)
                            updateViewState { it.copy(listTrip = MutableStateFlow(list.sortedByDescending { t -> t.date })) }
                        } else {
                            sharedViewModel.message(message = Constants.ERROR.ERROR)
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    sharedViewModel.message(message = Constants.ERROR.ERROR)
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            } finally {
                dismissAddDialog()
            }
        }
    }

    private fun showAddDialog() {
        updateViewState {
            it.copy(
                showAddSheetDialog = true
            )
        }
    }

    private fun loadListDropMenuRoutes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = sharedViewModel.viewState.value.user
                if (user != null) {
                    val responseRoute = routeApi.getCurrentListRoute(user.idFactory)
                    if (responseRoute.success) {
                        val routes = responseRoute.obj
                        if (!routes.isNullOrEmpty()) {
                            val list = routes.sortedBy { r -> r.name }
                            updateViewState {
                                it.copy(
                                    listRoute = MutableStateFlow(list),
                                    isLoadDataDropMenuRoute = true,
                                    currentRoute = list[0]
                                )
                            }
                        } else {
                            sharedViewModel.message(Constants.EMPTY.EMPTY_LIST + Constants.ADD.ROUTE)
                        }
                    } else {
                        sharedViewModel.message(responseRoute.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            }
        }
    }

    private fun loadListDropMenuCouriers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = sharedViewModel.viewState.value.user
                if (user != null) {
                    val responseUsers = userApi.getListUser(user.idFactory)
                    if (responseUsers.success) {
                        val users = responseUsers.obj
                        if (!users.isNullOrEmpty()) {
                            val list = users.sortedBy { r -> r.name }
                            updateViewState {
                                it.copy(
                                    listCourier = MutableStateFlow(list),
                                    isLoadDataDropMenuCourier = true,
                                    currentCourier = list[0]
                                )
                            }
                        } else {
                            sharedViewModel.message(Constants.EMPTY.EMPTY_LIST + Constants.ADD.COURIER)
                        }
                    } else {
                        sharedViewModel.message(responseUsers.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            }
        }
    }


    private fun changeStateDropMenuTrip(state: Boolean) {
        updateViewState { it.copy(dropDownStateTrips = state) }
    }

    private fun changeStateDropMenuCourier(state: Boolean) {
        updateViewState { it.copy(dropDownStateCourier = state) }
    }

    private fun showDeleteDialog(itemId: Long, itemName: String) {
        updateViewState {
            it.copy(
                showDeleteDialog = true, itemIdToDelete = itemId, itemNameToDelete = itemName
            )
        }
    }


    private fun deleteTrip() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val idTrip = viewState.value.itemIdToDelete
                if (idTrip != null) {
                    val response = tripApi.deleteTrip(idTrip = idTrip)
                    if (response.success) {
                        val list = viewState.value.listTrip.value.map { it.copy() }.toMutableList()
                        val item = list.first { it.id == idTrip }
                        val listNew = (list - item).sortedByDescending { it.date }
                        updateViewState { it.copy(listTrip = MutableStateFlow(listNew)) }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
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
                itemNameToDelete = ""
            )
        }
    }

    private fun dismissAddDialog() {
        updateViewState {
            it.copy(
                showAddSheetDialog = false,
                currentDate = Calendar.getInstance().timeInMillis + 86400000
            )
        }
    }

    private fun dismissUpdateDialog() {
        updateViewState {
            it.copy(
                showUpdateSheetDialog = false,
                currentRoute = viewState.value.listRoute.value[0],
                currentCourier = viewState.value.listCourier.value[0],
            )
        }
    }

    private fun shopsItemClicked(trip: TripModel) {
        updateViewState { it.copy(tripAction = TripAction.OpenShops) }
        sharedViewModel.initCurrentTrip(trip)
    }
}