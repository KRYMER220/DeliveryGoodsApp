package ru.krymer.delivery.ui.request

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.components.CommonInfoAlertDialog
import ru.krymer.delivery.ui.components.CommonInfoBottomSheet
import ru.krymer.delivery.ui.components.CommonSaveDialog
import ru.krymer.delivery.ui.components.ConfirmView
import ru.krymer.delivery.ui.request.models.RequestEvent
import ru.krymer.delivery.ui.request.models.RequestViewState
import ru.krymer.delivery.ui.request.view.AddSumEditView
import ru.krymer.delivery.ui.request.view.ArrearsEditView
import ru.krymer.delivery.ui.request.view.MessageEditView
import ru.krymer.delivery.ui.request.view.RequestView

@Composable
fun RequestScreen(shop: ShopModel, user: UserModel?, backStack: () -> Unit) {
    user?.let {
        val viewModel = hiltViewModel<RequestViewModel>()
        LaunchedEffect(Unit) {
            viewModel.initData(shop = shop)
        }
        RequestViews(
            state = viewModel.viewState.collectAsState().value,
            event = viewModel::obtainEvent,
            user = it,
            backStack = backStack
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RequestViews(
    state: RequestViewState, event: (RequestEvent) -> Unit, user: UserModel, backStack: () -> Unit
) {
    RequestView(state = state, event = event, user = user)

    if (state.toggleDeleteShop) {
        state.shop?.let {
            CommonDeleteDialog(
                itemName = it.nameShop,
                isVisible = true,
                onDismiss = { event(RequestEvent.ToggleDeleteShop) },
                onConfirm = {
                    event(RequestEvent.DeleteShop)
                    backStack()
                })
        }
    }

    if (state.toggleConfirmSaveShopDialog) {
        CommonInfoAlertDialog(
            onDismissRequest = { event(RequestEvent.ToggleConfirmDeleteShopDialog) },
            content = {
                ConfirmView(
                    onSubmit = { event(RequestEvent.UpdateShop) },
                    onDismiss = { event(RequestEvent.ToggleConfirmDeleteShopDialog) })
            })
    }

    if (state.toggleAddSumDialog) {
        CommonInfoBottomSheet(
            onDismissRequest = { event(RequestEvent.ToggleAddSumDialog) },
            content = {
                AddSumEditView(changeAddSum = {
                    event(RequestEvent.ChangeAddSum(it))
                }, saveAddSum = {
                    event(RequestEvent.ToggleAddSumDialog)
                })
            })
    }

    if (state.toggleArrearsDialog) {
        CommonInfoBottomSheet(
            onDismissRequest = { event(RequestEvent.ToggleArrearsDialog) },
            content = {
                ArrearsEditView(changeArrears = {
                    event(RequestEvent.ChangeArrears(it))
                }, saveArrear = { event(RequestEvent.ToggleArrearsDialog) })
            })
    }

    if (state.toggleMessageDialog) {
        CommonSaveDialog(dismiss = {
            event(RequestEvent.ToggleMessageDialog)
        }, confirm = {
            event(RequestEvent.SendMessage)
        }, content = {
            val messages = state.messages
            MessageEditView(
                changeTextMessage = {
                    event(RequestEvent.ChangeMessage(it))
                },
                deleteMessage = {
                    event(RequestEvent.DeleteMessage(it))
                },
                messages = messages,
            )
        })
    }
}