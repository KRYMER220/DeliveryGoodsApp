package ru.krymer.delivery.ui.screens.route.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.route.models.RouteViewState
import ru.krymer.delivery.utills.Constants

@Composable
fun AddRouteView(
    changeName: (String) -> Unit
) {

    var name by remember { mutableStateOf(Constants.EMPTY.EMPTY_STRING) }
    var errorName by remember { mutableStateOf(Error()) }

    Column {
        CommonTextField(
            value = name,
            placeholder = stringResource(
                id = R.string.name
            ),
            changerText = { str ->
                name = str
                errorName = when {
                    str == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    else -> {
                        changeName(str)
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = errorName.visible,
            errorValue = errorName.error
        )
    }
}