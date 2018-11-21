package com.malliina.boattracker.ui.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.malliina.boattracker.R
import com.malliina.boattracker.UserInfo
import com.malliina.boattracker.auth.Google

/**
 * See [Google's documentation](https://developers.google.com/identity/sign-in/android/start-integrating)
 */
class LoginActivity: AppCompatActivity(), View.OnClickListener {
    private val requestCodeSignIn = 100

    private lateinit var client: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        client = Google.instance.client(this)
        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        updateUI(null)
    }

    override fun onClick(button: View?) {
        signIn()
    }

    private fun signIn() {
        info("Signing in...")
        val signInIntent = client.signInIntent
        startActivityForResult(signInIntent, requestCodeSignIn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        info("Got activity result with $requestCode")
        if (requestCode == requestCodeSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val user = account?.let { a -> Google.readUser(a) }
            info("Sign in success")
            updateUI(user)
        } catch (e: ApiException) {
            Log.w(localClassName, "Sign in failed. Code ${e.statusCode}", e)
            updateFeedback("Sign in failed.")
        }
    }

    private fun updateUI(user: UserInfo?) {
        val msg = if (user != null) "Signed in as ${user.email}." else "Not signed in."
        updateFeedback(msg)
        user?.let {
            info("Updating UI with ${user.email} then finishing")
            finish()
        }
    }

    private fun updateFeedback(text: String) {
        findViewById<TextView>(R.id.userStatus).text = text
    }

    private fun info(msg: String) {
        Log.i(localClassName, msg)
    }
}
