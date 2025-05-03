package ru.krymer.delivery.ui.screens.courier.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.user.StatusModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.courier.models.CourierViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun CourierView(
    chooseUser: (UserModel) -> Unit,
    banUser: (UserModel) -> Unit,
    deleteUser: (UserModel) -> Unit,
    couriers: List<UserModel>
) {
    LazyColumn {
        items(couriers) { courier ->
            CourierItem(
                courier = courier,
                onItemClicked = chooseUser,
                onItemBan = banUser,
                onItemDelete = deleteUser
            )
            Spacer(modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}

@Composable
fun CourierItem(
    courier: UserModel,
    onItemClicked: (UserModel) -> Unit,
    onItemBan: (UserModel) -> Unit,
    onItemDelete: (UserModel) -> Unit,
) {
    Box(modifier = Modifier
        .clickable {
            onItemClicked(courier)
        }
        .background(
            color = colorResource(id = R.color.back), shape = RoundedCornerShape(16.dp)
        )
        .padding(15.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                contentDescription = "ban courier", painter = (when (courier.status) {
                    StatusModel.OFFLINE -> painterResource(id = R.drawable.inactive_circle)
                    StatusModel.ONLINE -> painterResource(id = R.drawable.active_circle)
                }), modifier = Modifier
                    .size(20.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = courier.name,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically),
                color = AppTheme.colors.textColor
            )
            Spacer(modifier = Modifier.width(5.dp))
            Image(
                contentDescription = "ban courier", painter = if (courier.isBan) {
                    painterResource(id = R.drawable.block_active)
                } else {
                    painterResource(id = R.drawable.block_negative)
                }, modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = { onItemBan(courier) })
            )
            Image(
                contentDescription = "ban courier",
                painter = painterResource(R.drawable.delete),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = { onItemDelete(courier) })
            )
        }
    }
}