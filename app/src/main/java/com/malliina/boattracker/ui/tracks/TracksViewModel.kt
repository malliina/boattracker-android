package com.malliina.boattracker.ui.tracks

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.malliina.boattracker.FullUrl
import com.malliina.boattracker.HttpClient
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TracksViewModel(val app: Application): AndroidViewModel(app) {
    private val tag = "TracksViewModel"
    private val baseUrl = FullUrl.https("www.boat-tracker.com", "")
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
        val http = HttpClient.getInstance(app)
        http.token = token
        uiScope.launch {
            try {
                val response = http.getData(baseUrl.append("/tracks"))
                tracks.value = TrackRef.parseList(response)
            } catch(e: Exception) {
                Log.e(tag, "Failed to load tracks. Token was $token")
            }
        }
    }
}
