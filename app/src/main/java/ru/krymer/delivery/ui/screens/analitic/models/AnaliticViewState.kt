package ru.krymer.delivery.ui.screens.analitic.models

import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.LoggerModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.response.Client
import ru.krymer.delivery.data.response.Factory
import ru.krymer.delivery.data.response.Trip
import ru.krymer.delivery.utills.getCurrentDayRangeTimestamps

sealed class AnaliticAction {
    data object OpenAll : AnaliticAction()
    data object OpenTrip : AnaliticAction()
    data object OpenClient : AnaliticAction()
    data object OpenLog : AnaliticAction()
    data object None : AnaliticAction()
}


data class AnaliticViewState(
    val analiticAction: AnaliticAction = AnaliticAction.None,

    val logs: MutableStateFlow<List<LoggerModel>> = MutableStateFlow(listOf()),
    val isLoadLogs: Boolean = false,

    val dateRangeForSearch: Pair<Long, Long> = getCurrentDayRangeTimestamps(),
    val isShowDatePicker: Boolean = false,

    val isLoadDataFactoryInRangeDate: Boolean = false,
    val requests: MutableStateFlow<List<RequestModel>> = MutableStateFlow(listOf()),
    val bars: MutableStateFlow<List<Bars>> = MutableStateFlow(listOf()),
    val lines: MutableStateFlow<List<Line>> = MutableStateFlow(listOf()),
    val allDataFactoryOfDateRange: MutableStateFlow<Factory> = MutableStateFlow(
        Factory()
    ),

    val isLoadClientData: Boolean = false,
    val isLoadAnaliticClient: Boolean = false,
    val clients: MutableStateFlow<List<ClientModel>> = MutableStateFlow(listOf()),
    val currentClient: MutableStateFlow<ClientModel>? = null,
    val stateDropDownMenuClients: Boolean = false,
    val allDataClient: MutableStateFlow<Client> = MutableStateFlow(
        Client()
    ),

    val isLoadTripData: Boolean = false,
    val isLoadAnaliticTrip: Boolean = false,
    val trips: MutableStateFlow<List<TripModel>> = MutableStateFlow(listOf()),
    val currentTrip: MutableStateFlow<TripModel> ?= null,
    val stateDropDownMenuTrip: Boolean = false,
    val allDataTrip: MutableStateFlow<Trip> = MutableStateFlow(
        Trip()
    ),


    )