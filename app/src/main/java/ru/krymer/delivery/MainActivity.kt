package ru.krymer.delivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.krymer.delivery.ui.screens.ApplicationScreen
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.theme.BoxTheme

@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sharedViewModel = hiltViewModel<SharedViewModel>()
            BoxTheme(content = {
                val navController = rememberNavController()
                ApplicationScreen(
                    navController = navController,
                    sharedState = sharedViewModel.viewState.collectAsState().value,
                    event = sharedViewModel::obtainEvent
                )
            }, state = sharedViewModel.viewState.collectAsState().value)
        }
    }
}
