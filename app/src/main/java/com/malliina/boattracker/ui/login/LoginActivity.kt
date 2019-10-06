package com.malliina.boattracker.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.malliina.boattracker.*
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.ui.profile.ProfileActivity
import timber.log.Timber

/**
 * See [Google's documentation](https://developers.google.com/identity/sign-in/android/start-integrating)
 */
class LoginActivity: AppCompatActivity(), View.OnClickListener {
    private val requestCodeSignIn = 100

    private lateinit var client: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.login_toolbar))
        setContentView(R.layout.login_activity)

        client = Google.instance.client(this)

        val settingsLang: SettingsLang = intent.getParcelableExtra(SettingsLang.key)
        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener(this)
        findViewById<Toolbar>(R.id.login_toolbar).title = settingsLang.signIn
    }

    override fun onStart() {
        super.onStart()
        updateUI(null)
    }

    override fun onClick(button: View?) {
        signIn()
    }

    private fun signIn() {
        Timber.i("Signing in...")
        val signInIntent = client.signInIntent
        startActivityForResult(signInIntent, requestCodeSignIn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.i("Got activity result of request $requestCode. Result code $resultCode.")
        if (requestCode == requestCodeSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val user = account?.let { a -> Google.readUser(a) }
            Timber.i("Sign in success.")
            updateUI(user)
        } catch (e: ApiException) {
            val str = CommonStatusCodes.getStatusCodeString(e.statusCode)
            Timber.w(e, "Sign in failed. Code ${e.statusCode}. $str.")
            updateFeedback("Sign in failed.")
        }
    }

    private fun updateUI(user: UserInfo?) {
        val msg = if (user != null) "Signed in as ${user.email}." else "Not signed in."
        updateFeedback(msg)
        user?.let {
            Timber.i("Updating UI with ${user.email} then finishing.")
            val intent = Intent().apply {
                this.putExtra(ProfileActivity.refreshSignIn, true)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun updateFeedback(text: String) {
        findViewById<TextView>(R.id.userStatus).text = text
    }
}
