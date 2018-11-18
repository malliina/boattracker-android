package com.malliina.boattracker.ui.profile

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.malliina.boattracker.R

class ProfileParentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.parent_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ProfileFragment.newInstance())
                .commitNow()
        }
    }

}
