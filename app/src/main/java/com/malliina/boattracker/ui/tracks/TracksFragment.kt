package com.malliina.boattracker.ui.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.malliina.boattracker.Lang
import com.malliina.boattracker.R
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.TrackTitle
import com.malliina.boattracker.ui.ComposeFragment
import com.malliina.boattracker.ui.Controls
import com.malliina.boattracker.ui.ResourceFragment
import com.malliina.boattracker.ui.Status
import kotlinx.android.synthetic.main.track_item.view.*
import kotlinx.android.synthetic.main.tracks_fragment.view.*

interface TrackDelegate {
    fun onTrack(selected: TrackRef)
    fun onMore(track: TrackRef, button: ImageButton)
}

class ComposeTracks : ComposeFragment() {
    private val viewModel: TracksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TracksView(viewModel, lang) { item ->
                val action = ComposeTracksDirections.tracksToMap(
                    refresh = false,
                    track = item.trackName,
                    fit = true
                )
                findNavController().navigate(action)
            }
        }
    }
}

class TracksFragment : ResourceFragment(R.layout.tracks_fragment), TrackDelegate {
    private lateinit var viewAdapter: TracksAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val viewModel: TracksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewManager = LinearLayoutManager(context)
        viewAdapter = TracksAdapter(emptyList(), this, lang)
        view.tracks_view.apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        val ctrl = Controls(view.tracks_loading, view.tracks_view, view.tracks_feedback_text)
        viewModel.tracks.observe(viewLifecycleOwner) { outcome ->
            when (outcome.status) {
                Status.Success -> {
                    ctrl.showList()
                    outcome.data?.let { list ->
                        if (list.isEmpty()) {
                            ctrl.display(lang.settings.noTracksHelp)
                        } else {
                            viewAdapter.tracks = list
                            viewAdapter.notifyDataSetChanged()
                        }
                    }
                }
                Status.Error -> {
//                    display(getString(R.string.error_generic), ctrl)
                }
                Status.Loading -> {
                    ctrl.enableLoading()
                }
            }
        }
    }

    // https://stackoverflow.com/a/1124988
    override fun onTrack(selected: TrackRef) {
        val action = ComposeTracksDirections.tracksToMap(
            refresh = false,
            track = selected.trackName,
            fit = true
        )
        findNavController().navigate(action)
    }

    override fun onMore(track: TrackRef, button: ImageButton) {
        val popup = PopupMenu(requireContext(), button)
        popup.inflate(R.menu.track_item_menu)
        val settingsLang = lang.settings
        popup.menu.getItem(0).title = settingsLang.edit
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_track -> {
                    val ctx = requireContext()
                    val editText = EditText(ctx)
                    MaterialAlertDialogBuilder(ctx)
                        .setView(editText)
                        .setTitle(settingsLang.rename)
                        .setPositiveButton(settingsLang.done) { d, i ->
                            val newTitle = TrackTitle(editText.text.toString())
                            viewModel.changeTitle(track.trackName, newTitle)
                        }.setNegativeButton(settingsLang.cancel) { d, i ->
                            d.cancel()
                        }.show()
                    true
                }
                else -> {
                    false
                }
            }
        }
        popup.show()
    }
}

class TracksAdapter(
    var tracks: List<TrackRef>,
    private val delegate: TrackDelegate,
    private val lang: Lang
) : RecyclerView.Adapter<TracksAdapter.TrackHolder>() {
    class TrackHolder(val layout: ConstraintLayout) : RecyclerView.ViewHolder(layout)

    private val trackLang = lang.track

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val layout = LayoutInflater.from(parent.context).inflate(
            R.layout.track_item,
            parent,
            false
        ) as ConstraintLayout
        return TrackHolder(layout)
    }

    override fun onBindViewHolder(th: TrackHolder, position: Int) {
        val track = tracks[position]
        val layout = th.layout
        layout.setOnClickListener {
            delegate.onTrack(track)
        }
        layout.date_text.text = track.times.start.date
        layout.title_text.text = track.trackTitle?.name ?: ""
        layout.first.fill(trackLang.distance, track.distanceMeters.formatKilometers())
        layout.second.fill(trackLang.duration, track.duration.formatted())
        layout.third.fill(
            trackLang.topSpeed,
            track.topSpeed?.formatted() ?: lang.messages.notAvailable
        )
        val moreButton = layout.track_more_button
        moreButton.setOnClickListener {
            delegate.onMore(track, moreButton)
        }
    }

    override fun getItemCount(): Int = tracks.size
}
