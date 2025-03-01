package ru.krymer.delivery.ui.screens.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.krymer.delivery.data.TokenManager
import ru.krymer.delivery.data.api.FactoryApi
import ru.krymer.delivery.data.api.LoggerApi
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.StatusModel
import ru.krymer.delivery.data.model.user.getStringByRole
import ru.krymer.delivery.data.model.user.getStringByStatus
import ru.krymer.delivery.data.model.utilModel.MessageModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.request.LogRequest
import ru.krymer.delivery.data.request.UpdateUserRequest
import ru.krymer.delivery.ui.screens.shared.models.AuthAction
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState
import ru.krymer.delivery.utills.Constants
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val userApi: UserApi,
    private val tokenManager: TokenManager,
    private val factoryApi: FactoryApi,
    private val loggerApi: LoggerApi
) : ViewModel() {

    fun saveCurrentNavRoute(route: String) {
        updateViewState { it.copy(currentNavRoute = route) }
    }

    suspend fun updateUserStatus(statusModel: StatusModel) {
        try {
            val user = viewState.value.user
            if (user != null) {
                user.apply {
                    val updatedUser = UpdateUserRequest(
                        id = id,
                        login = login,
                        name = name,
                        phone = phone,
                        status = statusModel.getStringByStatus(),
                        role = role.getStringByRole(),
                        isBanned = isBanned,
                        percentSalary = percentSalary
                    )
                    userApi.updateUser(updatedUser)
                }
            } else {
                message(Constants.EMPTY.EMPTY_DATA)
            }
        } catch (e: Exception) {
            message(e.message)
        }
    }

    private val _viewState = MutableStateFlow(SharedViewState())
    val viewState: StateFlow<SharedViewState> = _viewState.asStateFlow()
    private fun updateViewState(update: (SharedViewState) -> SharedViewState) {
        _viewState.update { update(it) }
    }

    init {
        initAuth()
    }

    fun initCurrentRoute(route: RouteModel) {
        updateViewState { it.copy(currentRoute = route) }
    }

    fun initCurrentTrip(tripModel: TripModel) {
        updateViewState { it.copy(currentTrip = tripModel) }
    }

    fun initSysAdm(): Boolean {
        val status = viewState.value.user?.role?.getStringByRole() in listOf(
            Constants.Role.SYSTEM, Constants.Role.ADMIN
        )
        return status
    }

    fun initSysAdmMod(): Boolean {
        val status = viewState.value.user?.role?.getStringByRole() in listOf(
            Constants.Role.SYSTEM, Constants.Role.ADMIN, Constants.Role.MODERATOR
        )
        return status
    }

    fun initSys(): Boolean {
        val status = viewState.value.user?.role?.getStringByRole() == Constants.Role.SYSTEM
        return status
    }

    fun clearTokenData() {
        tokenManager.saveAccessToken(null)
        updateViewState {
            it.copy(
                isLoadUserData = false,
                user = null,
                isLoadSettingsData = false,
                authAction = AuthAction.Unauthorized,
                isUserBlocked = false
            )
        }
    }

    private fun initAuth() {
        val accessToken = tokenManager.getAccessToken()
        if (accessToken != null) {
            checkValidityToken(accessToken)
        } else {
            unauthorized()
        }
    }


    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userApi.logout()
                if (response.success) {
                    clearTokenData()
                    message(response.message)
                } else {
                    message(response.message)
                }
            } catch (e: Exception) {
                message(e.message)
            } finally {
                clearTokenData()
            }
        }
    }

    private fun unauthorized() {
        updateViewState { it.copy(authAction = AuthAction.Unauthorized) }
    }

    fun authorized(isSignIn: Boolean = false) {
        updateViewState { it.copy(authAction = AuthAction.Authorized) }
        loadUserData(accessToken = tokenManager.getAccessToken()!!, isSignIn)
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = withContext(Dispatchers.IO) {
                    userApi.refreshAccessToken(token = Constants.TOKEN.TOKEN_TYPE + accessToken)
                }
                if (response.success) {
                    val tokens = response.obj
                    if (tokens != null) {
                        tokenManager.saveAccessToken(tokens.accessToken)
                        authorized()
                    }
                } else {
                    message(response.message)
                    unauthorized()
                }
            } catch (e: Exception) {
                message(message = e.message)
                unauthorized()
            }
        }
    }

    private fun loadUserData(accessToken: String, isSignIn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userApi.getUserData()
                if (response.success) {
                    val userData = response.obj
                    if (userData != null) {
                        updateViewState {
                            it.copy(
                                user = userData, isLoadUserData = true
                            )
                        }
                        if (userData.isBanned) updateViewState { it.copy(isUserBlocked = true) }
                        else {
                            updateUserStatus(statusModel = StatusModel.ONLINE)
                            loadFactoryData(
                                accessToken = accessToken, idFactory = userData.idFactory
                            )
                            if (isSignIn) {
                                loggerApi.addLog(
                                    log = LogRequest(
                                        idFactory = userData.idFactory,
                                        log = "Успешная авторизация пользователя: ${userData.name} - ${userData.id} - ${userData.role}",
                                        date = System.currentTimeMillis()
                                    )
                                )
                            }
                            updateViewState { it.copy(isUserBlocked = false) }
                        }
                    }
                } else {
                    message(response.message)
                }
            } catch (e: Exception) {
                message(e.message)
                unauthorized()
            }
        }
    }

    private fun loadFactoryData(accessToken: String, idFactory: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response =
                    factoryApi.getById(idFactory = idFactory)
                if (response.success) {
                    val factory = response.obj
                    if (factory != null) {
                        updateViewState {
                            it.copy(
                                factory = factory, isLoadSettingsData = true
                            )
                        }
                    }
                } else {
                    message(response.message)
                }
            } catch (e: Exception) {
                message(message = e.message)
                loadFactoryData(accessToken, idFactory)
            }
        }
    }

    fun saveNavControl(navController: NavController) {
        updateViewState { it.copy(navController = navController) }
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