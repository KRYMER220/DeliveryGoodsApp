package ru.krymer.delivery.ui.screens.trip.views

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripItem(
    trip: TripModel,
    updateTrip: (TripModel) -> Unit,
    deleteTrip: (TripModel) -> Unit,
    routeToTrip: (TripModel) -> Unit,
    sharedViewModel: SharedViewModel
) {
    Box(
        modifier = Modifier
            .combinedClickable(onLongClick = { updateTrip(trip) },
                onClick = { routeToTrip(trip) })
            .background(
                color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    style = AppTheme.typography.titleMedium,
                    text = trip.nameRoute,
                    color = AppTheme.colors.onSecondary
                )
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = convertToTextDate(trip.date),
                    color = AppTheme.colors.onSecondary
                )
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = trip.nameCourier,
                    color = AppTheme.colors.onSecondary
                )
            }
            if (sharedViewModel.initSysAdm()) {
                Image(
                    contentDescription = "delete trip",
                    painter = painterResource(id = R.drawable.delete),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = { deleteTrip(trip) })
                )
            }
        }
    }
}