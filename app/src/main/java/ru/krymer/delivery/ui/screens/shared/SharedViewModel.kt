package ru.krymer.delivery.ui.screens.shared

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
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
import ru.krymer.delivery.data.model.user.getStringByRole
import ru.krymer.delivery.data.model.utilModel.MessageModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.di.SecureDataStore
import ru.krymer.delivery.di.getBoolean
import ru.krymer.delivery.ui.screens.shared.models.SharedEvents
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState
import ru.krymer.delivery.utills.Constants
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val userApi: UserApi,
    private val factoryApi: FactoryApi,
    private val database: AppDatabase, private val secureDataStore: SecureDataStore
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
        launchCoroutine {
            updateViewState { it.copy(fontSizeIndex = index) }
            secureDataStore.putString(Constants.KEYS.FONT_SIZE, index.toString())
        }
    }

    private fun changeSettings() {
        launchCoroutine {
            updateViewState { it.copy(lightVersion = !it.lightVersion) }
            val settingsValue = _viewState.value.lightVersion
            secureDataStore.putString(Constants.KEYS.SETTINGS, settingsValue.toString())
        }
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
        initSettings()
    }

    private fun initSettings() {
        launchCoroutine {
            val settings =
                secureDataStore.getString(Constants.KEYS.SETTINGS)?.toBooleanStrictOrNull()
            val savedSize = secureDataStore.getString(Constants.KEYS.FONT_SIZE)?.toIntOrNull() ?: 2
            if (settings != null) {
                updateViewState { it.copy(lightVersion = settings, fontSizeIndex = savedSize) }
            }
        }
    }

    fun clearTokenData() {
<<<<<<< HEAD
        launchCoroutine {
            secureDataStore.clear()
            updateViewState {
                it.copy(
                    user = MutableStateFlow(null),
                    factory = MutableStateFlow(null),
                    isUserBlocked = false,
                )
            }
=======
        tokenManager.deleteToken()
        updateViewState {
            it.copy(
                user = MutableStateFlow(null),
                factory = null,
                isUserBlocked = false,
            )
>>>>>>> parent of 359f480 (fix)
        }
        message(Constants.ERROR.AUTH)
    }

    fun initAuth() {
        launchCoroutine {
            val token = secureDataStore.getString(Constants.TOKEN.ACCESS)
            val isAuth = secureDataStore.getBoolean(Constants.KEYS.AUTH) == true

            if (token != null) {
                withContext(Dispatchers.Main) {
                    if (isAuth != false && isAuth != null) {
                        backStack.clear()
                        backStack.add(Screens.Menu)
                        checkValidityToken(token)
                    } else {
                        checkValidityToken(token)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    backStack.clear()
                    backStack.add(Screens.Auth)
                }
            }
        }
    }


    suspend fun logout() {
        val response = userApi.logout()
        if (response.success) {
            val user = viewState.value.user.value
            val factory = viewState.value.factory
            if (user != null && factory != null) {
                database.userDao().deleteUser(user)
                database.factoryDao().deleteFactory(factory)
            }
            withContext(Dispatchers.Main) {
                backStack.clear()
                backStack.add(Screens.Auth)
            }
            clearTokenData()
        } else {
            message(response.message, type = TypeMessageModel.ERROR)
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

    private fun checkValidityToken(accessToken: String) {
        launchCoroutine {
            val response = withContext(Dispatchers.IO) {
                userApi.refreshToken(token = Constants.TOKEN.TOKEN_TYPE + accessToken)
            }
            if (response.success) {
                val tokens = response.obj
                if (tokens != null) {
<<<<<<< HEAD
                    secureDataStore.putString(Constants.TOKEN.ACCESS, tokens.accessToken)
=======
                    tokenManager.saveAccessToken(tokens.accessToken)
                    manager.saveBoolean(Constants.KEYS.AUTH, true)
<<<<<<< HEAD
>>>>>>> parent of 359f480 (fix)
=======
>>>>>>> parent of 359f480 (fix)
                    loadUserData()
                    withContext(Dispatchers.Main) {
                        backStack.clear()
                        backStack.add(Screens.Menu)
                    }
                }
            } else {
                message(response.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun loadUserData() {
        launchCoroutine {
            val response = userApi.getData()
            if (response.success) {
                val user = response.obj
                if (user != null) {
                    if (user.isBan) updateViewState { it.copy(isUserBlocked = true) }
                    else {
                        updateViewState {
                            it.copy(
                                user = MutableStateFlow(user), isUserBlocked = false
                            )
                        }
                        val localUser = database.userDao().getUserById(user.id)
                        if (localUser != null) {
                            database.userDao().updateUser(user = user)
                        } else {
                            database.userDao().insertUser(user = user)
                        }
                        loadFactoryData(idFactory = user.idFactory)
                    }
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
                if (factory != null) {
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
                }
            } else {
                message(response.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    fun updateFactory(factoryModel: FactoryModel) {
        updateViewState { it.copy(factory = factoryModel) }
    }
}