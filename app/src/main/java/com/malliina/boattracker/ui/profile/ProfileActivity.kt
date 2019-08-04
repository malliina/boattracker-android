package com.malliina.boattracker.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.button.MaterialButton
import com.malliina.boattracker.*
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.ui.attributions.AttributionsActivity
import com.malliina.boattracker.ui.boats.BoatsActivity
import com.malliina.boattracker.ui.language.LanguagesActivity
import com.malliina.boattracker.ui.tracks.TracksActivity

class ProfileActivity: AppCompatActivity() {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var client: GoogleSignInClient

    private lateinit var profile: ProfileInfo
    private val lang: Lang get() = profile.lang

    val token get() = profile.token
    private val trackName get() = profile.trackName

    companion object {
        const val refreshSignIn = "com.malliina.boattracker.ui.profile.refreshSignIn"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)
        setSupportActionBar(findViewById(R.id.profile_toolbar))
        profile = intent.getParcelableExtra(ProfileInfo.key)
        val summary = findViewById<TrackSummaryBox>(R.id.track_summary)
        summary.fillLabels(profile.lang.track)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        viewModel.getState().observe(this, Observer { state ->
            state?.let { s -> toggleSummary(s) }
        })
        viewModel.getCurrent().observe(this, Observer { ref ->
            ref?.let { update(it) }
        })
        client = Google.instance.client(this)
        findViewById<Toolbar>(R.id.profile_toolbar).title = lang.appName
        findViewById<MaterialButton>(R.id.tracks_link).text = lang.track.trackHistory
        findViewById<MaterialButton>(R.id.boats_link).text = lang.track.boats
        findViewById<MaterialButton>(R.id.languages_link).text = lang.profile.language
        findViewById<MaterialButton>(R.id.licenses_link).text = lang.attributions.title
        findViewById<TextView>(R.id.userEmailMessage).text = "${lang.profile.signedInAs} ${profile.email}"
        findViewById<MaterialButton>(R.id.logout).text = lang.profile.logout
    }

    private fun toggleSummary(state: LoadState) {
        val summary = findViewById<TrackSummaryBox>(R.id.track_summary)
        val shimmer = findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        when (state) {
            LoadState.Loading -> {
                shimmer.startShimmer()
                summary.visibility = View.INVISIBLE
                shimmer.visibility = View.VISIBLE
            }
            LoadState.Loaded -> {
                shimmer.stopShimmer()
                shimmer.visibility = View.INVISIBLE
                summary.visibility = View.VISIBLE
            }
            LoadState.NotLoaded -> {
                shimmer.stopShimmer()
                shimmer.visibility = View.INVISIBLE
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
        findViewById<TrackSummaryBox>(R.id.track_summary).fill(track, lang.messages)
    }

    fun tracksClicked(button: View) {
        val intent = Intent(this, TracksActivity::class.java).apply {
            putExtra(IdToken.key, token)
            putExtra(Lang.key, lang)
        }
        startActivity(intent)
    }

    fun boatsClicked(button: View) {
        val intent = Intent(this, BoatsActivity::class.java).apply {
            putExtra(IdToken.key, token)
            putExtra(Lang.key, lang)
        }
        startActivity(intent)
    }

    fun languagesClicked(button: View) {
        val intent = Intent(this, LanguagesActivity::class.java).apply {
            putExtra(ProfileInfo.key, profile)
        }
        startActivity(intent)
    }

    fun licensesClicked(button: View) {
        val intent = Intent(this, AttributionsActivity::class.java).apply {
            putExtra(AttributionInfo.key, lang.attributions)
        }
        startActivity(intent)
    }

    fun signOutClicked(button: View) {
        client.signOut().addOnCompleteListener {
            val intent = Intent().apply {
                this.putExtra(refreshSignIn, true)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
