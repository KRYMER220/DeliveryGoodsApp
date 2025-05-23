package ru.krymer.delivery


import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import ru.krymer.delivery.data.model.user.StatusModel
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import javax.inject.Inject

@HiltAndroidApp
class AppBox : Application(), LifecycleObserver