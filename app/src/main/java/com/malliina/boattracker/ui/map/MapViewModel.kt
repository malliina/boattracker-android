package com.malliina.boattracker.ui.map

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Handler
import android.os.Looper
import com.malliina.boattracker.CoordsData
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.UserInfo
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.backend.BoatSocket
import com.malliina.boattracker.backend.SocketDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class MapViewModel(val app: Application): AndroidViewModel(app), SocketDelegate {
    companion object {
        private const val CoordsCode = 0
    }

    private lateinit var userInfo: MutableLiveData<UserInfo?>
    private lateinit var coords: MutableLiveData<CoordsData>

    private val google = Google.instance
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var socket: BoatSocket? = null
    private val socketHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            CoordsCode -> coords.value = msg.obj as CoordsData
            else -> Unit
        }
        true
    }

    init {
        Timber.tag(javaClass.simpleName)
    }

    fun getUser(): LiveData<UserInfo?> {
        if (!::userInfo.isInitialized) {
            userInfo = MutableLiveData()
            // Might receive coords from the socket before we observe?
            coords = MutableLiveData()
        }
        return userInfo
    }

    fun coords(): LiveData<CoordsData> {
        if (!::userInfo.isInitialized) {
            coords = MutableLiveData()
        }
        return coords
    }

    override fun onCoords(coords: CoordsData) {
        if (coords.coords.isEmpty()) return
        val msg = socketHandler.obtainMessage(CoordsCode, coords)
        socketHandler.sendMessage(msg)
    }

    fun openSocket(token: IdToken?) {
        socket?.disconnect()
        socket = BoatSocket(token, this)
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
    }

    fun signInSilently(activity: Activity) {
        uiScope.launch {
            try {
                val user = google.signInSilently(activity)
                userInfo.value = user
                Timber.i("Hello, '${user.email}'!")
            } catch(e: Exception) {
                Timber.w(e, "No authenticated user.")
                userInfo.value = null
            }
        }
    }
}
