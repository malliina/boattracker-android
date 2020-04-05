package com.malliina.boattracker.ui.tracks

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.SingleError
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.backend.BoatClient
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
        val http = BoatClient.build(app, token)
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
}
