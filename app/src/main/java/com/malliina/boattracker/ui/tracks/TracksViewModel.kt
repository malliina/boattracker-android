package com.malliina.boattracker.ui.tracks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.backend.BoatClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class TracksViewModel(val app: Application): AndroidViewModel(app) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private lateinit var tracks: MutableLiveData<List<TrackRef>>

    fun getTracks(token: IdToken): LiveData<List<TrackRef>> {
        if (!::tracks.isInitialized) {
            tracks = MutableLiveData()
            loadTracks(token)
        }
        return tracks
    }

    private fun loadTracks(token: IdToken) {
        val http = BoatClient.build(app, token)
        uiScope.launch {
            try {
                tracks.value = http.tracks()
            } catch(e: Exception) {
                Timber.e(e, "Failed to load tracks. Token was $token")
            }
        }
    }
}
