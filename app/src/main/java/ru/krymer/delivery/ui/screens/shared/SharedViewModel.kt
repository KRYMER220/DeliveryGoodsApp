package ru.krymer.delivery.ui.screens.shared

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.Screens
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.FactoryApi
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.utilModel.MessageModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.di.AppPreferencesManager
import ru.krymer.delivery.di.TokenManager
import ru.krymer.delivery.ui.screens.shared.models.SharedEvents
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState
import ru.krymer.delivery.utills.Constants
import javax.inject.Inject
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.math.min

@OptIn(ExperimentalAtomicApi::class)
@HiltViewModel
class SharedViewModel @Inject constructor(
    private val userApi: UserApi,
    private val tokenManager: TokenManager,
    private val factoryApi: FactoryApi,
    private val database: AppDatabase,
    private val manager: AppPreferencesManager
) : ViewModel(), EventHandler<SharedEvents> {

    private val messageId = AtomicLong(System.currentTimeMillis())
    var backStack = mutableStateListOf<Screens>(Screens.Splash)

    override fun obtainEvent(event: SharedEvents) {
        when(event) {
            SharedEvents.ClearToken -> clearTokenData()
            SharedEvents.LogOut -> launchCoroutine { logout() }
            is SharedEvents.DeleteMessage -> deleteMessage(event.id)
            SharedEvents.ChangeSettings -> changeSettings()
            SharedEvents.OpenHideSettingsApp -> showHideSettings()
            is SharedEvents.ChangeFontSizeIndex -> changeFontSize(event.index)
            SharedEvents.OpenHideChangerPass -> showHideChangerPass()
            is SharedEvents.ChangePass -> changePass(event.oldPass, event.newPass)
        }
    }

    private fun changePass(oldPass: String, newPass: String) {
        launchCoroutine {
            val response = userApi.changePass(oldPass = oldPass, newPass = newPass)
            if (response.success) {
                message("Пароль успешно изменен!", type = TypeMessageModel.SUCCEED)
                showHideChangerPass()
            }
        }
    }

    private fun showHideChangerPass() {
        updateViewState { it.copy(isShowPassChanger = !it.isShowPassChanger) }
    }

    private fun changeFontSize(index: Int) {
        updateViewState { it.copy(fontSizeIndex = index) }
        manager.saveInt(Constants.KEYS.FONT_SIZE, index)
    }

    private fun changeSettings() {
        updateViewState { it.copy(lightVersion = !it.lightVersion) }
        val settingsValue = _viewState.value.lightVersion
        manager.saveBoolean(key = Constants.KEYS.SETTINGS, data = settingsValue)
    }

    private fun showHideSettings() {
        updateViewState { it.copy(isShowSettings = !it.isShowSettings) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
                message(Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR)
            } catch (e: Exception) {
                message(e.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    private suspend fun <T> retryWithBackoff(
        attempts: Int = 5,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 30_000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastError: Throwable? = null
        repeat(attempts - 1) {
            try {
                return block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                lastError = e
                delay(currentDelay)
                currentDelay = min((currentDelay * factor).toLong(), maxDelayMs)
            }
        }
        try {
            return block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            throw (lastError ?: e)
        }
    }

    private val _viewState = MutableStateFlow(SharedViewState())
    val viewState = _viewState.asStateFlow()
    fun updateViewState(update: (SharedViewState) -> SharedViewState) {
        _viewState.update { update(it) }
    }

    init {
        initAuth()
        val settings = manager.getBooleanData(Constants.KEYS.SETTINGS)
        val savedSize = manager.getIntData(Constants.KEYS.FONT_SIZE) ?: 2
        if (settings != null) {
            updateViewState { it.copy(lightVersion = settings,fontSizeIndex = savedSize) }
        }
    }

    fun clearTokenData() {
        tokenManager.deleteToken()
        updateViewState {
            it.copy(
                user = null,
                factory = null,
                isUserBlocked = false,
            )
        }
    }

    fun initAuth() {
        launchCoroutine {
            val token = tokenManager.getAccessToken()
            val isAuth = manager.getBooleanData(Constants.KEYS.AUTH)
            if (token != null) {
                if (isAuth != false) {
                    withContext(Dispatchers.Main) {
                        backStack.clear()
                        backStack.add(Screens.Menu)
                        localAuth(token = token)
                    }
                } else {
                    checkValidityToken(token)
                }
            } else {
                withContext(Dispatchers.Main) {
                    backStack.clear()
                    backStack.add(Screens.Auth)
                }
            }
        }
    }

    private fun localAuth(token: String) {
        launchCoroutine {
            val user = database.userDao().getUser()
            user?.let {
                updateViewState { it.copy(user = user) }
                checkValidityToken(token = token)
            } ?: run {
                checkValidityToken(token = token)
            }
        }
    }

    suspend fun logout() {
        try {
            val response = retryWithBackoff(attempts = 3, initialDelayMs = 2000) {
                userApi.logout()
            }
            database.factoryDao().deleteFactory()
            database.userDao().delete()
            manager.delete(Constants.KEYS.AUTH)
            tokenManager.deleteToken()
            withContext(Dispatchers.Main) {
                backStack.clear()
                backStack.add(Screens.Auth)
                clearTokenData()
            }
            if (!response.success) {
                message(response.message, TypeMessageModel.ERROR)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            message(e.message, TypeMessageModel.ERROR)
            withContext(Dispatchers.Main) {
                backStack.clear()
                backStack.add(Screens.Auth)
                clearTokenData()
            }
        }
    }

    private fun deleteMessage(id: Long) {
        launchCoroutine {
            updateViewState { state ->
                state.copy(
                    listMessage = state.listMessage.filter { it.id != id }
                )
            }
        }
    }

    fun message(message: String?, type: TypeMessageModel = TypeMessageModel.ERROR) {
        launchCoroutine {
            val obj = MessageModel(
                id = messageId.incrementAndFetch(),
                message = message ?: Constants.ERROR.ERROR,
                type = type
            )
            updateViewState { it.copy(listMessage = it.listMessage + obj) }
        }
    }

    private fun checkValidityToken(token: String) {
        launchCoroutine {
            val response = retryWithBackoff(
                attempts = 4,
                initialDelayMs = 2000,
                maxDelayMs = 30_000,
                factor = 2.0
            ) {
                userApi.refreshToken(token = Constants.TOKEN.TOKEN_TYPE + token)
            }
            if (response.success) {
                val tokens = response.obj
                if (tokens != null) {
                    tokenManager.saveAccessToken(tokens.accessToken)
                    loadUserData()
                    val isAuth = manager.getBooleanData(Constants.KEYS.AUTH)
                    if (isAuth == false) {
                        withContext(Dispatchers.Main) {
                            backStack.clear()
                            backStack.add(Screens.Menu)
                        }
                    }
                } else {
                    message(Constants.ERROR.AUTH_SIGN, TypeMessageModel.ERROR)
                }
            } else {
                message(response.message, TypeMessageModel.ERROR)
            }
        }
    }

    private fun loadUserData() {
        launchCoroutine {
            val response = retryWithBackoff(
                attempts = 4,
                initialDelayMs = 2000,
                maxDelayMs = 30_000,
                factor = 2.0
            ) {
                userApi.getData()
            }

            if (response.success) {
                val user = response.obj
                user?.let {
                    if (user.isBan) updateViewState { it.copy(isUserBlocked = true) }
                    else {
                        updateViewState {
                            it.copy(
                                user = user, isUserBlocked = false
                            )
                        }
                        val localUser = database.userDao().getUser()
                        if (localUser != null) {
                            database.userDao().updateUser(user = user)
                        } else {
                            database.userDao().insertUser(user = user)
                        }
                        loadFactoryData(idFactory = user.idFactory)
                    }
                } ?: run {
                    message(Constants.ERROR.NOT_FOUND, TypeMessageModel.ERROR)
                }
            } else {
                message(response.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun loadFactoryData(idFactory: Long) {
        launchCoroutine {
            val response = retryWithBackoff(
                attempts = 4,
                initialDelayMs = 2000,
                maxDelayMs = 15_000,
                factor = 2.0
            ) {
                factoryApi.getFactoryById(id = idFactory)
            }

            if (response.success) {
                val factory = response.obj
                factory?.let {
                    val localFactory = database.factoryDao().getFactoryById(factory.id)
                    if (localFactory != null) {
                        database.factoryDao().updateFactory(factory = factory)
                    } else {
                        database.factoryDao().insertFactory(factory = factory)
                    }
                    updateViewState {
                        it.copy(
                            factory = factory
                        )
                    }
                } ?: run {
                    message(Constants.ERROR.NOT_FOUND, TypeMessageModel.ERROR)

                }
            } else {
                message(response.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    fun updateFactory(factory: FactoryModel) {
        updateViewState { it.copy(factory = factory) }
    }
}