package com.malliina.boattracker.ui.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.malliina.boattracker.Lang
import com.malliina.boattracker.Language
import com.malliina.boattracker.R
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.ui.ResourceFragment
import kotlinx.android.synthetic.main.profile_activity.view.*

class ProfileFragment: ResourceFragment(R.layout.profile_activity) {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var client: GoogleSignInClient

    private var previousLanguage: Language? = null

    companion object {
        const val refreshSignIn = "com.malliina.boattracker.ui.profile.refreshSignIn"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        viewModel.getState().observe(viewLifecycleOwner) { state ->
            toggleSummary(state, view)
        }
        viewModel.getCurrent().observe(viewLifecycleOwner) { ref ->
            update(ref, view.track_summary)
        }
        client = Google.instance.client(requireContext())
        installTranslations(lang, view)
        view.tracks_link.setOnClickListener { tracksButton ->
            val action = ProfileFragmentDirections.profileToTracks()
            findNavController().navigate(action)
        }
        view.boats_link.setOnClickListener { boatsButton ->
            val action = ProfileFragmentDirections.profileToBoats()
            findNavController().navigate(action)
        }
        view.languages_link.setOnClickListener { languagesButton ->
            val action = ProfileFragmentDirections.profileToLanguages()
            findNavController().navigate(action)
        }
        view.licenses_link.setOnClickListener { licensesButton ->
            val action = ProfileFragmentDirections.profileToAttributions()
            findNavController().navigate(action)
        }
        view.logout.setOnClickListener { logoutButton ->
            client.signOut().addOnCompleteListener {
                settings.clear()
                val action = ProfileFragmentDirections.profileToMap(refresh = true)
                findNavController().navigate(action)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun installTranslations(lang: Lang, view: View) {
        view.profile_toolbar.title = lang.appName
        view.tracks_link.text = lang.track.trackHistory
        view.boats_link.text = lang.track.boats
        view.languages_link.text = lang.profile.language
        view.licenses_link.text = lang.attributions.title
        view.userEmailMessage.text = "${lang.profile.signedInAs} ${user.email}"
        view.logout.text = lang.profile.logout
        view.track_summary.fillLabels(lang.track)
    }

    private fun toggleSummary(state: LoadState, parent: View) {
        val summary = parent.track_summary
        val progress = parent.summary_progressbar
        when (state) {
            LoadState.Loading -> {
                progress.visibility = View.VISIBLE
                summary.visibility = View.INVISIBLE
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
        if (lang.language != previousLanguage) {
            installTranslations(lang, requireView())
        }
        previousLanguage = lang.language
        token?.let {
            viewModel.openSocket(it, settings.mapState?.track)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }

    private fun update(track: TrackRef, view: TrackSummaryBox) {
        view.fill(track, lang.messages)
    }
}
