package com.malliina.boattracker.ui.tracks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malliina.boattracker.*
import com.malliina.boattracker.ui.map.MapActivity
import kotlinx.android.synthetic.main.track_item.view.*
import timber.log.Timber

interface TrackDelegate {
    fun onTrack(selected: TrackRef)
}

class TracksActivity: AppCompatActivity(), TrackDelegate {
    private lateinit var viewAdapter: TracksAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var viewModel: TracksViewModel

    private lateinit var trackLang: TrackLang

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.tracks_toolbar))
        setContentView(R.layout.tracks_activity)

        trackLang = intent.getParcelableExtra(TrackLang.key)
        val token: IdToken = intent.getParcelableExtra(IdToken.key)

        viewManager = LinearLayoutManager(this)
        viewAdapter = TracksAdapter(emptyList(), this, trackLang)
        findViewById<RecyclerView>(R.id.tracks_view).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        Timber.i("Loading tracks...")
        viewModel = ViewModelProviders.of(this, TracksViewModelFactory(application, token)).get(TracksViewModel::class.java)
        viewModel.getTracks().observe(this, Observer<List<TrackRef>> { tracks ->
            viewAdapter.tracks = tracks ?: emptyList()
            viewAdapter.notifyDataSetChanged()
        })
    }

    // https://stackoverflow.com/a/1124988
    override fun onTrack(selected: TrackRef) {
        val intent = Intent(this, MapActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(TrackName.key, selected.trackName)
        startActivity(intent)
    }
}

class TracksAdapter(var tracks: List<TrackRef>, private val delegate: TrackDelegate, val lang: TrackLang): RecyclerView.Adapter<TracksAdapter.TrackHolder>() {
    class TrackHolder(val layout: ConstraintLayout): RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false) as ConstraintLayout
        return TrackHolder(layout)
    }

    override fun onBindViewHolder(th: TrackHolder, position: Int) {
        val ctx = th.itemView.context
        val track = tracks[position]
        val layout = th.layout
        layout.setOnClickListener {
            delegate.onTrack(track)
        }
        layout.date_text.text = track.times.start.date
        layout.title_text.text = track.trackTitle?.name ?: ""
        layout.first.fill(lang.distance, track.distanceMeters.formatted())
        layout.second.fill(lang.duration, track.duration.formatted())
        layout.third.fill(lang.topSpeed, track.topSpeed?.formatted() ?: ctx.getString(R.string.na))
    }

    override fun getItemCount(): Int = tracks.size
}
