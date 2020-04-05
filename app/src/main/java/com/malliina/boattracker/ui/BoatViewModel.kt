package com.malliina.boattracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.malliina.boattracker.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

open class BoatViewModel(val app: Application): AndroidViewModel(app) {
    val settings: UserSettings get() = UserSettings.instance
    private val viewModelJob = Job()
    protected val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    protected val ioScope = CoroutineScope(Dispatchers.IO)
}
