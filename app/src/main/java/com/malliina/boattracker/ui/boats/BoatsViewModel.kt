package com.malliina.boattracker.ui.boats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessaging
import com.malliina.boattracker.BoatUser
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.backend.Env
import com.malliina.boattracker.backend.HttpClient
import com.malliina.boattracker.push.PushService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class BoatsViewModel(val app: Application): AndroidViewModel(app) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var boats: MutableLiveData<BoatUser>

    fun getBoats(token: IdToken): LiveData<BoatUser> {
        if(!::boats.isInitialized) {
            boats = MutableLiveData()
            loadBoats(token)
        }
        return boats
    }

    var notificationsEnabled: Boolean = FirebaseMessaging.getInstance().isAutoInitEnabled

    fun toggleNotifications(isOn: Boolean) {
        PushService.getInstance(app).toggleNotifications(isOn)
    }

    private fun loadBoats(token: IdToken) {
        val http = HttpClient.getInstance(app)
        http.token = token
        uiScope.launch {
            try {
                val response = http.getData(Env.baseUrl.append("/users/me"))
                boats.value = BoatUser.parse(response.getJSONObject("user"))
            } catch(e: Exception) {
                Timber.e(e, "Failed to load boats. Token was $token")
            }
        }
    }
}
