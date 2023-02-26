package com.malliina.boattracker.ui.tracks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.malliina.boattracker.Distance
import com.malliina.boattracker.Duration
import com.malliina.boattracker.Lang
import com.malliina.boattracker.R
import com.malliina.boattracker.Speed
import com.malliina.boattracker.Stats
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.TrackTitle
import com.malliina.boattracker.ui.FontSize
import com.malliina.boattracker.ui.Margins
import com.malliina.boattracker.ui.Outcome
import com.malliina.boattracker.ui.Status
import com.malliina.boattracker.ui.theme.BoatDarkGray
import com.malliina.boattracker.ui.theme.BoatError

@Composable
fun TracksView(vm: TracksViewModel, lang: Lang, onClick: (TrackRef) -> Unit) {
    val outcome by vm.tracks.observeAsState()
    when (outcome?.status ?: Outcome.loading()) {
        Status.Success -> {
            outcome?.data?.let { data ->
                if (data.isEmpty()) {
                    Text(lang.settings.noTracksHelp, Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                } else {
                    LazyColumn(Modifier.fillMaxWidth()) {
                        itemsIndexed(data) { idx, item ->
                            TrackItemView(
                                TrackItemContent.ref(item),
                                TrackItemLang.lang(lang),
                                bottomDivider = idx != data.lastIndex,
                                moreIcon = true,
                                onNewTitle = { title -> vm.changeTitle(item.trackName, title) },
                                onClick = { onClick(item) }
                            )
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

@Composable
fun ErrorText(text: String) {
    Text(text, Modifier.fillMaxWidth().padding(Margins.large), textAlign = TextAlign.Center, color = BoatError)
}

@Composable
fun LoadingRow() {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator()
    }
}

data class TrackItemLang(
    val distance: String,
    val duration: String,
    val topSpeed: String,
    val boatDays: String,
    val notAvailable: String,
    val edit: String,
    val rename: String,
    val done: String,
    val cancel: String
) {
    companion object {
        fun lang(l: Lang) = TrackItemLang(l.track.distance, l.track.duration, l.track.topSpeed, l.track.days, l.messages.notAvailable, l.settings.edit, l.settings.rename, l.settings.done, l.settings.cancel)
    }
}

data class TrackItemContent(
    val startDate: String,
    val title: TrackTitle?,
    val distanceMeters: Distance,
    val duration: Duration,
    val topSpeed: Speed?,
    val boatDays: Long?
) {
    companion object {
        fun ref(t: TrackRef) = TrackItemContent(t.times.start.date, t.trackTitle, t.distanceMeters, t.duration, t.topSpeed, null)
        fun stat(s: Stats) = TrackItemContent(s.label, null, s.distance, s.duration, null, s.days)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackItemView(track: TrackItemContent, lang: TrackItemLang, moreIcon: Boolean, bottomDivider: Boolean = true, onNewTitle: (TrackTitle) -> Unit, onClick: (() -> Unit)?) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(Margins.normal)
            .clickable(enabled = onClick != null) {
                onClick?.let { it() }
            }
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Margins.normal), verticalAlignment = Alignment.CenterVertically) {
            Text(track.startDate, fontSize = FontSize.large)
            track.title?.name?.let { title ->
                Text(title, fontSize = FontSize.large, color = BoatDarkGray)
            }
        }
        var moreExpanded by remember { mutableStateOf(false) }
        var dialogExpanded by remember { mutableStateOf(false) }
        var trackName by remember { mutableStateOf(track.title?.name ?: "") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            StatBoxView(lang.distance, track.distanceMeters.formatKilometers())
            StatBoxView(lang.duration, track.duration.formatted())
            val thirdLabel =
                if (track.boatDays != null) lang.boatDays
                else lang.topSpeed
            val thirdValue =
                if (track.boatDays != null) "${track.boatDays}"
                else track.topSpeed?.formatted()
            StatBoxView(thirdLabel, thirdValue ?: lang.notAvailable)
            if (moreIcon) {
                Box {
                    IconButton(onClick = {
                        moreExpanded = true
                    }) {
                        Icon(painterResource(R.drawable.ic_more_vert_gray_24dp), stringResource(R.string.more))
                        DropdownMenu(
                            expanded = moreExpanded,
                            onDismissRequest = { moreExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(lang.edit) },
                                onClick = {
                                    moreExpanded = false
                                    dialogExpanded = true
                                }
                            )
                        }
                        if (dialogExpanded) {
                            AlertDialog(
                                onDismissRequest = {
                                    trackName = track.title?.name ?: ""
                                    dialogExpanded = false
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        onNewTitle(TrackTitle(trackName))
                                        dialogExpanded = false
                                    }) {
                                        Text(lang.done)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        trackName = track.title?.name ?: ""
                                        dialogExpanded = false
                                    }) {
                                        Text(lang.cancel)
                                    }
                                },
                                title = { Text(lang.rename) },
                                text = {
                                    TextField(value = trackName, onValueChange = { trackName = it }, label = { Text(lang.edit) })
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    if (bottomDivider) {
        Divider()
    }
}

@Composable
fun StatBoxView(label: String, value: String) {
    Column {
        Text(label, Modifier.padding(horizontal = Margins.xs, vertical = Margins.s), color = BoatDarkGray, fontSize = FontSize.xs)
        Text(value, Modifier.padding(horizontal = Margins.xs))
    }
}

@Preview
@Composable
fun TrackItemPreview() {
    val content = TrackItemContent("Now", TrackTitle("Yeah"), Distance(1200.0), Duration(123.0), Speed(42.0), null)
    val lang = TrackItemLang("Distance", "Duration", "Top speed", "Boat days", "Not available", "Edit", "Rename", "Done", "Cancel")
    TrackItemView(content, lang, true, onNewTitle = {}, onClick = null)
}
