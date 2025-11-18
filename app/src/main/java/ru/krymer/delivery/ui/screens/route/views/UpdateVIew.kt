package ru.krymer.delivery.ui.screens.route.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.route.models.RouteViewState

@Composable
fun UpdateRouteView(
    viewState: RouteViewState, changeName: (String) -> Unit
) {
    viewState.route?.let {
        var name by remember { mutableStateOf(it.name) }
        var errorName by remember { mutableStateOf(Error()) }
        val errorEmpty = stringResource(R.string.empty_input)

        Column {
            CommonTextField(
                value = name,
                placeholder = stringResource(
                    id = R.string.name
                ),
                changerText = { str ->
                    name = str
                    errorName = when {
                        str == "" -> Error(visible = true, error = errorEmpty)
                        else -> {
                            changeName(str)
                            Error()
                        }
                    }
                },
                infoValue = stringResource(R.string.name),
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = errorName.visible,
                errorValue = errorName.error
            )
        }
    }
}