package com.malliina.boattracker.ui.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malliina.boattracker.Lang
import com.malliina.boattracker.R
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.ui.ResourceFragment
import kotlinx.android.synthetic.main.track_item.view.*
import kotlinx.android.synthetic.main.tracks_activity.view.*

interface TrackDelegate {
    fun onTrack(selected: TrackRef)
}

class TracksFragment : ResourceFragment(R.layout.tracks_activity), TrackDelegate {
    private lateinit var viewAdapter: TracksAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewModel: TracksViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            TracksViewModelFactory(requireActivity().application)
        ).get(TracksViewModel::class.java)
        viewManager = LinearLayoutManager(context)
        viewAdapter = TracksAdapter(emptyList(), this, lang)
        view.tracks_view.apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        viewModel.getTracks().observe(viewLifecycleOwner){ tracks ->
            viewAdapter.tracks = tracks
            viewAdapter.notifyDataSetChanged()
        }
        view.tracks_toolbar.title = lang.track.tracks
    }

    // https://stackoverflow.com/a/1124988
    override fun onTrack(selected: TrackRef) {
        val action = TracksFragmentDirections.tracksToMap(refresh = false, track = selected.trackName, fit = true)
        findNavController().navigate(action)
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
    }

    override fun getItemCount(): Int = tracks.size
}
