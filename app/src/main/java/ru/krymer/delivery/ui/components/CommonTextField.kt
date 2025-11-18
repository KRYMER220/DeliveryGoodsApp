package ru.krymer.delivery.ui.components

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
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
    infoValue: String = "",
    infoAlign: TextAlign = TextAlign.Unspecified,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textStyle: TextStyle = AppTheme.typography.titleMedium,
    isNotCenter: Boolean = false,
    autoClearFocus: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Column(modifier = modifier.heightIn(min = 60.dp, max = Dp.Unspecified)) {
        Text(
            text = infoValue,
            color = AppTheme.colors.onSecondary,
            style = AppTheme.typography.bodySmall,
            textAlign = infoAlign,
            modifier = Modifier.padding(3.dp)
        )
        TextField(
            value = value,
            placeholder = {
                if (!isFocused && value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = AppTheme.typography.titleSmall,
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
            onValueChange = { newValue ->
                changerText(newValue)
            },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                errorTextColor = AppTheme.colors.onSecondary,
                errorContainerColor = AppTheme.colors.secondary,
                focusedTextColor = AppTheme.colors.onSecondary,
                unfocusedTextColor = AppTheme.colors.onSecondary,
                focusedContainerColor = AppTheme.colors.secondary,
                unfocusedContainerColor = AppTheme.colors.secondary,
                disabledContainerColor = AppTheme.colors.secondary,
                cursorColor = AppTheme.colors.onSecondary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ), modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onKeyEvent { keyEvent ->
                    if (autoClearFocus && keyEvent.key == Key.Enter) {
                        focusManager.clearFocus()
                        true
                    } else {
                        false
                    }
                },
            keyboardActions = KeyboardActions(
                onDone = {
                    if (autoClearFocus) {
                        focusManager.clearFocus()
                    }
                    keyboardActions.onDone?.invoke(this)
                },
                onGo = {
                    if (autoClearFocus) {
                        focusManager.clearFocus()
                    }
                    keyboardActions.onGo?.invoke(this)
                },
                onNext = {
                    if (autoClearFocus) {
                        focusManager.clearFocus()
                    }
                    keyboardActions.onNext?.invoke(this)
                },
                onPrevious = {
                    if (autoClearFocus) {
                        focusManager.clearFocus()
                    }
                    keyboardActions.onPrevious?.invoke(this)
                },
                onSearch = {
                    if (autoClearFocus) {
                        focusManager.clearFocus()
                    }
                    keyboardActions.onSearch?.invoke(this)
                },
                onSend = {
                    if (autoClearFocus) {
                        focusManager.clearFocus()
                    }
                    keyboardActions.onSend?.invoke(this)
                }
            ),
            textStyle = if (isNotCenter) textStyle else textStyle.copy(textAlign = TextAlign.Center)
        )
        if (isError) {
            Text(
                text = errorValue,
                color = Color.Red,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}