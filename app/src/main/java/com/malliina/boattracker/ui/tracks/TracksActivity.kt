package com.malliina.boattracker.ui.tracks

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.R
import com.malliina.boattracker.TrackRef
import kotlinx.android.synthetic.main.track_item.view.*
import timber.log.Timber

class TracksActivity: AppCompatActivity() {
    companion object {
        const val tokenExtra = "com.malliina.boattracker.token"
    }

    private lateinit var viewAdapter: TracksAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var viewModel: TracksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(localClassName)
        setContentView(R.layout.tracks_activity)

        viewManager = LinearLayoutManager(this)
        viewAdapter = TracksAdapter(emptyList())
        findViewById<RecyclerView>(R.id.tracks_view).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        Timber.i("Loading tracks...")
        val token = IdToken(intent.getStringExtra(tokenExtra))
        viewModel = ViewModelProviders.of(this).get(TracksViewModel::class.java)
        viewModel.getTracks(token).observe(this, Observer<List<TrackRef>> { tracks ->
            viewAdapter.tracks = tracks ?: emptyList()
            viewAdapter.notifyDataSetChanged()
        })
    }
}

class TracksAdapter(var tracks: List<TrackRef>): RecyclerView.Adapter<TracksAdapter.TrackHolder>() {
    class TrackHolder(val layout: ConstraintLayout): RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false) as ConstraintLayout
        return TrackHolder(layout)
    }

    override fun onBindViewHolder(th: TrackHolder, position: Int) {
        val ctx = th.itemView.context
        val track = tracks[position]
        val layout = th.layout
        layout.date_text.text = track.formatStart()
        layout.first.fill(ctx.getString(R.string.distance), track.distance.formatted())
        layout.second.fill(ctx.getString(R.string.duration), track.duration.formatted())
        layout.third.fill(ctx.getString(R.string.top), track.topSpeed?.formatted() ?: ctx.getString(R.string.na))
    }

    override fun getItemCount(): Int = tracks.size
}
