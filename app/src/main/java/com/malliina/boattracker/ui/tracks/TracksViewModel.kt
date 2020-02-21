package com.malliina.boattracker.ui.tracks

import android.app.Application
import androidx.lifecycle.*
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.SingleError
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.UserSettings
import com.malliina.boattracker.backend.BoatClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

enum class Status {
    Success,
    Error,
    Loading
}

data class Outcome<out T>(val status: Status, val data: T?, val error: SingleError?) {
    companion object {
        fun <T> success(t: T): Outcome<T> = Outcome(Status.Success, t, null)
        fun error(err: SingleError): Outcome<Nothing> = Outcome(Status.Error, null, err)
        fun loading(): Outcome<Nothing> = Outcome(Status.Loading, null, null)
    }
}

class TracksViewModelFactory(val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TracksViewModel(app) as T
    }
}

class TracksViewModel(val app: Application) : AndroidViewModel(app) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val tracksData: MutableLiveData<Outcome<List<TrackRef>>> by lazy {
        MutableLiveData<Outcome<List<TrackRef>>>().also {
            UserSettings.instance.token?.let { loadTracks(it) }
        }
    }
    val tracks: LiveData<Outcome<List<TrackRef>>> = tracksData

    private fun loadTracks(token: IdToken) {
        val http = BoatClient.build(app, token)
        uiScope.launch {
            tracksData.value = Outcome.loading()
            val outcome = try {
                Outcome.success(http.tracks())
            } catch (e: Exception) {
                Timber.e(e, "Failed to load tracks. Token was $token")
                Outcome.error(SingleError("backend", "Failed to load tracks."))
            }
            tracksData.value = outcome
        }
    }
}
