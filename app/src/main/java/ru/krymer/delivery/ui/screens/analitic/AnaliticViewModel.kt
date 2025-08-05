package ru.krymer.delivery.ui.screens.analitic

import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.ehsannarmani.compose_charts.models.Bars
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.AnaliticApi
import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.api.LoggerApi
import ru.krymer.delivery.data.api.TripApi
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.request.DateRequest
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticAction
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticEvent
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.colorChangerDay
import ru.krymer.delivery.utills.colorChangerMonth
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AnaliticViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel,
    private val logApi: LoggerApi,
    private val analiticApi: AnaliticApi,
    private val clientApi: ClientApi,
    private val tripApi: TripApi
) : ViewModel(), EventHandler<AnaliticEvent> {

    private val _viewState = MutableStateFlow(AnaliticViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateViewState(update: (AnaliticViewState) -> AnaliticViewState) {
        _viewState.update { update(it) }
    }

    override fun obtainEvent(event: AnaliticEvent) {
        when (event) {
            is AnaliticEvent.AnaliticActionInvoked -> actionInvoked()
            is AnaliticEvent.AllInfoClickedToOpen -> showAnaliticFactoryView()
            is AnaliticEvent.ClientInfoClickedToOpen -> showAnaliticClientView()
            is AnaliticEvent.TripInfoClickedToOpen -> showAnaliticTripView()
            is AnaliticEvent.ShowLogView -> showLogView()
            is AnaliticEvent.ChangeRangeDatePicker -> changeRangeDate(pair = event.pair)
            is AnaliticEvent.DismissDatePicker -> dismissDatePicker()
            is AnaliticEvent.ShowDatePicker -> showDatePicker()
            is AnaliticEvent.ChangeStateDropDownMenuClients -> changeStateDropDownMenuClients(event.isShow)
            is AnaliticEvent.SetCurrentClient -> setCurrentClient(client = event.client)
            is AnaliticEvent.SetCurrentTrip -> setCurrentTrip(trip = event.trip)
            AnaliticEvent.DeleteLogs -> deleteLogs()
        }
    }

    private fun deleteLogs() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user.value
            user?.let {
                val response = logApi.deleteLogs(idFactory = user.idFactory)
                if (response.success) {
                    updateViewState { it.copy(logs = MutableStateFlow(emptyList())) }
                }
            }
        }
    }

    private fun setCurrentTrip(trip: TripModel) {
        updateViewState { it.copy(currentTrip = MutableStateFlow(trip)) }
        launchCoroutine { loadDataTrip(trip = trip) }
    }

    private suspend fun loadDataTrip(trip: TripModel) {
        val result = analiticApi.getTripAnalitic(idRoute = trip.idRoute)
        if (result.success) {
            val data = result.obj
            if (data != null) {
                updateViewState {
                    it.copy(
                        data = MutableStateFlow(data),
                        isLoadTripData = true,
                        requests = MutableStateFlow(data.requests),
                    )
                }
                convertDataTripToBars()
            }
        }
    }




    private fun showAnaliticTripView() {
        updateViewState {
            it.copy(
                analiticAction = AnaliticAction.OpenTrip,
                requests = MutableStateFlow(listOf()),
                bars = MutableStateFlow(
                    listOf()
                )
            )
        }
        launchCoroutine { getTrips() }
    }

    private suspend fun getTrips() {
        val user = sharedViewModel.viewState.value.user.value
        if (user != null) {
            val result = tripApi.getRoutesByTrip(idFactory = user.idFactory)
            if (result.success) {
                val routes = result.obj
                if (!routes.isNullOrEmpty()) {
                    updateViewState {
                        it.copy(
                            trips = MutableStateFlow(routes),
                            currentTrip = MutableStateFlow(routes[0])
                        )
                    }
                }
            }
        } else {
            sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
        }
    }

    private fun setCurrentClient(client: ClientModel) {
        updateViewState { it.copy(currentClient = MutableStateFlow(client)) }
        launchCoroutine { loadDataClient(client = client) }
    }

    private suspend fun loadDataClient(client: ClientModel) {
        val result = analiticApi.getClientAnalitic(idClient = client.id)
        if (result.success) {
            val data = result.obj
            if (data != null) {
                updateViewState {
                    it.copy(
                        data = MutableStateFlow(data),
                        isLoadClientData = true,
                        requests = MutableStateFlow(data.requests),
                    )
                }
                convertDataClientToBars()
            }
        }

    }

    private fun changeStateDropDownMenuClients(isShow: Boolean) {
        updateViewState { it.copy(stateDropDownMenuClients = isShow) }
    }

    private fun showAnaliticClientView() {
        updateViewState {
            it.copy(
                analiticAction = AnaliticAction.OpenClient,
                requests = MutableStateFlow(listOf()),
                bars = MutableStateFlow(
                    listOf()
                )
            )
        }
        launchCoroutine { getClients() }
    }

    private suspend fun getClients() {
        val user = sharedViewModel.viewState.value.user.value
        if (user != null) {
            val result = clientApi.getClientsByFactory(idFactory = user.id)
            if (result.success) {
                val clients = result.obj
                if (!clients.isNullOrEmpty()) {
                    updateViewState {
                        it.copy(
                            clients = MutableStateFlow(clients),
                            currentClient = MutableStateFlow(clients[0])
                        )
                    }
                }
            }
        } else {
            sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
        }
    }

    private fun showDatePicker() {
        updateViewState { it.copy(isShowDatePicker = true) }
    }

    private fun changeRangeDate(pair: Pair<Long, Long>) {
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = pair.first
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = startCalendar.timeInMillis
        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = pair.second
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endOfDay = endCalendar.timeInMillis
        updateViewState {
            it.copy(
                dateRangeForSearch = Pair(startOfDay, endOfDay)
            )
        }
        launchCoroutine {
            when(viewState.value.analiticAction) {
                AnaliticAction.None -> {}
                AnaliticAction.OpenAll -> loadDataFactoryOfDateRange()
                AnaliticAction.OpenClient -> loadDataClientOfDateRange()
                AnaliticAction.OpenLog -> loadLogsOfDateRange()
                AnaliticAction.OpenTrip -> loadDatTripOfDateRange()
            }
        }
    }

    private fun loadLogsOfDateRange() {
        launchCoroutine {
            val date = viewState.value.dateRangeForSearch
            val user = sharedViewModel.viewState.value.user.value
            user?.let {
                val response = logApi.getLogsOfRange(dateRange = DateRequest(
                    dateStart = date.first, dateEnd = date.second, id = user.id
                ))
                if (response.success) {
                    val logs = response.obj
                    if (logs.isNullOrEmpty()) {
                        sharedViewModel.message(Constants.ERROR.LIST_EMPTY)
                    } else {
                        updateViewState {
                            it.copy(
                                logs = MutableStateFlow(logs), isLoadLogs = true
                            )
                        }
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.SERVER_ERROR_RESPONSE)
                }
            }
        }
    }

    private fun dismissDatePicker() {
        updateViewState { it.copy(isShowDatePicker = false) }
    }

    private fun showAnaliticFactoryView() {
        updateViewState {
            it.copy(
                analiticAction = AnaliticAction.OpenAll,
                requests = MutableStateFlow(listOf()),
                bars = MutableStateFlow(
                    listOf()
                )
            )
        }
    }

    private fun showLogView() {
        updateViewState {
            it.copy(
                analiticAction = AnaliticAction.OpenLog
            )
        }
        launchCoroutine { getLogData() }
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

    private suspend fun getLogData() {
        val user = sharedViewModel.viewState.value.user.value
        user?.let {
            val result = logApi.getLogs(idFactory = user.idFactory)
            if (result.success) {
                val logs = result.obj
                if (logs.isNullOrEmpty()) {
                    sharedViewModel.message(Constants.ERROR.LIST_EMPTY)
                } else {
                    updateViewState {
                        it.copy(
                            logs = MutableStateFlow(logs), isLoadLogs = true
                        )
                    }
                }
            } else {
                sharedViewModel.message(Constants.ERROR.SERVER_ERROR_RESPONSE)
            }
        }
    }


    private suspend fun loadDataFactoryOfDateRange() {
        val date = viewState.value.dateRangeForSearch
        val response = analiticApi.getDataFactoryOfRange(
            dateRange = DateRequest(
                dateStart = date.first, dateEnd = date.second
            )
        )
        if (response.success) {
            val data = response.obj
            data?.let {
                updateViewState {
                    it.copy(
                        data = MutableStateFlow(data),
                        isLoadDataFactoryInRangeDate = true,
                        requests = MutableStateFlow(data.requests),
                    )
                }
                convertDataFactoryToBars()
            }
        }
    }

    private suspend fun loadDatTripOfDateRange() {
        val date = viewState.value.dateRangeForSearch
        val trip = viewState.value.currentTrip
        trip?.let {
            val response = analiticApi.getDataTripOfRange(
                dateRange = DateRequest(
                    dateStart = date.first, dateEnd = date.second, id = trip.value.idRoute
                )
            )
            if (response.success) {
                val data = response.obj
                data?.let {
                    updateViewState {
                        it.copy(
                            data = MutableStateFlow(data),
                            isLoadTripData = true,
                            requests = MutableStateFlow(data.requests),
                        )
                    }
                    convertDataFactoryToBars()
                }
            }
        }
    }

    private suspend fun loadDataClientOfDateRange() {
        val date = viewState.value.dateRangeForSearch
        val client = viewState.value.currentClient
        client?.let {
            val response = analiticApi.getDataClientOfRange(
                dateRange = DateRequest(
                    dateStart = date.first, dateEnd = date.second, id = client.value.id
                )
            )
            if (response.success) {
                val data = response.obj
                data?.let {
                    updateViewState {
                        it.copy(
                            data = MutableStateFlow(data),
                            isLoadClientData = true,
                            requests = MutableStateFlow(data.requests),
                        )
                    }
                    convertDataFactoryToBars()
                }
            }
        }
    }

    private fun convertDataFactoryToBars() {
        val data = viewState.value.data.value.bars
        val bars = mutableListOf<Bars>()
        data.forEach { item ->
            val bar = Bars(
                label = item.title.substringBefore(".") + "\n" + item.title.substringAfter(".")
                    .substringBefore(".") + "\n" + item.day, values = listOf(
                    Bars.Data(
                        value = item.value,
                        color = Brush.verticalGradient(colorChangerDay(day = item.day))
                    )
                )
            )
            bars.add(bar)
        }
        updateViewState {
            it.copy(
                bars = MutableStateFlow(bars)
            )
        }
    }

    private fun convertDataClientToBars() {
        val data = viewState.value.data.value.bars
        val bars = mutableListOf<Bars>()
        data.forEach { item ->
            val year = item.title.substringAfter(".")
            val month = item.title.substringBefore(".")
            val bar = Bars(
                label = month + "\n" + year.substring(year.length - 2), values = listOf(
                    Bars.Data(
                        value = item.value,
                        color = Brush.verticalGradient(colorChangerMonth(month = item.day))
                    )
                )
            )
            bars.add(bar)
        }
        updateViewState { it.copy(bars = MutableStateFlow(bars)) }
    }

    private fun convertDataTripToBars() {
        val data = viewState.value.data.value.bars
        val bars = mutableListOf<Bars>()
        data.forEach { item ->
            val year = item.title.substringAfter(".")
            val month = item.title.substringBefore(".")
            val bar = Bars(
                label = month + "\n" + year.substring(year.length - 2), values = listOf(
                    Bars.Data(
                        value = item.value,
                        color = Brush.verticalGradient(colorChangerMonth(month = item.day))
                    )
                )
            )
            bars.add(bar)
        }
        updateViewState {
            it.copy(
                bars = MutableStateFlow(bars)
            )
        }
    }

    private fun actionInvoked() {
        _viewState.value = viewState.value.copy(analiticAction = AnaliticAction.None)
    }
}