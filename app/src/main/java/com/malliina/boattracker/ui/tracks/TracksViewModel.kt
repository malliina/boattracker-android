package com.malliina.boattracker.ui.tracks

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.malliina.boattracker.*
import com.malliina.boattracker.ui.BoatViewModel
import com.malliina.boattracker.ui.Outcome
import kotlinx.coroutines.launch
import timber.log.Timber

class TracksViewModel(app: Application) : BoatViewModel(app) {
    private val tracksData: MutableLiveData<Outcome<List<TrackRef>>> by lazy {
        MutableLiveData<Outcome<List<TrackRef>>>().also {
            userState.token?.let { loadTracks(it) }
        }
    }
    val tracks: LiveData<Outcome<List<TrackRef>>> = tracksData

    private fun loadTracks(token: IdToken) {
        uiScope.launch {
            tracksData.postValue(Outcome.loading())
            val outcome = try {
                Outcome.success(http.tracks())
            } catch (e: Exception) {
                Timber.e(e, "Failed to load tracks. Token was $token")
                Outcome.error(SingleError.backend("Failed to load tracks."))
            }
            tracksData.postValue(outcome)
        }
    }

    fun changeTitle(track: TrackName, to: TrackTitle) {
        ioScope.launch {
            try {
                val updated = http.changeTitle(to, track)
                uiScope.launch {
                    val existing = tracksData.value?.data ?: emptyList()
                    val idx = existing.indexOfFirst { t -> t.trackName == track }
                    if (idx >= 0) {
                        val newList =
                            existing.mapIndexed { index, trackRef -> if (index == idx) updated else trackRef }
                        tracksData.value = Outcome.success(newList)
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to change title of '$track' to '$to'.")
            }
        }
    }
}
