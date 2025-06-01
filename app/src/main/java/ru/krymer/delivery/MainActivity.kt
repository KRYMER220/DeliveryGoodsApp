package ru.krymer.delivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.ui.screens.ApplicationScreen
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.theme.BoxTheme

sealed class Screens: NavKey {
    @Serializable
    data object Auth : Screens()
    @Serializable
    data object Menu: Screens()
    @Serializable
    data object Route: Screens()
    @Serializable
    data class Client(val route: RouteModel, val routes: List<RouteModel>): Screens()
    @Serializable
    data object Trip: Screens()
    @Serializable
    data object Analitic: Screens()
    @Serializable
    data class Shop(val trip: TripModel): Screens()
    @Serializable
    data object Splash: Screens()
    @Serializable
    data object Courier: Screens()
    @Serializable
    data object Product: Screens()
    @Serializable
    data object Ban: Screens()
}

@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sharedViewModel = viewModel<SharedViewModel>()
            val state = sharedViewModel.viewState.collectAsState()
            val backStack = sharedViewModel.backStack
            BoxTheme(content = {
                ApplicationScreen(
                    backStack = backStack,
                    sharedState = state,
                    event = sharedViewModel::obtainEvent,
                    modifier = Modifier.navigationBarsPadding()
                )
            })
        }
    }
}
