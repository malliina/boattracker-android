package com.malliina.boattracker.ui.tracks

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.R
import com.malliina.boattracker.TrackName

class TracksActivity: AppCompatActivity() {
    companion object {
        const val tokenExtra = "com.malliina.boattracker.token"
    }

    private lateinit var viewModel: TracksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracks_activity)
        Log.i(localClassName, "Loading tracks...")
        val token = IdToken(intent.getStringExtra(tokenExtra))
        viewModel = ViewModelProviders.of(this).get(TracksViewModel::class.java)
        viewModel.getTracks(token).observe(this, Observer<List<TrackName>> { tracks ->
            val first = tracks?.first()?.name ?: "no tracks"
            findViewById<TextView>(R.id.tracks_message)?.text = first
        })
    }
}
