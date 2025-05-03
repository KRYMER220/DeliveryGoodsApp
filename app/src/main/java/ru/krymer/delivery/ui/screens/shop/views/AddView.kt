package ru.krymer.delivery.ui.screens.shop.views

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.ui.components.KeyBoardDialog
import ru.krymer.delivery.ui.screens.shop.ShopViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants

@Composable
fun AddShopAndRequestView(
    viewState: ShopViewState, viewModel: ShopViewModel
) {
    val clients = viewState.listClient.collectAsState().value
    val products = viewState.listProduct.collectAsState().value
    if (clients.isNotEmpty() && products.isNotEmpty()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(
                        color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            viewModel.obtainEvent(ShopEvent.ShowSelectorClientInAddDialog)
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewState.currentClient?.name ?: Constants.EMPTY.EMPTY_DATA,
                        modifier = Modifier.padding(start = 15.dp),
                        color = AppTheme.colors.onSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 15.dp),
                        tint = AppTheme.colors.onSecondary
                    )
                    DropdownMenu(
                        expanded = viewState.isShowSelectorClientInAddDialog,
                        onDismissRequest = {
                            viewModel.obtainEvent(ShopEvent.DismissSelectorClientInAddDialog)
                        }) {
                        val list = viewState.listClient.collectAsState().value
                        list.forEach {
                            DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                viewModel.obtainEvent(
                                    ShopEvent.DropDownSelectClient(it)
                                )
                                viewModel.obtainEvent(
                                    ShopEvent.DismissSelectorClientInAddDialog
                                )
                            })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Box(modifier = Modifier.weight(0.4f))
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Бонусы",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.onSecondary
                    )
                }
                Box(
                    modifier = Modifier.weight(0.2f)
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Заявка",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.onSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            LazyColumn {
                items(viewState.listProductRequest.value) { product ->
                    ProductAddShopWithOrderItem(product = product, onVCCount = {
                        viewModel.obtainEvent(
                            ShopEvent.ChangeCountProduct(
                                product = product, count = it
                            )
                        )
                    }, onVCCountBonus = {
                        viewModel.obtainEvent(
                            ShopEvent.ChangeCountBonusProduct(
                                product = product, bonus = it
                            )
                        )
                    },
                        onChangeStatus = { viewModel.obtainEvent(ShopEvent.ChangeAddStatusProduct(it)) })
                    Spacer(modifier = Modifier.height(10.dp))
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
fun AddRequestView(
    viewState: ShopViewState, viewModel: ShopViewModel
) {
    val products = viewState.listProduct.collectAsState().value
    if (products.isNotEmpty()) {
        Column {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.6f))
                Box(
                    modifier = Modifier.weight(0.2f)
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Бонусы",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.onSecondary
                    )
                }
                Box(
                    modifier = Modifier.weight(0.2f)
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = "Заявка",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.onSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            LazyColumn {
                items(viewState.listProductRequest.value) { product ->
                    ProductAddShopWithOrderItem(product = product, onVCCount = {
                        viewModel.obtainEvent(
                            ShopEvent.ChangeCountProduct(
                                product = product, count = it
                            )
                        )
                    }, onVCCountBonus = {
                        viewModel.obtainEvent(
                            ShopEvent.ChangeCountBonusProduct(
                                product = product, bonus = it
                            )
                        )
                    },
                        onChangeStatus = { viewModel.obtainEvent(ShopEvent.ChangeAddStatusProduct(it)) })
                    Spacer(modifier = Modifier.height(10.dp))
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
fun ProductAddShopWithOrderItem(
    product: ProductModel,
    onVCCount: (String) -> Unit,
    onVCCountBonus: (String) -> Unit,
    onChangeStatus: (ProductModel) -> Unit,
) {

    var count by remember {
        mutableStateOf("")
    }

    var countBonus by remember {
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
            }, text = "Изменить заявку - " + product.name, value = count
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
            }, text = "Изменить бонус - " + product.name, value = countBonus
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
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            text = product.name,
            fontSize = 14.sp,
            modifier = Modifier
                .weight(0.4f)
                .wrapContentHeight()
                .padding(end = 8.dp)
                .clickable(onClick = { onChangeStatus(product) }),
            color = if (product.isAdd) Color.Red else AppTheme.colors.onSecondary
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
                    shape = RoundedCornerShape(10.dp), color = AppTheme.colors.secondaryVariant
                )
        ) {
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = countBonus,
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
                    shape = RoundedCornerShape(10.dp), color = AppTheme.colors.secondaryVariant
                )
        ) {
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = count,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Center),
                color = AppTheme.colors.onSecondary
            )
        }
    }
}


