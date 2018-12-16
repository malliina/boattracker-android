package com.malliina.boattracker.ui.map

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.malliina.boattracker.CoordsData
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackName
import com.malliina.boattracker.UserInfo
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.backend.BoatSocket
import com.malliina.boattracker.backend.SocketDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

data class MapState(val user: UserInfo?, val track: TrackName?)

class MapViewModel(val app: Application): AndroidViewModel(app), SocketDelegate {
    private lateinit var mapState: MutableLiveData<MapState>
    private lateinit var coords: MutableLiveData<CoordsData>

    private val google = Google.instance
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var socket: BoatSocket? = null

    init {
        Timber.tag(javaClass.simpleName)
    }

    fun getUser(): LiveData<MapState> {
        if (!::mapState.isInitialized) {
            mapState = MutableLiveData()
            // Might receive coords from the socket before we observe?
            coords = MutableLiveData()
            // https://developers.google.com/identity/sign-in/android/backend-auth
            signInSilently(app.applicationContext)
        }
        return mapState
    }

    fun getCoords(): LiveData<CoordsData> {
        if (!::coords.isInitialized) {
            coords = MutableLiveData()
        }
        return coords
    }

    fun update(state: MapState) {
        mapState.value = state
    }

    override fun onCoords(newCoords: CoordsData) {
        if (newCoords.coords.isEmpty()) return
        coords.postValue(newCoords)
    }

    fun openSocket(token: IdToken?, trackName: TrackName?) {
        socket?.disconnect()
        socket = BoatSocket.token(token, trackName, this)
        socket?.connect()
    }

    fun disconnect() {
        Timber.i("Disconnecting socket...")
        socket?.disconnect()
    }

    private fun signInSilently(ctx: Context) {
        uiScope.launch {
            try {
                val user = google.signInSilently(ctx)
                mapState.value = MapState(user, null)
                Timber.i("Hello, '${user.email}'!")
            } catch(e: Exception) {
                Timber.w(e, "No authenticated user.")
                mapState.value = MapState(null, null)
            }
        }
    }
}
