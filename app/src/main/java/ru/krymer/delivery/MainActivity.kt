package ru.krymer.delivery

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.ui.screens.ApplicationScreen
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.theme.BoxTheme
import ru.rustore.sdk.appupdate.manager.RuStoreAppUpdateManager
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.AppUpdateInfo
import ru.rustore.sdk.appupdate.model.AppUpdateOptions
import ru.rustore.sdk.appupdate.model.AppUpdateType
import ru.rustore.sdk.appupdate.model.InstallStatus
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import androidx.core.net.toUri

@Serializable
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

    @Serializable
    data class Request(val shop: ShopModel, val shops: List<ShopModel>) : Screens()
}

@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var updateManager: RuStoreAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateManager = RuStoreAppUpdateManagerFactory.create(this)
        checkForUpdate(updateManager)
        enableEdgeToEdge()

        setContent {
            val sharedViewModel = hiltViewModel<SharedViewModel>()
            val state = sharedViewModel.viewState.collectAsState()
            val backStack = sharedViewModel.backStack
            BoxTheme(content = {
                ApplicationScreen(
                    backStack = backStack,
                    sharedState = state,
                    event = sharedViewModel::obtainEvent,
                    modifier = Modifier.navigationBarsPadding()
                )
            },fontSizeIndex = state.value.fontSizeIndex)
        }
    }

    private fun checkForUpdate(updateManager: RuStoreAppUpdateManager) {
        updateManager.getAppUpdateInfo().addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
                val currentVersionCode = getCurrentVersionCode()
                val availableVersionCode = appUpdateInfo.availableVersionCode
                showUpdateDialog(currentVersionCode, availableVersionCode)
            }
        }.addOnFailureListener { throwable ->
            Log.e("Updater", "getAppUpdateInfo error", throwable)
        }
    }

    private fun showUpdateDialog(currentVersion: Long, availableVersion: Long) {
        val isCriticalUpdate = isCriticalUpdate(currentVersion, availableVersion)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (isCriticalUpdate) "Требуется обновление" else "Доступно обновление")
            .setMessage("Доступна новая версия приложения. Перейдите в RuStore для установки обновления.")
            .setPositiveButton("Обновить") { _, _ ->
                openAppInRuStore()
            }

        if (!isCriticalUpdate) {
            dialog.setNegativeButton("Позже") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        }

        dialog.setCancelable(!isCriticalUpdate)
            .show()
    }

    private fun openAppInRuStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "rustore://details?id=$packageName".toUri()
                setPackage("ru.rustore.app")
            }
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = "https://apps.rustore.ru/app/$packageName".toUri()
                }
                startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "Не удалось открыть RuStore", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isCriticalUpdate(currentVersion: Long, availableVersion: Long): Boolean {
        val currentMajor = currentVersion / 10
        val availableMajor = availableVersion / 10
        return availableMajor > currentMajor
    }

    private fun getCurrentVersionCode(): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, 0).longVersionCode
            } else {
                @Suppress("DEPRECATION") packageManager.getPackageInfo(
                    packageName,
                    0
                ).versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("Version", "Package not found", e)
            0L
        }
    }
}
