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
import timber.log.Timber

class ProfileViewModel(val app: Application): AndroidViewModel(app), SocketDelegate {
    init {
        Timber.tag(javaClass.simpleName)
    }

    private lateinit var loadState: MutableLiveData<LoadState>
    private lateinit var current: MutableLiveData<TrackRef>

    private var socket: BoatSocket? = null

    fun getState(): LiveData<LoadState> {
        if(!::loadState.isInitialized) {
            loadState = MutableLiveData()
            loadState.value = LoadState.NotLoaded
        }
        return loadState
    }

    fun getCurrent(): LiveData<TrackRef> {
        if (!::current.isInitialized) {
            current = MutableLiveData()
        }
        return current
    }

    fun openSocket(token: IdToken?, trackName: TrackName?) {
        socket?.disconnect()
        loadState.value = LoadState.Loading
        socket = BoatSocket.token(token, trackName, this)
        socket?.connect()
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
