package ru.krymer.delivery.ui.screens.trip.views

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
import androidx.compose.foundation.layout.height
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
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@Composable
fun TripView(
    viewState: TripViewState,
    onItemLongClicked: (TripModel) -> Unit,
    onItemDelete: (TripModel) -> Unit,
    onItemClick: (TripModel) -> Unit,
    sharedViewModel: SharedViewModel
) {
    LazyColumn {
        items(viewState.listTrip.value) { trip ->
            TripItem(
                trip = trip,
                onItemLongClicked = onItemLongClicked,
                onItemDelete = onItemDelete,
                onItemClick = onItemClick,
                sharedViewModel = sharedViewModel
            )
            Spacer(modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripItem(
    trip: TripModel,
    onItemLongClicked: (TripModel) -> Unit,
    onItemDelete: (TripModel) -> Unit,
    onItemClick: (TripModel) -> Unit,
    sharedViewModel: SharedViewModel
) {
    Box(
        modifier = Modifier
            .combinedClickable(onLongClick = { onItemLongClicked(trip) },
                onClick = { onItemClick(trip) })
            .background(
                color = AppTheme.colors.secondary, shape = RoundedCornerShape(16.dp)
            )
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    text = trip.nameRoute,
                    fontSize = 20.sp,
                    color = AppTheme.colors.onSecondary
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    style = MaterialTheme.typography.labelLarge,
                    text = convertToTextDate(trip.date),
                    fontSize = 16.sp,
                    color = AppTheme.colors.onSecondary
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    style = MaterialTheme.typography.labelLarge,
                    text = trip.nameCourier,
                    fontSize = 16.sp,
                    color = AppTheme.colors.onSecondary
                )
            }
            if (sharedViewModel.initSysAdm()) {
                Image(
                    contentDescription = "delete trip",
                    painter = painterResource(id = R.drawable.delete),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = { onItemDelete(trip) })
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}