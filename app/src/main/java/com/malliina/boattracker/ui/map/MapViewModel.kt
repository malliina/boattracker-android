package com.malliina.boattracker.ui.map

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.malliina.boattracker.*
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.backend.BoatClient
import com.malliina.boattracker.backend.BoatSocket
import com.malliina.boattracker.backend.SocketDelegate
import com.malliina.boattracker.ui.BoatViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

data class UserTrack(val user: UserInfo?, val track: TrackName?)

class MapViewModel(appl: Application) : BoatViewModel(appl), SocketDelegate {
    private val userTrack: MutableLiveData<UserTrack> by lazy {
        MutableLiveData<UserTrack>().also {
            // https://developers.google.com/identity/sign-in/android/backend-auth
            signInSilently(app.applicationContext)
        }
    }
    private val coordsData = MutableLiveData<CoordsData>()
    private val vesselsData = MutableLiveData<List<Vessel>>()
    private val confData: MutableLiveData<ClientConf> by lazy {
        MutableLiveData<ClientConf>().also {
            loadConf()
        }
    }
    private val google = Google.instance
    private var socket: BoatSocket? = null

    val user: LiveData<UserTrack> = userTrack
    val conf: LiveData<ClientConf> = confData
    val coords: LiveData<CoordsData> = coordsData
    val vessels: LiveData<List<Vessel>> = vesselsData

    private fun loadProfile(token: IdToken) {
        val http = BoatClient.build(app, token)
        ioScope.launch {
            try {
                val me = http.me()
                settings.profile = me
            } catch (e: Exception) {
                Timber.e(e, "Failed to load profile.")
            }
        }
    }

    private fun loadConf() {
        ioScope.launch {
            val cache = settings.conf
            val isCacheHit = cache != null
            if (isCacheHit) {
                confData.postValue(cache)
            }
            val http = BoatClient.basic(app)
            try {
                val data = http.conf()
                settings.conf = data
                if (!isCacheHit) {
                    confData.postValue(data)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load configuration.")
            }
        }
    }

    private fun update(state: UserTrack) {
        userState.userTrack = state
        userTrack.postValue(state)
        state.user?.idToken?.let { token ->
            loadProfile(token)
        }
    }

    override fun onCoords(newCoords: CoordsData) {
        if (newCoords.coords.isEmpty()) return
        coordsData.postValue(newCoords)
    }

    override fun onVessels(vessels: List<Vessel>) {
        if (vessels.isEmpty()) return
        vesselsData.postValue(vessels)
    }

    override fun onNewToken(user: UserInfo) {
        // Is it necessary to be on UI thread when doing LiveData.value?
        // If not, no need to launch on UI scope here.
        uiScope.launch {
            update(UserTrack(user, userTrack.value?.track))
        }
    }

    private fun openSocket(token: IdToken?, trackName: TrackName?) {
        socket?.disconnect()
        val newSocket = BoatSocket.token(token, trackName, this, app.applicationContext)
        socket = newSocket
        ioScope.launch {
            try {
                newSocket.connectWithRetry()
            } catch (e: Exception) {
                Timber.e(e, "Failed to connect to socket.")
            }
        }
    }

    fun restart() {
        reconnect(userTrack.value?.track)
    }

    fun reconnect(trackName: TrackName?) {
        val state = userTrack.value
        val activeState = UserTrack(state?.user, trackName ?: state?.track)
        userState.userTrack = activeState
//        userTrack.postValue(activeState)
        openSocket(state?.user?.idToken, activeState.track)
    }

    fun disconnect() {
        Timber.i("Disconnecting socket...")
        socket?.disconnect()
    }

    fun signInSilently(ctx: Context) {
        ioScope.launch {
            Timber.i("Signing in silently...")
            try {
                val user = google.signInSilently(ctx)
                update(UserTrack(user, null))
                Timber.i("Hello, '${user.email}'!")
            } catch (e: Exception) {
                Timber.w(e, "No authenticated profile.")
                userTrack.postValue(UserTrack(null, null))
            }
            Timber.i("Signing in complete...")
        }
    }
}
