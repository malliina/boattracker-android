package com.malliina.boattracker.ui.tracks

import android.app.Application
import androidx.lifecycle.*
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.UserSettings
import com.malliina.boattracker.backend.BoatClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class TracksViewModelFactory(val app: Application): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TracksViewModel(app) as T
    }
}

class TracksViewModel(val app: Application): AndroidViewModel(app) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val tracks: MutableLiveData<List<TrackRef>> by lazy {
        MutableLiveData<List<TrackRef>>().also {
            UserSettings.instance.token?.let { loadTracks(it) }
        }
    }

    fun getTracks(): LiveData<List<TrackRef>> {
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
