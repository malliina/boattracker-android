package com.malliina.boattracker.ui.map

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.malliina.boattracker.UserInfo
import com.malliina.boattracker.auth.Google
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class MapViewModel(val app: Application): AndroidViewModel(app) {
    companion object {
        private const val TAG = "MapViewModel"
    }
    private lateinit var userInfo: MutableLiveData<UserInfo?>
    private val google = Google.instance
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun getUser(activity: Activity): LiveData<UserInfo?> {
        if (!::userInfo.isInitialized) {
            userInfo = MutableLiveData()
            refreshUserInfo(activity)
        }
        return userInfo
    }

    fun refreshUserInfo(activity: Activity) {
        uiScope.launch {
            try {
                val user = google.refreshUserInfo(activity)
                userInfo.value = user
                Log.i(TAG, "Hello, ${user.email}!")
            } catch(e: Exception) {
                Log.w(TAG, "No authenticated user.")
                userInfo.value = null
            }
        }
    }
}
