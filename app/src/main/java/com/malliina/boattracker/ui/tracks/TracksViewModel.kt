package com.malliina.boattracker.ui.tracks

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.malliina.boattracker.FullUrl
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.backend.Env
import com.malliina.boattracker.backend.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class TracksViewModel(val app: Application): AndroidViewModel(app) {
    private val baseUrl = FullUrl.https(Env.BackendDomain, "")
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private lateinit var tracks: MutableLiveData<List<TrackRef>>

    init {
        Timber.tag("TracksViewModel")
    }

    fun getTracks(token: IdToken): LiveData<List<TrackRef>> {
        if (!::tracks.isInitialized) {
            tracks = MutableLiveData()
            loadTracks(token)
        }
        return tracks
    }

    private fun loadTracks(token: IdToken) {
        val http = HttpClient.getInstance(app)
        http.token = token
        uiScope.launch {
            try {
                val response = http.getData(baseUrl.append("/tracks"))
                tracks.value = TrackRef.parseList(response)
            } catch(e: Exception) {
                Timber.e(e, "Failed to load tracks. Token was $token")
            }
        }
    }
}
