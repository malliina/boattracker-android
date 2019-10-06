package com.malliina.boattracker.ui.map

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.malliina.boattracker.*
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.backend.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

data class MapState(val user: UserInfo?, val track: TrackName?)

class MapViewModel(val app: Application): AndroidViewModel(app), SocketDelegate {
    private val mapState: MutableLiveData<MapState> by lazy {
        MutableLiveData<MapState>().also {
            // https://developers.google.com/identity/sign-in/android/backend-auth
            signInSilently(app.applicationContext)
        }
    }
    private val coords: MutableLiveData<CoordsData> by lazy {
        MutableLiveData<CoordsData>()
    }
    private val conf: MutableLiveData<ClientConf> by lazy {
        MutableLiveData<ClientConf>().also {
            loadConf()
        }
    }
    private val profile: MutableLiveData<BoatUser> by lazy {
        MutableLiveData<BoatUser>()
    }
    private val language: MutableLiveData<Language> by lazy {
        MutableLiveData<Language>()
    }

    private val google = Google.instance
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var socket: BoatSocket? = null

    fun getUser(): LiveData<MapState> {
        return mapState
    }

    fun getConf(): LiveData<ClientConf> {
        return conf
    }

    fun getCoords(): LiveData<CoordsData> {
        return coords
    }

    fun getProfile(): LiveData<BoatUser> {
        return profile
    }

    private fun loadProfile(token: IdToken) {
        val http = BoatClient.build(app, token)
        uiScope.launch {
            try {
                profile.value = http.me()
            } catch(e: Exception) {
                Timber.e(e, "Failed to load profile.")
            }
        }
    }

    private fun loadConf() {
        val http = BoatClient.basic(app)
        uiScope.launch {
            try {
                conf.value = http.conf()
            } catch(e: Exception) {
                Timber.e(e, "Failed to load conf.")
            }
        }
    }

    fun update(state: MapState) {
        mapState.postValue(state)
        state.user?.idToken?.let { token ->
            loadProfile(token)
        }
    }

    override fun onCoords(newCoords: CoordsData) {
        if (newCoords.coords.isEmpty()) return
        coords.postValue(newCoords)
    }

    fun openSocket(token: IdToken?, trackName: TrackName?) {
        socket?.disconnect()
        socket = BoatSocket.token(token, trackName, this, app.applicationContext)
        uiScope.launch {
            socket?.connectWithRetry()
        }
    }

    fun reconnect() {
        val state = mapState.value
        openSocket(state?.user?.idToken, state?.track)
    }

    fun disconnect() {
        Timber.i("Disconnecting socket...")
        socket?.disconnect()
    }

    fun signInSilently(ctx: Context) {
        uiScope.launch {
            try {
                val user = google.signInSilently(ctx)
                update(MapState(user, null))
                Timber.i("Hello, '${user.email}'!")
            } catch(e: Exception) {
                Timber.w(e, "No authenticated profile.")
                mapState.value = MapState(null, null)
            }
        }
    }
}
