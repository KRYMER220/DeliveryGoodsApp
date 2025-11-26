package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.ui.components.GenericDropdown
import ru.krymer.delivery.ui.components.KeyBoardDialog
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.convertToTextDate

@Composable
fun AddShopAndRequestView(
    state: ShopViewState, event: (ShopEvent) -> Unit
) {
    val clients = state.clients
    val listShop = state.listCurrentShopInfo
    val products = state.listProductRequest
    var toggleMenuClients by remember { mutableStateOf(false) }
    if (clients.isNotEmpty() && products.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            item {
                GenericDropdown(
                    selectedItem = state.currentClient,
                    items = clients,
                    expanded = toggleMenuClients,
                    onExpandedChange = {
                        toggleMenuClients = !toggleMenuClients
                    },
                    itemLabel = { it.name },
                    placeholder = stringResource(R.string.not_select_client),
                    onItemSelected = { client ->
                        event(ShopEvent.SelectClient(client))
                    })
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Spacer(modifier = Modifier.weight(0.6f))
                    Text(
                        style = AppTheme.typography.bodySmall,
                        text = stringResource(R.string.bonus),
                        modifier = Modifier
                            .weight(0.2f)
                            .clickable(onClick = {
                                event(ShopEvent.SwitchBonusState)
                            }),
                        color = AppTheme.colors.onSecondary
                    )
                    Text(
                        style = AppTheme.typography.bodySmall,
                        text = stringResource(R.string.request),
                        modifier = Modifier.weight(0.2f),
                        color = AppTheme.colors.onSecondary
                    )
                }
            }

            items(products) { product ->
                ProductAddShopWithOrderItem(product = product, onVCCount = {
                    event(
                        ShopEvent.ChangeCountProduct(
                            product = product, count = it
                        )
                    )
                }, onVCCountBonus = {
                    event(
                        ShopEvent.ChangeCountBonusProduct(
                            product = product, bonus = it
                        )
                    )
                },
                    stateBonus = state.isBonusState
                )
            }

            item {
                MessagesView(viewState = state, deleteMessage = {
                    event(ShopEvent.DeleteMessage(it))
                })
            }

            if (listShop.isNotEmpty()) {
                items(listShop) { shop ->
                    InfoAboutShopForCreate(shop, copyInfoData = {
                        event(ShopEvent.ToggleConfirmCopyAndSave)
                        event(ShopEvent.SelectShop(it))
                    })
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center),
                strokeWidth = 2.dp,
                color = AppTheme.colors.onSecondary
            )
        }
    }

}

@Composable
fun MessagesView(viewState: ShopViewState, deleteMessage: (MessageModel) -> Unit) {
    val messages = viewState.messages
    if (messages.isNotEmpty()) {
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            items(messages) { message ->
                MessageItem(messageModel = message, deleteMessage = deleteMessage)
            }
        }
    }
}

@Composable
fun MessageItem(messageModel: MessageModel, deleteMessage: (MessageModel) -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(AppTheme.colors.secondary, shape = RoundedCornerShape(5.dp))) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = convertToTextDate(messageModel.date, pattern = Constants.PatternDate.FULL), color = AppTheme.colors.error, textAlign = TextAlign.Center)
                Text(modifier = Modifier.fillMaxWidth(), text = messageModel.text, color = AppTheme.colors.onSecondary)
            }
            Image(
                contentDescription = null,
                painter = painterResource(id = R.drawable.delete),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = { deleteMessage(messageModel) })
            )
        }
    }
}

@Composable
fun ProductAddShopWithOrderItem(
    product: ProductModel,
    onVCCount: (String) -> Unit,
    onVCCountBonus: (String) -> Unit,
    stateBonus: Boolean = false
) {

    var count by rememberSaveable {
        mutableStateOf("")
    }

    var countBonus by rememberSaveable {
        mutableStateOf("")
    }

    var stateKeyBoard by remember { mutableStateOf(false) }
    var editBonus by remember { mutableStateOf(false) }
    var editCount by remember { mutableStateOf(false) }

    if (stateKeyBoard) {
        if (editCount) {
            KeyBoardDialog(onDismissRequest = {
                stateKeyBoard = false
                editCount = false
            }, setNumber = {
                stateKeyBoard = false
                editCount = false
                count = it.toString()
                onVCCount(count)
            }, text = "${stringResource(R.string.request)}: " + product.name, value = count
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
                onVCCountBonus(countBonus)
            }, text = "${stringResource(R.string.bonus)}: " + product.name, value = countBonus
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp))
            .height(60.dp)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            style = AppTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            text = product.name,
            modifier = Modifier
                .weight(0.4f)
                .wrapContentHeight()
                .padding(end = 8.dp),
            color = if (product.isAdd) Color.Red else AppTheme.colors.onSecondary
        )
        if (stateBonus) {
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
                        shape = RoundedCornerShape(10.dp), color = AppTheme.colors.secondaryVariant
                    )
            ) {
                Text(
                    style = AppTheme.typography.labelLarge,
                    text = countBonus,
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
                .background(
                    shape = RoundedCornerShape(10.dp), color = AppTheme.colors.secondaryVariant
                )
        ) {
            Text(
                style = AppTheme.typography.labelLarge,
                text = count,
                modifier = Modifier
                    .align(Alignment.Center),
                color = AppTheme.colors.onSecondary
            )
        }
    }
}


