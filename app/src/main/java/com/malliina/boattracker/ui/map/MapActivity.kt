package com.malliina.boattracker.ui.map

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import com.malliina.boattracker.BuildConfig
import com.malliina.boattracker.R
import com.malliina.boattracker.UserInfo
import com.malliina.boattracker.ui.login.LoginActivity
import com.malliina.boattracker.ui.profile.ProfileActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes

class MapActivity: AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var viewModel: MapViewModel
    private var user: UserInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, BuildConfig.MapboxAccessToken)
        setContentView(R.layout.map_activity)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        AppCenter.start(application, "768ec01e-fe9c-46b2-a05a-5389fa9d148f",
            Analytics::class.java, Crashes::class.java)

        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        viewModel.getUser(this).observe(this, Observer { userInfo ->
            user = userInfo
            findViewById<Button>(R.id.profile).visibility = Button.VISIBLE
        })
    }

    override fun onStart() {
        super.onStart()
        // https://developers.google.com/identity/sign-in/android/backend-auth
        viewModel.refreshUserInfo(this)
    }

    fun profileClicked(button: View) {
        val u = user
        if(u != null) {
            info( "Opening profile for ${u.email}...")
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra(ProfileActivity.userEmail, u.email.email)
                putExtra(ProfileActivity.userToken, u.idToken.token)
            }
            startActivity(intent)
        } else {
            info("Opening login screen...")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    fun locationClicked(button: View) {
        info("Location")
    }

    private fun info(text: String) {
        Log.i(localClassName, text)
    }
}
