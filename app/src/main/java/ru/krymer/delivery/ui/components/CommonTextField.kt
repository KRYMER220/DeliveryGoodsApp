package ru.krymer.delivery.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun CommonTextField(
    value: String,
    placeholder: String,
    onVC: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    errorValue: String = "",
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textStyle: TextStyle = TextStyle.Default,
) {
    TextField(
        modifier = modifier,
        value = value,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.onSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        onValueChange = onVC,
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.secondary,
            unfocusedContainerColor = AppTheme.colors.secondary,
            disabledContainerColor = AppTheme.colors.secondary,
            cursorColor = AppTheme.colors.onSecondary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            unfocusedTextColor = AppTheme.colors.onSecondary,
            focusedTextColor = AppTheme.colors.onSecondary,
            disabledTextColor = AppTheme.colors.onSecondary,
        ),
        keyboardActions = keyboardActions,
        textStyle = textStyle
    )
    if (isError) {
        Text(text = errorValue, color = Color.Red, fontSize = 12.sp)
    }
}