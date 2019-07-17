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

class ProfileViewModel(val app: Application): AndroidViewModel(app), SocketDelegate {
    private val loadState: MutableLiveData<LoadState> by lazy {
        MutableLiveData<LoadState>().also {
            it.value = LoadState.NotLoaded
        }
    }

    private val current: MutableLiveData<TrackRef> by lazy {
        MutableLiveData<TrackRef>()
    }

    private var socket: BoatSocket? = null

    fun getState(): LiveData<LoadState> {
        return loadState
    }

    fun getCurrent(): LiveData<TrackRef> {
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
