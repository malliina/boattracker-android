package com.malliina.boattracker.ui.statistics

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.SingleError
import com.malliina.boattracker.StatsResponse
import com.malliina.boattracker.ui.BoatViewModel
import com.malliina.boattracker.ui.Outcome
import kotlinx.coroutines.launch
import timber.log.Timber

class StatisticsViewModel(app: Application) : BoatViewModel(app) {
    private val statsData: MutableLiveData<Outcome<StatsResponse>> by lazy {
        MutableLiveData<Outcome<StatsResponse>>().also {
            userState.token?.let { loadStats(it) }
        }
    }
    val stats: LiveData<Outcome<StatsResponse>> = statsData

    private fun loadStats(token: IdToken) {
        ioScope.launch {
            statsData.postValue(Outcome.loading())
            val outcome = try {
                Outcome.success(http.stats())
            } catch (e: Exception) {
                Timber.e(e, "Failed to load stats. Token was $token")
                Outcome.error(SingleError.backend("Failed to load stats."))
            }
            statsData.postValue(outcome)
        }
    }
}
