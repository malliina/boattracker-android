package com.malliina.boattracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.malliina.boattracker.BoatApp
import com.malliina.boattracker.UserSettings
import com.malliina.boattracker.UserState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

open class BoatViewModel(private val appl: Application) : AndroidViewModel(appl) {
    val app: BoatApp get() = appl as BoatApp
    val settings: UserSettings get() = app.settings
    val userState: UserState get() = UserState.instance
    private val viewModelJob = Job()
    protected val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    protected val ioScope = CoroutineScope(Dispatchers.IO)
}
