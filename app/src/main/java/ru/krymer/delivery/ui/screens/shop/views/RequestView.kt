package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.components.KeyBoardDialog
import ru.krymer.delivery.ui.screens.shop.ShopViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlertDialogRequestShop(
    viewState: ShopViewState, viewModel: ShopViewModel
) {
    viewState.currentShop?.let { shop ->

        val listMenu = listOf("Долг", "Доп.сумму", "Тип оплаты", "Старая цена", "Добавить заявку")
        var isExpandedMenu by remember { mutableStateOf(false) }
        val requests = viewState.listDataRequests.collectAsState().value
        val orderMoney = viewState.orderMoney.collectAsState().value.toString()
        val stateCash = viewState.getCash.collectAsState().value
        val stateNoCash = viewState.getNoCash.collectAsState().value
        var cash by remember { mutableStateOf("") }
        var noCash by remember { mutableStateOf("") }
        val typePayState = viewState.typePay.collectAsState().value
        val switchOldPrice = viewState.stateSwitchPrice.collectAsState().value

        LaunchedEffect(stateCash) {
            cash = stateCash
        }
        LaunchedEffect(stateNoCash) {
            noCash = stateNoCash
        }

        Column(modifier = Modifier.wrapContentHeight()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, top = 10.dp, end = 5.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.labelSmall,
                    text = shop.nameShop,
                    fontSize = 16.sp,
                    color = AppTheme.colors.onSecondary,
                    modifier = Modifier.weight(0.7f)
                )
                Row(modifier = Modifier.weight(0.3f)) {
                    Image(
                        contentDescription = "confirm",
                        painter = painterResource(id = R.drawable.submit),
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(onClick = {
                                viewModel.obtainEvent(ShopEvent.InitSaveRequestDialog)
                            })
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Row(
                        modifier = Modifier.clickable { isExpandedMenu = !isExpandedMenu },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            contentDescription = "menu list",
                            painter = painterResource(id = R.drawable.list_item),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = isExpandedMenu,
                        onDismissRequest = { isExpandedMenu = false }) {
                        listMenu.forEach { item ->
                            DropdownMenuItem(onClick = {
                                isExpandedMenu = false
                                when (item) {
                                    "Долг" -> {
                                        viewModel.obtainEvent(ShopEvent.ShowDialogChangeArrears)
                                    }

                                    "Доп.сумму" -> {
                                        viewModel.obtainEvent(ShopEvent.OpenAddSumDialog)
                                    }

                                    "Тип оплаты" -> {
                                        viewModel.obtainEvent(ShopEvent.ShowChangeTypePayDialog)
                                    }

                                    "Старая цена" -> {
                                        viewModel.obtainEvent(ShopEvent.SwitchPrice)
                                    }

                                    "Добавить заявку" -> {
                                        viewModel.obtainEvent(ShopEvent.ShowDialogAddRequest)
                                    }
                                }
                            }, text = {
                                when (item) {
                                    "Старая цена" -> Text(
                                        color = if (switchOldPrice) Color.Red else Color.Black,
                                        text = item
                                    )

                                    "Тип оплаты" -> Text(
                                        text = item + when (typePayState) {
                                            TypePayModel.CASH -> " (Нал)"
                                            TypePayModel.NO_CASH -> " (Без/Нал)"
                                            TypePayModel.ANOTHER -> " (Смешаный)"
                                        }
                                    )

                                    else -> Text(text = item)
                                }
                            })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, end = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.40f)
                        .padding(start = 3.dp, end = 5.dp)
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Товар",
                        fontSize = 12.sp,
                        color = AppTheme.colors.onSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(0.20f)
                        .padding(end = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Бонус",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.onSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(0.20f)
                        .padding(end = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Заявка",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.onSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(0.20f)
                        .padding(end = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Обмены",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.onSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            LazyColumn(
                Modifier
                    .padding(start = 5.dp, end = 5.dp)
                    .heightIn(max = 400.dp)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (requests.isNotEmpty()) {
                    items(
                        items = requests,
                        key = { request -> request.id }
                    ) { request ->
                        ProductRequestItem(
                            product = request,
                            deleteRequest = {
                                viewModel.obtainEvent(ShopEvent.DeleteRequest(request))
                            }, viewModel = viewModel
                        )
                    }
                } else {
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
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

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(0.333f)
                        .combinedClickable(onClick = {
                            viewModel.obtainEvent(
                                ShopEvent.SetArrearsInField
                            )
                        }, onLongClick = {
                            viewModel.obtainEvent(ShopEvent.SetArrearsAndAddInField)
                        }, onDoubleClick = {
                            viewModel.obtainEvent(ShopEvent.SetOrderAndArrearsSumInField)
                        })
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Долг",
                        fontSize = 12.sp,
                        color = AppTheme.colors.onSecondary
                    )
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "${shop.arrears}",
                        fontSize = 14.sp,
                        color = AppTheme.colors.onSecondary
                    )
                }
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(0.333f)
                        .combinedClickable(onLongClick = {
                            viewModel.obtainEvent(ShopEvent.SetOrderAndArrearsAndAddSumInField)
                        }, onClick = {
                            viewModel.obtainEvent(ShopEvent.SetAddInField)
                        })
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Доп",
                        fontSize = 12.sp,
                        color = AppTheme.colors.onSecondary
                    )
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "${shop.addSum}",
                        fontSize = 14.sp,
                        color = AppTheme.colors.onSecondary
                    )
                }
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(0.333f)
                        .combinedClickable(onClick = {
                            viewModel.obtainEvent(ShopEvent.SetOrderInField)
                        }, onLongClick = {
                            viewModel.obtainEvent(ShopEvent.SetOrderAndAddInField)
                        }, onDoubleClick = {
                            viewModel.obtainEvent(ShopEvent.SetOrderAndArrearsSumInField)
                        })
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Заявка",
                        fontSize = 12.sp,
                        color = AppTheme.colors.onSecondary
                    )
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = orderMoney,
                        fontSize = 14.sp,
                        color = AppTheme.colors.onSecondary
                    )
                }
            }
            when (typePayState) {
                TypePayModel.ANOTHER -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(5.dp)
                    ) {
                        CommonTextField(
                            value = cash,
                            placeholder = "Нал",
                            onVC = { newValue ->
                                cash = newValue
                                viewModel.obtainEvent(ShopEvent.ValueChangeCash(newValue))
                            },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                            ),
                            textStyle = TextStyle(textAlign = TextAlign.Center)
                        )
                        Spacer(Modifier.width(5.dp))
                        CommonTextField(
                            value = noCash,
                            placeholder = "Без/нал",
                            onVC = { newValue ->
                                noCash = newValue
                                viewModel.obtainEvent(ShopEvent.ValueChangeNoCashMoney(newValue))
                            },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                            ),
                            textStyle = TextStyle(textAlign = TextAlign.Center)
                        )
                    }
                }

                TypePayModel.CASH -> {
                    CommonTextField(
                        value = cash,
                        placeholder = "Нал",
                        onVC = { newValue ->
                            cash = newValue
                            viewModel.obtainEvent(ShopEvent.ValueChangeCash(newValue))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                        ),
                        textStyle = TextStyle(textAlign = TextAlign.Center)
                    )
                }

                TypePayModel.NO_CASH -> {
                    CommonTextField(
                        value = noCash,
                        placeholder = "Без/нал",
                        onVC = { newValue ->
                            noCash = newValue
                            viewModel.obtainEvent(ShopEvent.ValueChangeNoCashMoney(newValue))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                        ),
                        textStyle = TextStyle(textAlign = TextAlign.Center)
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductRequestItem(
    product: RequestModel, deleteRequest: (RequestModel) -> Unit, viewModel: ShopViewModel
) {


    var count by remember {
        mutableStateOf(product.count.toString())
    }

    var countBonus by remember {
        mutableStateOf(product.bonus.toString())
    }

    var countExchange by remember {
        mutableStateOf(product.exchange.toString())
    }

    var stateKeyBoard by remember { mutableStateOf(false) }
    var editBonus by remember { mutableStateOf(false) }
    var editCount by remember { mutableStateOf(false) }
    var editExchange by remember { mutableStateOf(false) }

    if (stateKeyBoard) {
        if (editCount) {
            KeyBoardDialog(onDismissRequest = {
                stateKeyBoard = false
                editCount = false
            }, setNumber = {
                stateKeyBoard = false
                editCount = false
                count = it.toString()
                viewModel.obtainEvent(
                    ShopEvent.ChangeCountRequest(
                        count = it.toString(), item = product
                    )
                )
            }, text = "Изменить заявку - " + product.name, value = count
            )
        }
        if (editExchange) {
            KeyBoardDialog(onDismissRequest = {
                stateKeyBoard = false
                editExchange = false
            }, setNumber = {
                stateKeyBoard = false
                editExchange = false
                countExchange = it.toString()
                viewModel.obtainEvent(
                    ShopEvent.ChangeExchangeRequest(
                        exchange = it.toString(), item = product
                    )
                )
            }, text = "Изменить возврат - " + product.name, value = countExchange
            )
        }
        if (editBonus) {
            KeyBoardDialog(onDismissRequest = {
                stateKeyBoard = false
                editBonus = false
            }, setNumber = {
                stateKeyBoard = false
                editBonus = false
                countBonus = it.toString()
                viewModel.obtainEvent(
                    ShopEvent.ChangeBonusRequest(
                        bonus = it.toString(), item = product
                    )
                )
            }, text = "Изменить бонус - " + product.name, value = countBonus
            )
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp))
            .padding(3.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            style = MaterialTheme.typography.labelSmall,
            text = product.name,
            fontSize = 14.sp,
            modifier = Modifier
                .weight(0.4f)
                .padding(end = 5.dp)
                .combinedClickable(onDoubleClick = { deleteRequest(product) }, onClick = {}),
            color = AppTheme.colors.onSecondary
        )
        Box(
            modifier = Modifier
                .clickable(onClick = {
                    stateKeyBoard = true
                    editBonus = true
                })
                .fillMaxHeight()
                .weight(0.2f)
                .padding(end = 5.dp)
                .background(
                    shape = RoundedCornerShape(10.dp),
                    color = AppTheme.colors.secondaryVariant
                )
        ) {
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = if (countBonus == "0") "" else countBonus,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Center),
                color = AppTheme.colors.onSecondary
            )
        }
        Box(
            modifier = Modifier
                .clickable(onClick = {
                    stateKeyBoard = true
                    editCount = true
                })
                .fillMaxHeight()
                .weight(0.2f)
                .padding(end = 5.dp)
                .background(
                    shape = RoundedCornerShape(10.dp),
                    color = AppTheme.colors.secondaryVariant
                )
        ) {
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = if (count == "0") "" else count,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Center),
                color = AppTheme.colors.onSecondary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.2f)
                .padding(end = 5.dp)
                .clickable(onClick = {
                    stateKeyBoard = true
                    editExchange = true
                })
                .background(
                    shape = RoundedCornerShape(10.dp),
                    color = AppTheme.colors.secondaryVariant
                )
        ) {
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = if (countExchange == "0") "" else countExchange,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Center),
                color = AppTheme.colors.onSecondary
            )
        }
    }
}