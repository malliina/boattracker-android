package com.malliina.boattracker.ui.profile

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.malliina.boattracker.Lang
import com.malliina.boattracker.Language
import com.malliina.boattracker.R
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.ui.ResourceFragment
import kotlinx.android.synthetic.main.profile_fragment.view.*

class ProfileFragment : ResourceFragment(R.layout.profile_fragment) {
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var client: GoogleSignInClient

    private var previousLanguage: Language? = null

    companion object {
        const val refreshSignIn = "com.malliina.boattracker.ui.profile.refreshSignIn"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.observe(viewLifecycleOwner) { state ->
            toggleSummary(state, view)
        }
        viewModel.currentTrack.observe(viewLifecycleOwner) { ref ->
            update(ref, view.track_summary)
        }
        client = Google.instance.client(requireContext())
        installTranslations(lang, view)
        view.tracks_link.setOnClickListener {
            navigate(ProfileFragmentDirections.profileToTracks(lang.track.tracks))
        }
        view.statistics_link.setOnClickListener {
            navigate(ProfileFragmentDirections.profileToStatistics(lang.labels.statistics))
        }
        view.boats_link.setOnClickListener {
            navigate(ProfileFragmentDirections.profileToBoats(lang.track.boats))
        }
        view.languages_link.setOnClickListener {
            navigate(ProfileFragmentDirections.profileToLanguages(lang.profile.language))
        }
        view.licenses_link.setOnClickListener {
            navigate(ProfileFragmentDirections.profileToAttributions(lang.attributions.title))
        }
        view.logout.setOnClickListener {
            client.signOut().addOnCompleteListener {
                userState.clear()
                val action = ProfileFragmentDirections.profileToMap(refresh = true)
                navigate(action)
            }
        }
    }

    private fun navigate(to: NavDirections) {
        findNavController().navigate(to)
    }

    @SuppressLint("SetTextI18n")
    private fun installTranslations(lang: Lang, view: View) {
        view.tracks_link.text = lang.track.trackHistory
        view.statistics_link.text = lang.labels.statistics
        view.boats_link.text = lang.track.boats
        view.languages_link.text = lang.profile.language
        view.licenses_link.text = lang.attributions.title
        view.userEmailMessage.text = "${lang.profile.signedInAs} ${user.email}"
        view.logout.text = lang.profile.logout
        view.track_summary.fillLabels(lang.track)
        val a = requireActivity()
        val versionName = a.packageManager.getPackageInfo(a.packageName, PackageManager.GET_ACTIVITIES).versionName
        view.versionText.text = "${lang.appMeta.version} $versionName"
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
        (activity as AppCompatActivity).supportActionBar?.show()
        if (lang.language != previousLanguage) {
            installTranslations(lang, requireView())
        }
        previousLanguage = lang.language
        token?.let {
            viewModel.openSocket(it, userState.userTrack?.track)
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
