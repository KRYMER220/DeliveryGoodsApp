package ru.krymer.delivery.ui.screens.route.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.ui.screens.route.models.RouteViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun RouteView(
    viewState: RouteViewState,
    onItemClicked: (RouteModel) -> Unit,
    onItemLongClicked: (RouteModel) -> Unit,
    onItemDelete: (RouteModel) -> Unit,
    sharedViewModel: SharedViewModel
) {
    LazyColumn {
        items(viewState.listRoute.value) { route ->
            RouteItem(
                route = route,
                onItemClicked = onItemClicked,
                onItemDelete = onItemDelete,
                onItemLongClicked = onItemLongClicked,
                sharedViewModel = sharedViewModel
            )
            Spacer(modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RouteItem(
    route: RouteModel,
    onItemClicked: (RouteModel) -> Unit,
    onItemLongClicked: (RouteModel) -> Unit,
    onItemDelete: (RouteModel) -> Unit,
    sharedViewModel: SharedViewModel
) {
    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = { onItemClicked(route) },
                onLongClick = { onItemLongClicked(route) })
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
                text = route.name,
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
                        .clickable(onClick = { onItemDelete(route) })
                )
            }
        }
    }
}