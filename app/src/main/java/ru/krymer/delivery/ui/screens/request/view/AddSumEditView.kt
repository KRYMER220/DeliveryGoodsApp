package ru.krymer.delivery.ui.screens.request.view

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun AddSumEditView(
    changeAddSum: (String) -> Unit, saveAddSum: () -> Unit
) {
    var addSum by remember { mutableStateOf("") }
    val errorEmpty = stringResource(R.string.empty_input)
    val errorNumber = stringResource(R.string.error_num)
    var errorAddSum by remember {
        mutableStateOf(
            Error(
                visible = true,
                error = errorEmpty
            )
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        CommonTextField(
            value = addSum,
            placeholder = stringResource(R.string.add_sum),
            changerText = { str ->
                addSum = str
                errorAddSum = when {
                    str == "" -> Error(visible = true, error = errorEmpty)
                    !startsWithDigit(str) -> Error(
                        visible = true,
                        error = errorNumber
                    )

                    else -> {
                        changeAddSum(str)
                        Error()
                    }
                }

            },
            modifier = Modifier.weight(0.6f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            textStyle = AppTheme.typography.titleLarge,
            errorValue = errorAddSum.error,
            isError = errorAddSum.visible
        )
        Image(
            contentDescription = null,
            painter = painterResource(id = R.drawable.submit),
            modifier = Modifier
                .weight(0.3f)
                .size(50.dp)
                .combinedClickable(onClick = {
                    if (addSum.isNotEmpty()) saveAddSum() else Error(
                        visible = true,
                        error = errorEmpty
                    )
                })
        )
    }
}