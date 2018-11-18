package com.malliina.boattracker.ui.profile

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.malliina.boattracker.Email
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.R
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.ui.tracks.TracksActivity

class ProfileActivity: AppCompatActivity() {
    companion object {
        const val userEmail = "com.malliina.boattracker.userEmail"
        const val userToken = "com.malliina.boattracker.userToken"
    }

    private lateinit var client: GoogleSignInClient
    private lateinit var email: Email
    private lateinit var token: IdToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)
        client = Google.instance.client(this)
        email = Email(intent.getStringExtra(userEmail))
        token = IdToken(intent.getStringExtra(userToken))
        findViewById<TextView>(R.id.userEmailMessage).text = getString(R.string.signedInAs, email)
    }

    fun tracksClicked(button: View) {
        val intent = Intent(this, TracksActivity::class.java).apply {
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
