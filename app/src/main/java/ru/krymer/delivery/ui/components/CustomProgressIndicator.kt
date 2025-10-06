package ru.krymer.delivery.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomCircularProgressIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.Center),
            strokeWidth = 2.dp,
            color = Color.White
        )
    }
}
