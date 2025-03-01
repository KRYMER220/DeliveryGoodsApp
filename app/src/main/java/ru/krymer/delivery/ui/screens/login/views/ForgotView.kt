package ru.krymer.delivery.ui.screens.login.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.login.models.LoginViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun ForgotView(
    viewState: LoginViewState,
    onLoginClick: () -> Unit,
    onRestorePassClick: () -> Unit,
    onEmailTFC: (String) -> Unit,
) {
    Column {
        CommonTextField(
            value = viewState.emailValue,
            placeholder = stringResource(id = R.string.email_hint),
            onVC = onEmailTFC,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = !viewState.isForgotProgress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.padding(top = 10.dp))
        Button(
            onClick = onRestorePassClick, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = !viewState.isForgotProgress,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {
            if (viewState.isForgotProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = stringResource(id = R.string.restore), style = TextStyle(
                        color = Color.White
                    ), fontSize = 20.sp
                )
            }
        }
        if (viewState.isError) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                Text(
                    text = viewState.errorValue,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.login_action),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable(onClick = onLoginClick),
                color = AppTheme.colors.onSecondary
            )
        }
    }
}