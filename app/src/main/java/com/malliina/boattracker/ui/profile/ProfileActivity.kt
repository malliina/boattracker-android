package com.malliina.boattracker.ui.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.malliina.boattracker.Email
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.R
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.ui.LabeledStat
import com.malliina.boattracker.ui.boats.BoatsActivity
import com.malliina.boattracker.ui.tracks.TracksActivity

class ProfileActivity: AppCompatActivity() {
    companion object {
        const val userEmail = "com.malliina.boattracker.userEmail"
        const val userToken = "com.malliina.boattracker.userToken"
    }

    private lateinit var viewModel: ProfileViewModel

    private lateinit var client: GoogleSignInClient
    private lateinit var email: Email
    private lateinit var token: IdToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        viewModel.getCurrent().observe(this, Observer { ref ->
            ref?.let { update(it) }
        })
        client = Google.instance.client(this)
        email = Email(intent.getStringExtra(userEmail))
        token = IdToken(intent.getStringExtra(userToken))
        findViewById<TextView>(R.id.userEmailMessage).text = getString(R.string.signedInAs, email)
    }

    override fun onStart() {
        super.onStart()
        viewModel.openSocket(token)
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }

    private fun update(track: TrackRef) {
        val na = getString(R.string.na)
        fillStat(R.id.duration, R.string.duration, track.duration.formatted())
        fillStat(R.id.distance, R.string.distance, track.distance.formatted())
        fillStat(R.id.topSpeed, R.string.topSpeed, track.topSpeed?.formatted() ?: na)
        fillStat(R.id.avgSpeed, R.string.avgSpeed, track.avgSpeed?.formatted() ?: na)
        fillStat(R.id.waterTemp, R.string.waterTemp, track.avgWaterTemp?.formatted() ?: na)
        fillStat(R.id.date, R.string.date, track.formatStart())
    }

    fun fillStat(id: Int, labelRes: Int, value: String) {
        findViewById<LabeledStat>(id).fill(getString(labelRes), value)
    }

    fun tracksClicked(button: View) {
        val intent = Intent(this, TracksActivity::class.java).apply {
            putExtra(TracksActivity.tokenExtra, token.token)
        }
        startActivity(intent)
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
