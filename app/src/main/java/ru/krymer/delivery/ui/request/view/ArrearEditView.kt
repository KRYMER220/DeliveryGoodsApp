package ru.krymer.delivery.ui.request.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun ArrearsEditView(
    changeArrears: (String) -> Unit, saveArrear: () -> Unit
) {
    var arrears by remember { mutableStateOf("") }
    var errorArrears by remember {
        mutableStateOf(
            Error(
                visible = true,
                error = Constants.EMPTY.EMPTY_FIELD
            )
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        CommonTextField(
            value = arrears,
            placeholder = "Долг",
            changerText = { str ->
                arrears = str
                errorArrears = when {
                    str == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    !startsWithDigit(str) -> Error(
                        visible = true,
                        error = Constants.ERROR.ERROR_NUMBER_INPUT
                    )

                    else -> {
                        changeArrears(str)
                        Error()
                    }
                }
            },
            modifier = Modifier.weight(0.6f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            textStyle = AppTheme.typography.titleLarge,
            errorValue = errorArrears.error,
            isError = errorArrears.visible
        )
        Image(
            contentDescription = "submit",
            painter = painterResource(id = R.drawable.submit),
            modifier = Modifier
                .weight(0.3f)
                .size(50.dp)
                .combinedClickable(onClick = { if (!errorArrears.visible) saveArrear() })
        )
    }
}