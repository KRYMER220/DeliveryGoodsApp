package ru.krymer.delivery.ui.screens.courier.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.user.RoleModel
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.courier.models.CourierEvent
import ru.krymer.delivery.ui.screens.courier.models.CourierViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun UpdateCourierView(
    state: CourierViewState,
    event: (CourierEvent) -> Unit
) {
    val user = state.updatedUser
    user?.let {
        var username by remember { mutableStateOf(user.name) }
        var percent by remember { mutableStateOf("${user.percentSalary}") }
        var salary by remember { mutableStateOf("${user.salary.toInt()}") }
        var errorSalary by remember { mutableStateOf(Error()) }
        var errorPercent by remember { mutableStateOf(Error()) }
        val roles = RoleModel.entries.toList() - RoleModel.SYSTEM

        Column {
            CommonTextField(
                value = username,
                placeholder = stringResource(
                    id = R.string.name
                ),
                changerText = {
                    username = it
                    event(CourierEvent.ChangeUsername(it))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = username.isEmpty(),
                errorValue = Constants.EMPTY.EMPTY_FIELD
            )
            Spacer(modifier = Modifier.height(10.dp))
            CommonTextField(
                value = percent,
                placeholder = stringResource(
                    id = R.string.percent_double
                ),
                changerText = {
                    percent = it
                    errorPercent = when {
                        it == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                        !startsWithDigit(it) -> Error(
                            visible = true,
                            error = Constants.ERROR.ERROR_NUMBER_INPUT
                        )

                        else -> {
                            event(CourierEvent.ChangeUsername(it))
                            Error()
                        }
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = errorPercent.visible,
                errorValue = errorPercent.error
            )
            Spacer(modifier = Modifier.height(10.dp))
            CommonTextField(
                value = salary,
                placeholder = stringResource(
                    id = R.string.salary
                ),
                changerText = {
                    salary = it
                    errorSalary = when {
                        it == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                        !startsWithDigit(it) -> Error(
                            visible = true,
                            error = Constants.ERROR.ERROR_NUMBER_INPUT
                        )

                        else -> {
                            event(CourierEvent.ChangeUserSalary(it))
                            Error()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = errorSalary.visible,
                errorValue = errorSalary.error
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            event(CourierEvent.DropDownMenuState(true))
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.userRole!!.name,
                        modifier = Modifier.padding(start = 15.dp),
                        color = AppTheme.colors.onSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    DropdownMenu(expanded = state.stateDropMenu, onDismissRequest = {
                        event(CourierEvent.DropDownMenuState(false))
                    }) {

                        roles.forEach { role ->
                            DropdownMenuItem(text = { Text(text = role.name) }, onClick = {
                                event(
                                    CourierEvent.SelectedItemMenu(
                                        role
                                    )
                                )
                                event(
                                    CourierEvent.DropDownMenuState(
                                        false
                                    )
                                )
                            })
                        }
                    }

                }
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Image(
                    contentDescription = "ban courier", painter = if (user.isBan) {
                        painterResource(id = R.drawable.block_active)
                    } else {
                        painterResource(id = R.drawable.block_negative)
                    }, modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = { event(CourierEvent.ShowBanDialog(it)) })
                )
            }
        }
    }
}
