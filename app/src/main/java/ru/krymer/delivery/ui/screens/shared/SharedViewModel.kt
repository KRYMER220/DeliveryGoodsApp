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
import ru.krymer.delivery.data.model.utilModel.MessageModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.di.AppPreferencesManager
import ru.krymer.delivery.di.TokenManager
import ru.krymer.delivery.ui.screens.shared.models.SharedEvents
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState
import ru.krymer.delivery.utills.Constants
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val userApi: UserApi,
    private val tokenManager: TokenManager,
    private val factoryApi: FactoryApi,
    private val database: AppDatabase,
    private val manager: AppPreferencesManager
) : ViewModel(), EventHandler<SharedEvents> {

    var backStack = mutableStateListOf<Screens>(Screens.Splash)

    override fun obtainEvent(event: SharedEvents) {
        when(event) {
            SharedEvents.ClearToken -> clearTokenData()
            SharedEvents.LogOut -> launchCoroutine { logout() }
            is SharedEvents.DeleteMessage -> deleteMessage(event.id)
            SharedEvents.ChangeSettings -> changeSettings()
            SharedEvents.OpenHideSettingsApp -> showHideSettings()
            is SharedEvents.ChangeFontSizeIndex -> changeFontSize(event.index)
        }
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
        updateViewState { it.copy(isShowSettings = MutableStateFlow( !it.isShowSettings.value )) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                message(Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR)
            } catch (e: Exception) {
                message(e.message, type = TypeMessageModel.ERROR)
            }
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
                user = MutableStateFlow(null),
                factory = MutableStateFlow(null),
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
                    clearTokenData()
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
                updateViewState { it.copy(user = MutableStateFlow(user)) }
                checkValidityToken(token = token)
            } ?: run {
                checkValidityToken(token = token)
            }
        }
    }

    suspend fun logout() {
        val response = userApi.logout()
        withContext(Dispatchers.Main) {
            database.factoryDao().deleteFactory()
            database.userDao().delete()
            backStack.clear()
            backStack.add(Screens.Auth)
            clearTokenData()
        }
        if (!response.success) {
            delay(5000)
            logout()
        }
    }

    private fun deleteMessage(id: Long) {
        launchCoroutine {
            updateViewState { state ->
                state.copy(
                    listMessage = MutableStateFlow(
                        state.listMessage.value.filter { it.id != id }
                    )
                )
            }
        }
    }

    fun message(message: String?, type: TypeMessageModel = TypeMessageModel.ERROR) {
        launchCoroutine {
            val obj = MessageModel(
                id = System.currentTimeMillis(),
                message = message ?: Constants.ERROR.ERROR,
                type = type
            )
            updateViewState { it.copy(listMessage = MutableStateFlow(it.listMessage.value + obj)) }
        }
    }

    private fun checkValidityToken(token: String) {
        launchCoroutine {
            val response = withContext(Dispatchers.IO) {
                userApi.refreshToken(token = Constants.TOKEN.TOKEN_TYPE + token)
            }
            if (response.success) {
                val tokens = response.obj
                if (tokens != null) {
                    tokenManager.saveAccessToken(tokens.accessToken)
                    loadUserData()
                    withContext(Dispatchers.Main) {
                        backStack.clear()
                        backStack.add(Screens.Menu)
                    }
                }
            } else {
                delay(7000)
                checkValidityToken(token = token)
                message(response.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun loadUserData() {
        launchCoroutine {
            val response = userApi.getData()
            if (response.success) {
                val user = response.obj
                user?.let {
                    if (user.isBan) updateViewState { it.copy(isUserBlocked = true) }
                    else {
                        updateViewState {
                            it.copy(
                                user = MutableStateFlow(user), isUserBlocked = false
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
                    loadUserData()
                }
            } else {
                message(response.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun loadFactoryData(idFactory: Long) {
        launchCoroutine {
            val response =
                factoryApi.getFactoryById(id = idFactory)
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
                            factory = MutableStateFlow(factory)
                        )
                    }
                } ?: run {
                    delay(7000)
                    loadFactoryData(idFactory = idFactory)
                }
            } else {
                database.factoryDao().deleteFactory()
                delay(7000)
                loadFactoryData(idFactory = idFactory)
                message(response.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    fun updateFactory(factory: FactoryModel) {
        updateViewState { it.copy(factory = MutableStateFlow(factory)) }
    }
}