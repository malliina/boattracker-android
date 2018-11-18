package com.malliina.boattracker.auth

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.malliina.boattracker.Email
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.UserInfo

class Google {
    companion object {
        val instance = Google()

        fun readUser(account: GoogleSignInAccount): UserInfo? {
            val idToken = account.idToken
            val email = account.email
            return idToken?.let { token ->
                email?.let { email ->
                    UserInfo(Email(email), IdToken(token))
                }
            }
        }
    }

    private val webClientId = "497623115973-c6v1e9khup8bqj41vf228o2urnv86muh.apps.googleusercontent.com"

    fun client(activity: Activity): GoogleSignInClient {
        val options =  GoogleSignInOptions.Builder()
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, options)
    }
}
