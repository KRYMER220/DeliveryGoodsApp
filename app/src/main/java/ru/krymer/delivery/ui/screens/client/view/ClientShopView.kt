package ru.krymer.delivery.ui.screens.client.view

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.ui.screens.client.ClientViewModel
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.models.ClientShopViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun ClientView(
    viewState: ClientShopViewState,
    onItemClicked: (ClientModel) -> Unit,
    onItemDelete: (ClientModel) -> Unit,
    viewModel: ClientViewModel,
    sharedViewModel: SharedViewModel
) {
    LazyColumn {
        itemsIndexed(viewState.listClient.value, key = { _, item -> item.id }) { index, client ->
            ClientItem(
                index = index,
                client = client,
                onItemClicked = onItemClicked,
                onItemDelete = onItemDelete,
                viewState = viewState,
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                    placementSpec = tween(durationMillis = 400)
                ),
                onItemDownIndex = { viewModel.obtainEvent(ClientEvent.DownItemIndex(index)) },
                onItemUpIndex = { viewModel.obtainEvent(ClientEvent.UpItemIndex(index)) },
                sharedViewModel = sharedViewModel
            )
            Spacer(modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClientItem(
    client: ClientModel,
    onItemClicked: (ClientModel) -> Unit,
    onItemDelete: (ClientModel) -> Unit,
    viewState: ClientShopViewState,
    modifier: Modifier,
    onItemUpIndex: (Int) -> Unit,
    onItemDownIndex: (Int) -> Unit,
    sharedViewModel: SharedViewModel,
    index: Int
) {
    val list = viewState.listClient.collectAsState().value.size
    Box(
        modifier = modifier
            .combinedClickable(
                onClick = { onItemClicked(client) })
            .background(
                color = AppTheme.colors.secondary, shape = RoundedCornerShape(16.dp)
            )
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = client.name,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically),
                color = AppTheme.colors.textColor
            )
            if (sharedViewModel.initSysAdm()) {
                Image(
                    contentDescription = "delete route",
                    painter = painterResource(id = R.drawable.delete),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = { onItemDelete(client) })
                )
            }
            Column(Modifier.padding(start = 10.dp)) {
                if (index != 0) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = null,
                        tint = AppTheme.colors.onSecondary,
                        modifier = Modifier.clickable(onClick = { onItemUpIndex(index) })
                    )
                }
                if (index != list - 1) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            onItemDownIndex(index)
                        }), tint = AppTheme.colors.onSecondary
                    )
                }
            }
        }
    }

}
