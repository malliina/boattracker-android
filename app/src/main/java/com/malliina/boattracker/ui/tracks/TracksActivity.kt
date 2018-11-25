package com.malliina.boattracker.ui.tracks

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.R
import com.malliina.boattracker.TrackRef
import kotlinx.android.synthetic.main.labeled_stat.view.*
import kotlinx.android.synthetic.main.track_item.view.*

class TracksActivity: AppCompatActivity() {
    companion object {
        const val tokenExtra = "com.malliina.boattracker.token"
    }

    private lateinit var viewAdapter: TracksAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var viewModel: TracksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracks_activity)

        viewManager = LinearLayoutManager(this)
        viewAdapter = TracksAdapter(emptyList())
        findViewById<RecyclerView>(R.id.tracks_view).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        Log.i(localClassName, "Loading tracks...")
        val token = IdToken(intent.getStringExtra(tokenExtra))
        viewModel = ViewModelProviders.of(this).get(TracksViewModel::class.java)
        viewModel.getTracks(token).observe(this, Observer<List<TrackRef>> { tracks ->
            Log.i(localClassName, "$tracks")
            viewAdapter.tracks = tracks ?: emptyList()
            viewAdapter.notifyDataSetChanged()
        })
    }
}

class TracksAdapter(var tracks: List<TrackRef>): RecyclerView.Adapter<TracksAdapter.TrackHolder>() {
    class TrackHolder(val layout: ConstraintLayout): RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false) as ConstraintLayout
        return TrackHolder(textView)
    }

    override fun onBindViewHolder(th: TrackHolder, position: Int) {
        val track = tracks[position]
        val layout = th.layout
        layout.date_text.text = track.formatStart()
        layout.first.stat_label.text = "Distance"
        layout.first.stat_value.text = track.formatDistance()
        layout.second.stat_label.text = "Duration"
        layout.second.stat_value.text = track.formatDuration()
        layout.third.stat_label.text = "Top"
        layout.third.stat_value.text = track.formatTopSpeed()
    }

    override fun getItemCount(): Int = tracks.size
}
