package com.malliina.boattracker.ui.map

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.malliina.boattracker.*
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.backend.BoatSocket
import com.malliina.boattracker.backend.Env
import com.malliina.boattracker.backend.HttpClient
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
    private lateinit var conf: MutableLiveData<ClientConf>

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

    fun getConf(): LiveData<ClientConf> {
        if (!::conf.isInitialized) {
            conf = MutableLiveData()
            loadConf()
        }
        return conf
    }

    private fun loadConf() {
        val http = HttpClient.getInstance(app)
        uiScope.launch {
            try {
                val response = http.getData(Env.baseUrl.append("/conf"))
                conf.value = ClientConf.parse(response)
            } catch(e: Exception) {
                Timber.e(e, "Failed to load conf.")
            }
        }
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

    fun signInSilently(ctx: Context) {
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
