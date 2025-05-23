package ru.krymer.delivery.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    changerText: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    errorValue: String = "",
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textStyle: TextStyle = TextStyle.Default,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value


    TextField(
        modifier = modifier,
        value = value,
        placeholder = {
            if (!isFocused && value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = AppTheme.typography.titleLarge,
                    color = AppTheme.colors.onSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        onValueChange = changerText,
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            errorTextColor = AppTheme.colors.onSecondary,
            errorContainerColor = AppTheme.colors.secondaryVariant,
            focusedTextColor = AppTheme.colors.onSecondary,
            unfocusedTextColor = AppTheme.colors.onSecondary,
            focusedContainerColor = AppTheme.colors.secondaryVariant,
            unfocusedContainerColor = AppTheme.colors.secondaryVariant,
            disabledContainerColor = AppTheme.colors.secondaryVariant,
            cursorColor = AppTheme.colors.onSecondary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
        keyboardActions = keyboardActions,
        textStyle = textStyle
    )
    if (isError) {
        Text(text = errorValue, color = Color.Red, style = AppTheme.typography.titleSmall)
    }
}