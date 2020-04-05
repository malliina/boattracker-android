package com.malliina.boattracker.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.malliina.boattracker.CoordsData
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackName
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.backend.BoatSocket
import com.malliina.boattracker.backend.SocketDelegate
import com.malliina.boattracker.ui.BoatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileViewModel(app: Application): BoatViewModel(app), SocketDelegate {
    private val loadState = MutableLiveData<LoadState>().apply {
        value = LoadState.NotLoaded
    }
    private val current = MutableLiveData<TrackRef>()
    private var socket: BoatSocket? = null

    val state: LiveData<LoadState> = loadState
    val currentTrack: LiveData<TrackRef> = current

    fun openSocket(token: IdToken?, trackName: TrackName?) {
        socket?.disconnect()
        loadState.value = LoadState.Loading
        val newSocket = BoatSocket.token(token, trackName, this, app.applicationContext)
        socket = newSocket
        uiScope.launch {
            try {
                newSocket.connectWithRetry()
                loadState.postValue(LoadState.NotLoaded)
            } catch (e: Exception) {
                Timber.e(e, "Failed to connect to socket.")
                loadState.postValue(LoadState.NotLoaded)
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
    }

    override fun onCoords(newCoords: CoordsData) {
        // Not on main thread here, therefore using postValue instead of setValue
        current.postValue(newCoords.from)
        loadState.postValue(LoadState.Loaded)
    }
}
