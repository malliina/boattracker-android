package com.malliina.boattracker.ui.profile

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Handler
import android.os.Looper
import com.malliina.boattracker.CoordsData
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.backend.BoatSocket
import com.malliina.boattracker.backend.SocketDelegate
import timber.log.Timber

class ProfileViewModel(val app: Application): AndroidViewModel(app), SocketDelegate {
    companion object {
        private const val CoordsCode = 1
    }

    init {
        Timber.tag(javaClass.simpleName)
    }

    private lateinit var current: MutableLiveData<TrackRef>

    private var socket: BoatSocket? = null
    private val socketHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            CoordsCode -> current.value = (msg.obj as CoordsData).from
            else -> Unit
        }
        true
    }

    fun getCurrent(): LiveData<TrackRef> {
        if (!::current.isInitialized) {
            current = MutableLiveData()
        }
        return current
    }

    fun openSocket(token: IdToken?) {
        socket?.disconnect()
        socket = BoatSocket.token(token, null, this)
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
    }

    override fun onCoords(coords: CoordsData) {
        val msg = socketHandler.obtainMessage(CoordsCode, coords)
        socketHandler.sendMessage(msg)
    }
}
