package ru.krymer.delivery

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
    data class Request(val shop: ShopModel) : Screens()
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
                    val isCriticalUpdate =
                        isCriticalUpdate(currentVersionCode, availableVersionCode)

                    val updateType = when {
                        isCriticalUpdate && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                            Log.d("Updater", "Critical update detected, using IMMEDIATE")
                            AppUpdateType.IMMEDIATE
                        }

                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                            Log.d("Updater", "Standard update, using FLEXIBLE")
                            AppUpdateType.FLEXIBLE
                        }

                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.SILENT) -> {
                            Log.d("Updater", "Using SILENT update as fallback")
                            AppUpdateType.SILENT
                        }

                        else -> {
                            Log.w("Updater", "Update available, but no supported types")
                            return@addOnSuccessListener
                        }
                    }

                    startUpdateFlow(
                        updateManager = updateManager,
                        appUpdateInfo = appUpdateInfo,
                        updateType = updateType
                    )
                } else {
                    Log.d("Updater", "No update available")
                }
            }.addOnFailureListener { throwable ->
                Log.e("Updater", "getAppUpdateInfo error", throwable)
            }
    }

    private fun startUpdateFlow(
        updateManager: RuStoreAppUpdateManager,
        appUpdateInfo: AppUpdateInfo,
        updateType: Int
    ) {
        val options = AppUpdateOptions.Builder()
            .appUpdateType(updateType)
            .build()

        updateManager.startUpdateFlow(appUpdateInfo, options)
            .addOnSuccessListener {
                Log.d("Updater", "Update flow started successfully for type: $updateType")

                if (updateType == AppUpdateType.FLEXIBLE) {
                    setupFlexibleUpdateListener(updateManager, options)
                }
            }
            .addOnFailureListener { throwable ->
                Log.e("Updater", "startUpdateFlow error", throwable)
            }
    }

    private fun setupFlexibleUpdateListener(
        updateManager: RuStoreAppUpdateManager,
        options: AppUpdateOptions
    ) {
        updateManager.registerListener { state ->
            when (state.installStatus) {
                InstallStatus.DOWNLOADING -> {
                    val percent = if (state.totalBytesToDownload > 0) {
                        (state.bytesDownloaded * 100 / state.totalBytesToDownload).toInt()
                    } else {
                        0
                    }
                    Log.d("Updater", "Downloading update: $percent%")
                }

                InstallStatus.DOWNLOADED -> {
                    Log.d("Updater", "Update downloaded, ready to install")

                    updateManager.completeUpdate(options)
                        .addOnSuccessListener {
                            Log.d("Updater", "Update completed successfully")
                        }
                        .addOnFailureListener { throwable ->
                            Log.e("Updater", "completeUpdate error", throwable)
                        }
                }

                InstallStatus.FAILED -> {
                    Log.e("Updater", "Update failed with status: ${state.installErrorCode}")
                }

                else -> Unit
            }
        }
    }

    private fun isCriticalUpdate(currentVersion: Long, availableVersion: Long): Boolean {
        val currentMajor = currentVersion / 1000
        val availableMajor = availableVersion / 1000
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
