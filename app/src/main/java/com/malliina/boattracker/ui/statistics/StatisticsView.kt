package com.malliina.boattracker.ui.statistics

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.malliina.boattracker.Distance
import com.malliina.boattracker.Duration
import com.malliina.boattracker.Lang
import com.malliina.boattracker.ui.Outcome
import com.malliina.boattracker.ui.Status
import com.malliina.boattracker.ui.tracks.ErrorText
import com.malliina.boattracker.ui.tracks.LoadingRow
import com.malliina.boattracker.ui.tracks.TrackItemContent
import com.malliina.boattracker.ui.tracks.TrackItemLang
import com.malliina.boattracker.ui.tracks.TrackItemView

data class StatContent(val distance: Distance, val duration: Duration, val days: Int)

@Composable
fun StatisticsView(vm: StatisticsViewModel, lang: Lang) {
    val outcome by vm.stats.observeAsState()

    when (outcome?.status ?: Outcome.loading()) {
        Status.Success -> {
            outcome?.data?.let { res ->
                if (res.yearly.isNotEmpty()) {
                    val list = res.yearly.flatMap { y -> listOf(y) + y.monthly }
                    LazyColumn {
                        itemsIndexed(list) { idx, stat ->
                            TrackItemView(TrackItemContent.stat(stat), TrackItemLang.lang(lang), bottomDivider = idx != list.lastIndex, moreIcon = false, onNewTitle = {}, onClick = null)
                        }
                    }
                }
            }
        }
        Status.Error -> {
            outcome?.error?.let { err ->
                ErrorText(err.message)
            }
        }
        Status.Loading -> {
            LoadingRow()
        }
    }
}
