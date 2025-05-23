package ru.krymer.delivery.ui.screens.shared

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.di.TokenManager
import ru.krymer.delivery.data.api.FactoryApi
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.dao.FailedDao
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.user.StatusModel
import ru.krymer.delivery.data.model.user.getStringByRole
import ru.krymer.delivery.data.model.user.getStringByStatus
import ru.krymer.delivery.data.model.utilModel.MessageModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.request.UpdateUserRequest
import ru.krymer.delivery.di.AppPreferencesManager
import ru.krymer.delivery.di.RetryManager
import ru.krymer.delivery.ui.screens.shared.models.AuthAction
import ru.krymer.delivery.ui.screens.shared.models.SharedEvents
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState
import ru.krymer.delivery.utills.Constants
import java.security.PrivateKey
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val userApi: UserApi,
    private val tokenManager: TokenManager,
    private val factoryApi: FactoryApi,
    private val database: AppDatabase,
    private val manager: AppPreferencesManager,
    private val retryManager: RetryManager
) : ViewModel(), EventHandler<SharedEvents> {

    override fun obtainEvent(event: SharedEvents) {
        when(event) {
            SharedEvents.ClearToken -> clearTokenData()
        }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                message(e.message)
            }
        }
    }

    private val _viewState = MutableStateFlow(SharedViewState())
    val viewState = _viewState.asStateFlow()
    fun updateViewState(update: (SharedViewState) -> SharedViewState) {
        _viewState.update { update(it) }
    }

    init {
        val keyFont = manager.getIntData(Constants.KEYS.FONT) ?: 0
        updateViewState { it.copy(currentFont = MutableStateFlow(keyFont)) }
        initAuth()
        retryFailedRequests()
    }

    fun retryFailedRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            retryManager.retryFailedRequests()
        }
    }

    fun initCurrentRoute(route: RouteModel) {
        updateViewState { it.copy(currentRoute = route) }
    }

    fun initSysAdm(): Boolean {
        val status = viewState.value.user.value?.role?.getStringByRole() in listOf(
            Constants.Role.SYSTEM, Constants.Role.ADMIN
        )
        return status
    }

    fun initSysAdmMod(): Boolean {
        val status = viewState.value.user.value?.role?.getStringByRole() in listOf(
            Constants.Role.SYSTEM, Constants.Role.ADMIN, Constants.Role.MODERATOR
        )
        return status
    }

    fun clearTokenData() {
        tokenManager.deleteToken()
        updateViewState {
            it.copy(
                user = MutableStateFlow(null),
                factory = null,
                authAction = AuthAction.Unauthorized,
                isUserBlocked = false
            )
        }
        message("Требуется повторная авторизация!")
    }

    private fun initAuth() {
        launchCoroutine {
            val user = database.userDao().getUser()
            val factory = database.factoryDao().getFactory()
            val token = tokenManager.getAccessToken()
            if (user != null && factory != null && token != null) {
                updateViewState { it.copy(user = MutableStateFlow(user), factory = factory) }
                authorized()
            }
            if (token != null) {
                checkValidityToken(token)
            } else {
                unauthorized()
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
            clearTokenData()
        } else {
            message(response.message)
        }
    }

    private fun unauthorized() {
        updateViewState { it.copy(authAction = AuthAction.Unauthorized) }
    }

    fun authorized() {
        launchCoroutine {
            updateViewState { it.copy(authAction = AuthAction.Authorized) }
            val token = tokenManager.getAccessToken()
            if (token != null) {
                loadUserData()
            } else {
                unauthorized()
            }
        }
    }

    fun message(message: String?, typeMessageModel: TypeMessageModel = TypeMessageModel.ERROR) {
        val listMessage = viewState.value.listMessage.value.map { it.copy() }.toMutableList()
        val obj = MessageModel(
            id = listMessage.lastIndex.toLong(),
            message = message ?: Constants.ERROR.ERROR,
            type = typeMessageModel
        )
        listMessage.add(obj)
        updateViewState { it.copy(listMessage = MutableStateFlow(listMessage)) }
    }

    private fun checkValidityToken(accessToken: String) {
        launchCoroutine {
            val response = withContext(Dispatchers.IO) {
                userApi.refreshToken(token = Constants.TOKEN.TOKEN_TYPE + accessToken)
            }
            if (response.success) {
                val tokens = response.obj
                if (tokens != null) {
                    tokenManager.saveAccessToken(tokens.accessToken)
                    authorized()
                }
            } else {
                message(response.message)
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
                message(response.message)
            }
        }
    }

    private fun loadFactoryData(idFactory: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
                    message(response.message)
                }
            } catch (e: Exception) {
                message(message = e.message)
            }
        }
    }



    fun saveRouteList(list: List<RouteModel>) {
        updateViewState { it.copy(routeList = list) }
    }

    fun firstRouteToId(id: Long): RouteModel? = _viewState.value.routeList.firstOrNull {
        it.id == id
    }

    fun getListRoute(): List<RouteModel> = viewState.value.routeList

    fun updateFactory(factoryModel: FactoryModel) {
        updateViewState { it.copy(factory = factoryModel) }
    }


}