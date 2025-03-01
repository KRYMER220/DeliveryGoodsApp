package ru.krymer.delivery.ui.screens.analitic.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.ui.screens.analitic.AnaliticViewModel
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticEvent
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@Composable
fun AnaliticTripView(viewModel: AnaliticViewModel) {
    val viewState = viewModel.viewState.collectAsState().value
    val requests = viewState.requests.collectAsState().value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        item {
            CustomDropDownMenuTrip(viewState = viewState, changerState = { state ->
                viewModel.obtainEvent(
                    AnaliticEvent.ChangeStateDropDownMenuClients(isShow = state)
                )
            }, setCurrentRoute = { trip ->
                viewModel.obtainEvent(
                    AnaliticEvent.SetCurrentTrip(trip = trip)
                )
            })
        }
        if (viewState.isLoadTripData) {
            item {
                TextTripInformation(viewState = viewState)
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                TitlesForRequestsList()
            }

            item {
                Spacer(modifier = Modifier.height(5.dp))
            }

            items(requests) { request ->
                RequestItems(request = request)
                Spacer(modifier = Modifier.height(5.dp))
            }

            item {
                BarsView(viewState = viewState)
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                }
            }
        }
    }

}

@Composable
fun TextTripInformation(viewState: AnaliticViewState) {
    val route = viewState.currentTrip?.collectAsState()?.value
    val dataTrip = viewState.allDataTrip.collectAsState().value
    if (route != null) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextItemInfo(value = "Имя: ${route.nameRoute}")
            TextItemInfo(value = "Дата создания: ${convertToTextDate(route.date)}")
            TextItemInfo(value = "Стоимость доп.продукции: ${dataTrip.otherProductMoney}")
            TextItemInfo(value = "Выручка: ${dataTrip.money}")
            TextItemInfo(value = "Наличные: ${dataTrip.cash}")
            TextItemInfo(value = "Без/нал: ${dataTrip.noCash}")
            TextItemInfo(value = "Сумма возвратов: ${dataTrip.exchange} ${dataTrip.perSumExchange}%")
        }
    }
}


@Composable
fun CustomDropDownMenuTrip(
    viewState: AnaliticViewState,
    changerState: (Boolean) -> Unit,
    setCurrentRoute: (TripModel) -> Unit
) {
    val list = viewState.trips.collectAsState().value
    if (list.isNotEmpty()) {
        val trip = viewState.currentTrip!!.collectAsState().value
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                    changerState(true)
                }, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = trip.nameRoute,
                modifier = Modifier.padding(start = 15.dp),
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.padding(end = 15.dp)
            )
            DropdownMenu(expanded = viewState.stateDropDownMenuClients, onDismissRequest = {
                changerState(false)
            }) {
                list.forEach { trip ->
                    DropdownMenuItem(text = { Text(text = trip.nameRoute) }, onClick = {
                        setCurrentRoute(trip)
                        changerState(false)
                    })
                }
            }
        }
    }
}