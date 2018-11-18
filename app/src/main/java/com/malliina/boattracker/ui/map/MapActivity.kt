package com.malliina.boattracker.ui.map

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.malliina.boattracker.BuildConfig
import com.malliina.boattracker.R
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.ui.login.LoginActivity
import com.malliina.boattracker.ui.profile.ProfileActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes

class MapActivity: AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var client: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, BuildConfig.MapboxAccessToken)
        setContentView(R.layout.map_activity)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        AppCenter.start(application, "768ec01e-fe9c-46b2-a05a-5389fa9d148f",
            Analytics::class.java, Crashes::class.java)
        client = Google.instance.client(this)
    }

    fun profileClicked(button: View) {
        client.silentSignIn().addOnCompleteListener { task ->
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let { a -> Google.readUser(a) }?.let { user ->
                    Log.i(localClassName, "Opening profile for ${user.email.email}...")
                    val intent = Intent(this, ProfileActivity::class.java).apply {
                        putExtra(ProfileActivity.userEmail, user.email.email)
                        putExtra(ProfileActivity.userToken, user.idToken.token)
                    }
                    startActivity(intent)
                }
            } catch (e: ApiException) {
                Log.i(localClassName, "Silent sign in failed. The user is probably not logged in.", e)
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun locationClicked(button: View) {
        info("Location")
    }

    private fun info(text: String) {
        Log.i(localClassName, text)
    }
}
