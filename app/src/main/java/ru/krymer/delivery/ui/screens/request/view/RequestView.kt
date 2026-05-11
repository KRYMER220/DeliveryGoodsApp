package ru.krymer.delivery.ui.screens.request.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.data.model.utilModel.StatusModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.toStatusModel
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.components.CustomCircularProgressIndicator
import ru.krymer.delivery.ui.components.KeyBoardDialog
import ru.krymer.delivery.ui.screens.request.models.RequestEvent
import ru.krymer.delivery.ui.screens.request.models.RequestViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RequestView(state: RequestViewState, event: (RequestEvent) -> Unit) {
    OnceItemShop(state = state, event = event)
}

@Composable
fun OnceItemShop(state: RequestViewState, event: (RequestEvent) -> Unit) {
    state.shop?.let { shop ->
        val listMenu = listOf(
            stringResource(R.string.arrears),
            stringResource(R.string.add_sum),
            stringResource(R.string.old_price),
            stringResource(R.string.bonus),
            stringResource(R.string.del_shop),
            stringResource(R.string.send_message),
            stringResource(R.string.all_req)
        )
        var isExpandedMenu by remember { mutableStateOf(false) }
        val requests = state.requests
        val orderMoney = state.orderMoney.toInt().toString()
        val stateCash = state.getCash
        val stateNoCash = state.getNoCash
        var cash by remember { mutableStateOf("") }
        var noCash by remember { mutableStateOf("") }
        val typePayState = state.typePay
        val switchOldPrice = state.isOldPrice
        var errorCash by remember { mutableStateOf(Error()) }
        var errorNoCash by remember { mutableStateOf(Error()) }
        val errorEmpty = stringResource(R.string.empty_input)


        LaunchedEffect(stateCash) {
            cash = stateCash
        }
        LaunchedEffect(stateNoCash) {
            noCash = stateNoCash
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp, color = when (typePayState) {
                                TypePayModel.CASH -> Color.Transparent
                                TypePayModel.NO_CASH -> Color.Magenta
                                TypePayModel.ANOTHER -> Color.Green
                            }, shape = RoundedCornerShape(10.dp)
                        )
                        .weight(0.333f)
                        .height(40.dp)
                        .clickable(onClick = {
                            event(RequestEvent.ChangeTypePay)
                        })
                        .wrapContentHeight(Alignment.CenterVertically),
                    text = when (typePayState) {
                        TypePayModel.CASH -> stringResource(R.string.cash)
                        TypePayModel.NO_CASH -> stringResource(R.string.noCash)
                        TypePayModel.ANOTHER -> stringResource(R.string.another_pay)
                    },
                    style = AppTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    color = AppTheme.colors.onSecondary
                )
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = convertToTextDate(shop.date),
                    color = AppTheme.colors.onSecondary,
                    modifier = Modifier
                        .weight(0.333f),
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier
                        .weight(0.333f)
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Image(
                        contentDescription = "menu list",
                        painter = painterResource(id = R.drawable.list_item),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { isExpandedMenu = !isExpandedMenu }
                    )
                    DropdownMenu(
                        expanded = isExpandedMenu,
                        onDismissRequest = { isExpandedMenu = false },
                        modifier = Modifier.background(AppTheme.colors.onPrimary)
                    ) {
                        listMenu.forEach { item ->
                            DropdownMenuItem(onClick = {
                                isExpandedMenu = false
                                when (item) {
                                    listMenu[0] -> {
                                        event(RequestEvent.ToggleArrearsDialog)
                                    }

                                    listMenu[1] -> {
                                        event(RequestEvent.ToggleAddSumDialog)
                                    }

                                    listMenu[2] -> {
                                        event(RequestEvent.ChangeTypePrice)
                                    }

                                    listMenu[3] -> {
                                        event(RequestEvent.SwitchBonus)
                                    }

                                    listMenu[4] -> {
                                        event(RequestEvent.ToggleDeleteShop)
                                    }

                                    listMenu[5] -> {
                                        event(RequestEvent.ToggleMessageDialog)
                                    }

                                    listMenu[6] -> {
                                        event(RequestEvent.ToggleListShops)
                                    }
                                }
                            }, text = {
                                when (item) {
                                    listMenu[2] -> Text(
                                        color = if (switchOldPrice) Color.Red else AppTheme.colors.onSecondary,
                                        text = item,
                                        style = AppTheme.typography.titleSmall
                                    )

                                    else -> Text(
                                        text = item,
                                        style = AppTheme.typography.titleSmall,
                                        color = AppTheme.colors.onSecondary
                                    )
                                }
                            })
                        }
                    }
                }
            }

            Text(
                style = AppTheme.typography.titleLarge,
                text = shop.nameShop,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier
                    .padding(5.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

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
                        style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                        text = "",
                        color = AppTheme.colors.onSecondary
                    )
                }
                if (shop.isBonus) {
                    Box(
                        modifier = Modifier
                            .weight(0.20f)
                            .padding(end = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                            text = stringResource(R.string.bonus),
                            modifier = Modifier.align(Alignment.Center),
                            color = AppTheme.colors.onSecondary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(0.20f)
                        .padding(end = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                        text = stringResource(R.string.request),
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
                        style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                        text = stringResource(R.string.exchange),
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.onSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    Modifier
                        .padding(start = 5.dp, end = 5.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (requests.isNotEmpty()) {
                        items(
                            items = requests,
                            key = { request -> request.id }
                        ) { request ->
                            ProductRequestItem(
                                request = request,
                                event = event,
                                shop = shop,
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
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
                                            event(
                                                RequestEvent.SetArrearsInField(arrears = shop.arrears)
                                            )
                                        }, onLongClick = {
                                            event(RequestEvent.SetArrearsAndAddInField(sum = shop.arrears + shop.addSum))
                                        }, onDoubleClick = {
                                            event(RequestEvent.SetOrderAndArrearsSumInField(arrears = shop.arrears))
                                        })
                                ) {
                                    Text(
                                        style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 4).sp),
                                        text = stringResource(R.string.arrears),
                                        color = AppTheme.colors.onSecondary
                                    )
                                    Text(
                                        style = AppTheme.typography.labelSmall,
                                        text = "${shop.arrears.toInt()}",
                                        color = AppTheme.colors.onSecondary
                                    )
                                }
                                if (shop.addSum > 0.0) {
                                    Column(
                                        verticalArrangement = Arrangement.SpaceAround,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(0.333f)
                                            .combinedClickable(onLongClick = {
                                                event(
                                                    RequestEvent.SetOrderAndArrearsAndAddSumInField(
                                                        arrears = shop.arrears,
                                                        addSum = shop.addSum
                                                    )
                                                )
                                            }, onClick = {
                                                event(RequestEvent.SetAddInField(addSum = shop.addSum))
                                            })
                                    ) {
                                        Text(
                                            style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                            text = stringResource(R.string.add_sum_short),
                                            color = AppTheme.colors.onSecondary
                                        )
                                        Text(
                                            style = AppTheme.typography.labelSmall,
                                            text = "${shop.addSum.toInt()}",
                                            color = AppTheme.colors.onSecondary
                                        )
                                    }
                                }
                                Column(
                                    verticalArrangement = Arrangement.SpaceAround,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(0.333f)
                                        .combinedClickable(onClick = {
                                            event(RequestEvent.SetOrderInField)
                                        }, onLongClick = {
                                            event(RequestEvent.SetOrderAndAddInField(addSum = shop.addSum))
                                        }, onDoubleClick = {
                                            event(RequestEvent.SetOrderAndArrearsSumInField(arrears = shop.arrears))
                                        })
                                ) {
                                    Text(
                                        style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                        text = stringResource(R.string.request),
                                        color = AppTheme.colors.onSecondary
                                    )
                                    Text(
                                        style = AppTheme.typography.labelSmall,
                                        text = orderMoney,
                                        color = AppTheme.colors.onSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(15.dp))
                            when (typePayState) {
                                TypePayModel.ANOTHER -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(0.333f)) {
                                                Text(
                                                    style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                                    text = stringResource(R.string.cash),
                                                    color = AppTheme.colors.onSecondary
                                                )
                                                CommonTextField(
                                                    value = cash,
                                                    placeholder = stringResource(R.string.cash),
                                                    modifier = Modifier,
                                                    changerText = { newValue ->
                                                        cash = newValue
                                                        errorCash = when {
                                                            newValue == "" -> Error(
                                                                visible = true,
                                                                error = errorEmpty
                                                            )

                                                            else -> {
                                                                event(
                                                                    RequestEvent.ChangeCash(
                                                                        newValue
                                                                    )
                                                                )
                                                                Error()
                                                            }
                                                        }
                                                    },
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Number,
                                                        imeAction = ImeAction.Done
                                                    ),
                                                    textStyle = AppTheme.typography.titleLarge,
                                                    isError = errorCash.visible,
                                                    errorValue = errorCash.error,
                                                    autoClearFocus = true
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(0.333f)) {
                                                Text(
                                                    style = AppTheme.typography.bodySmall.copy(fontSize = (AppTheme.typography.bodySmall.fontSize.value - 2).sp),
                                                    text = stringResource(R.string.noCash),
                                                    color = AppTheme.colors.onSecondary
                                                )
                                                CommonTextField(
                                                    value = noCash,
                                                    placeholder = stringResource(R.string.noCash),
                                                    modifier = Modifier,
                                                    changerText = { newValue ->
                                                        noCash = newValue
                                                        errorNoCash = when {
                                                            newValue == "" -> Error(
                                                                visible = true,
                                                                error = errorEmpty
                                                            )

                                                            else -> {
                                                                event(
                                                                    RequestEvent.ChangeNoCash(
                                                                        newValue
                                                                    )
                                                                )
                                                                Error()
                                                            }
                                                        }
                                                    },
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Number,
                                                        imeAction = ImeAction.Done
                                                    ),
                                                    textStyle = AppTheme.typography.titleLarge,
                                                    isError = errorNoCash.visible,
                                                    errorValue = errorNoCash.error,
                                                    autoClearFocus = true
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                contentDescription = "submit",
                                                painter = when {
                                                    !shop.status && shop.statusServer.toStatusModel() == StatusModel.NOT_CHANGE -> painterResource(
                                                        id = R.drawable.submit
                                                    )

                                                    shop.statusServer.toStatusModel() == StatusModel.UN_SYNC -> painterResource(
                                                        id = R.drawable.submit_unsync
                                                    )

                                                    shop.statusServer.toStatusModel() == StatusModel.SYNC_FAILED -> painterResource(id = R.drawable.submit_failed)


                                                    else -> painterResource(id = R.drawable.submit_active)
                                                },
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .combinedClickable(onClick = {
                                                        when {
                                                            (cash.isEmpty() && noCash.isEmpty()) -> {
                                                                errorCash = Error(
                                                                    visible = true,
                                                                    error = errorEmpty
                                                                )
                                                                errorNoCash = Error(
                                                                    visible = true,
                                                                    error = errorEmpty
                                                                )
                                                            }

                                                            noCash.isEmpty() -> errorNoCash =
                                                                Error(
                                                                    visible = true,
                                                                    error = errorEmpty
                                                                )

                                                            cash.isEmpty() -> errorCash = Error(
                                                                visible = true,
                                                                error = errorEmpty
                                                            )

                                                            else -> {
                                                                event(RequestEvent.SubmitSaveShop)
                                                            }
                                                        }
                                                    }, onDoubleClick = {
                                                        event(RequestEvent.ToggleStatusShop)
                                                    })
                                            )
                                        }
                                    }

                                }

                                TypePayModel.CASH -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        CommonTextField(
                                            value = cash,
                                            placeholder = stringResource(R.string.cash),
                                            changerText = { newValue ->
                                                cash = newValue
                                                errorCash = when {
                                                    newValue == "" -> Error(
                                                        visible = true,
                                                        error = errorEmpty
                                                    )

                                                    else -> {
                                                        event(RequestEvent.ChangeCash(newValue))
                                                        Error()
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f, fill = false)
                                                .padding(5.dp),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Done
                                            ),
                                            textStyle = AppTheme.typography.titleLarge,
                                            isError = errorCash.visible,
                                            errorValue = errorCash.error,
                                            autoClearFocus = true
                                        )
                                        Image(
                                            contentDescription = "submit",
                                            painter = when {
                                                !shop.status && shop.statusServer.toStatusModel() == StatusModel.NOT_CHANGE -> painterResource(
                                                    id = R.drawable.submit
                                                )

                                                shop.statusServer.toStatusModel() == StatusModel.UN_SYNC -> painterResource(
                                                    id = R.drawable.submit_unsync
                                                )
                                                shop.statusServer.toStatusModel() == StatusModel.SYNC_FAILED -> painterResource(id = R.drawable.submit_failed)


                                                else -> painterResource(id = R.drawable.submit_active)
                                            },
                                            modifier = Modifier
                                                .size(60.dp)
                                                .combinedClickable(onClick = {
                                                    if (cash.isEmpty()) {
                                                        errorCash = Error(
                                                            visible = true,
                                                            error = errorEmpty
                                                        )
                                                    } else {
                                                        event(RequestEvent.SubmitSaveShop)
                                                    }
                                                }, onDoubleClick = {
                                                    event(RequestEvent.ToggleStatusShop)
                                                })
                                        )
                                    }

                                }

                                TypePayModel.NO_CASH -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        CommonTextField(
                                            value = noCash,
                                            placeholder = stringResource(R.string.noCash),
                                            changerText = { newValue ->
                                                noCash = newValue
                                                errorNoCash = when {
                                                    newValue == "" -> Error(
                                                        visible = true,
                                                        error = errorEmpty
                                                    )

                                                    else -> {
                                                        event(
                                                            RequestEvent.ChangeNoCash(
                                                                newValue
                                                            )
                                                        )
                                                        Error()
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f, fill = false)
                                                .padding(5.dp),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Done
                                            ),
                                            textStyle = AppTheme.typography.titleLarge,
                                            isError = errorNoCash.visible,
                                            errorValue = errorNoCash.error,
                                            autoClearFocus = true
                                        )
                                        Image(
                                            contentDescription = "submit",
                                            painter = when {
                                                !shop.status && shop.statusServer.toStatusModel() == StatusModel.NOT_CHANGE -> painterResource(
                                                    id = R.drawable.submit
                                                )

                                                shop.statusServer.toStatusModel() == StatusModel.UN_SYNC -> painterResource(
                                                    id = R.drawable.submit_unsync
                                                )
                                                shop.statusServer.toStatusModel() == StatusModel.SYNC_FAILED -> painterResource(id = R.drawable.submit_failed)


                                                else -> painterResource(id = R.drawable.submit_active)
                                            },
                                            modifier = Modifier
                                                .size(60.dp)
                                                .combinedClickable(onDoubleClick = {
                                                    event(RequestEvent.ToggleStatusShop)
                                                }, onClick = {
                                                    if (noCash.isEmpty()) {
                                                        errorNoCash = Error(
                                                            visible = true,
                                                            error = errorEmpty
                                                        )
                                                    } else {
                                                        event(RequestEvent.SubmitSaveShop)
                                                    }
                                                })
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    } else {
                        item {
                            CustomCircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductRequestItem(
    request: RequestModel,
    event: (RequestEvent) -> Unit,
    shop: ShopModel
) {


    var count by remember {
        mutableStateOf(request.count.toString())
    }

    var countBonus by remember {
        mutableStateOf(request.bonus.toString())
    }

    var countExchange by remember {
        mutableStateOf(request.exchange.toString())
    }

    var stateKeyBoard by remember { mutableStateOf(false) }
    var editBonus by remember { mutableStateOf(false) }
    var editCount by remember { mutableStateOf(false) }
    var editExchange by remember { mutableStateOf(false) }

    if (stateKeyBoard) {
        if (editCount) {
            KeyBoardDialog(
                onDismissRequest = {
                    stateKeyBoard = false
                    editCount = false
                }, setNumber = {
                    stateKeyBoard = false
                    editCount = false
                    count = it.toString()
                    event(
                        RequestEvent.ChangeCountRequest(
                            count = it.toString(), request = request
                        )
                    )
                }, text = "${stringResource(R.string.request)}:\n" + request.name, value = count
            )
        }
        if (editExchange) {
            KeyBoardDialog(
                onDismissRequest = {
                stateKeyBoard = false
                editExchange = false
            }, setNumber = {
                stateKeyBoard = false
                editExchange = false
                countExchange = it.toString()
                event(
                    RequestEvent.ChangeExchangeRequest(
                        exchange = it.toString(), request = request
                    )
                )
                },
                text = "${stringResource(R.string.exchange)}:\n" + request.name,
                value = countExchange
            )
        }
        if (editBonus) {
            KeyBoardDialog(
                onDismissRequest = {
                stateKeyBoard = false
                editBonus = false
            }, setNumber = {
                stateKeyBoard = false
                editBonus = false
                countBonus = it.toString()
                event(
                    RequestEvent.ChangeBonusRequest(
                        bonus = it.toString(), request = request
                    )
                )
                }, text = "${stringResource(R.string.bonus)}:\n" + request.name, value = countBonus
            )
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp))
            .padding(top = 3.dp, bottom = 3.dp, start = 15.dp, end = 3.dp)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            style = AppTheme.typography.titleMedium,
            text = request.name,
            modifier = Modifier
                .weight(0.4f)
                .padding(end = 5.dp),
            color = AppTheme.colors.onSecondary
        )
        if (shop.isBonus) {
            Box(
                modifier = Modifier
                    .clickable(onClick = {
                        stateKeyBoard = true
                        editBonus = true
                    })
                    .fillMaxHeight()
                    .weight(0.2f)
                    .padding(end = 5.dp)
                    .border(
                        width = 1.dp,
                        color = AppTheme.colors.onPrimary,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Text(
                    style = AppTheme.typography.labelSmall,
                    text = if (countBonus == "0") "" else countBonus,
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.onSecondary
                )
            }
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
                .border(
                    width = 1.dp,
                    color = AppTheme.colors.onPrimary,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Text(
                style = AppTheme.typography.labelSmall,
                text = if (count == "0") "" else count,
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
                .border(
                    width = 1.dp,
                    color = AppTheme.colors.onPrimary,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Text(
                style = AppTheme.typography.labelSmall,
                text = if (countExchange == "0") "" else countExchange,
                modifier = Modifier.align(Alignment.Center),
                color = AppTheme.colors.onSecondary
            )
        }
    }
}

