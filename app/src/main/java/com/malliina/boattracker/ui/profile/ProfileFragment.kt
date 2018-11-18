package com.malliina.boattracker.ui.profile

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.malliina.boattracker.R
import com.malliina.boattracker.auth.Google

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var client: GoogleSignInClient
//    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            client = Google.instance.client(it)
        }
//        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
//        viewModel.getTracks().observe(this, Observer<List<String>> { tracks ->
//            val first = tracks?.first() ?: "no tracks"
//            activity?.findViewById<TextView>(R.id.message)?.text = first
//        })
    }

    fun signOutClicked(button: View) {
        client.signOut().addOnCompleteListener {
//            finish
        }
    }
}
