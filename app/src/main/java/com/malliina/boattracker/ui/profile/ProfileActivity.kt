package com.malliina.boattracker.ui.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.malliina.boattracker.*
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.ui.boats.BoatsActivity
import com.malliina.boattracker.ui.tracks.TracksActivity

class ProfileActivity: AppCompatActivity() {
    companion object {
        const val userEmail = "com.malliina.boattracker.userEmail"
        const val userToken = "com.malliina.boattracker.userToken"
        const val selectTrackRequest = 100
    }

    private lateinit var viewModel: ProfileViewModel

    private lateinit var client: GoogleSignInClient
    private lateinit var email: Email
    private lateinit var token: IdToken
    private var trackName: TrackName? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        viewModel.getState().observe(this, Observer { state ->
            state?.let { s -> toggleSummary(s) }
        })
        viewModel.getCurrent().observe(this, Observer { ref ->
            ref?.let { update(it) }
        })
        client = Google.instance.client(this)
        email = Email(intent.getStringExtra(userEmail))
        token = IdToken(intent.getStringExtra(userToken))
        trackName = intent.getStringExtra(TracksActivity.trackNameExtra)?.let { TrackName(it) }
        findViewById<TextView>(R.id.userEmailMessage).text = getString(R.string.signedInAs, email)
    }

    private fun toggleSummary(state: LoadState) {
        val summary = findViewById<TrackSummaryBox>(R.id.track_summary)
        val progress = findViewById<ProgressBar>(R.id.indeterminateBar)
        when (state) {
            LoadState.Loading -> {
                summary.visibility = View.INVISIBLE
                progress.visibility = View.VISIBLE
            }
            LoadState.Loaded -> {
                progress.visibility = View.INVISIBLE
                summary.visibility = View.VISIBLE
            }
            LoadState.NotLoaded -> {
                progress.visibility = View.INVISIBLE
                summary.visibility = View.INVISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.openSocket(token, trackName)
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }

    private fun update(track: TrackRef) {
        findViewById<TrackSummaryBox>(R.id.track_summary).fill(track)
    }

    fun tracksClicked(button: View) {
        val intent = Intent(this, TracksActivity::class.java).apply {
            putExtra(TracksActivity.tokenExtra, token.token)
        }
        startActivityForResult(intent, selectTrackRequest)
    }

    fun boatsClicked(button: View) {
        val intent = Intent(this, BoatsActivity::class.java).apply {
            putExtra(TracksActivity.tokenExtra, token.token)
        }
        startActivity(intent)
    }

    fun signOutClicked(button: View) {
        client.signOut().addOnCompleteListener {
            finish()
        }
    }
}
