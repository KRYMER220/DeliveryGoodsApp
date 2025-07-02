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
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticEvent
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@Composable
fun AnaliticClientView(state: AnaliticViewState, event: (AnaliticEvent) -> Unit) {

    val requests = state.requests.collectAsState().value


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        item {
            CustomDropDownMenuClient(viewState = state, changerState = { state ->
                event(
                    AnaliticEvent.ChangeStateDropDownMenuClients(isShow = state)
                )
            }, setCurrentClient = { client ->
                event(
                    AnaliticEvent.SetCurrentClient(client = client)
                )
            })
        }
        if (state.isLoadClientData) {
            item {
                TextClientInformation(viewState = state)
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
                BarsView(viewState = state)
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
fun TextClientInformation(viewState: AnaliticViewState) {
    val client = viewState.currentClient?.collectAsState()?.value
    val dataClient = viewState.data.collectAsState().value
    if (client != null) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextItemInfo(value = "Имя: ${client.name}")
            TextItemInfo(value = "Телефон: ${client.phone}")
            TextItemInfo(value = "Дата создания: ${convertToTextDate(client.date)}")
            TextItemInfo(value = "Долг: ${client.arrears}")
            TextItemInfo(value = "Стоимость доп.продукции: ${dataClient.otherProductMoney}")
            TextItemInfo(value = "Выручка: ${dataClient.money}")
            TextItemInfo(value = "Наличные: ${dataClient.cash}")
            TextItemInfo(value = "Без/нал: ${dataClient.noCash}")
            TextItemInfo(value = "Сумма возвратов: ${dataClient.exchange} ${dataClient.perSumExchange}%")
        }
    }
}

@Composable
fun CustomDropDownMenuClient(
    viewState: AnaliticViewState,
    changerState: (Boolean) -> Unit,
    setCurrentClient: (ClientModel) -> Unit
) {
    val list = viewState.clients.collectAsState().value
    if (list.isNotEmpty()) {
        val client = viewState.currentClient!!.collectAsState().value
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
                text = client.name,
                modifier = Modifier.padding(start = 15.dp),
                color = AppTheme.colors.onSecondary, style = AppTheme.typography.titleMedium
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
                list.forEach { client ->
                    DropdownMenuItem(text = { Text(text = client.name, style = AppTheme.typography.titleSmall) }, onClick = {
                        setCurrentClient(client)
                        changerState(false)
                    })
                }
            }
        }
    }
}