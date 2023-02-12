package com.malliina.boattracker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.malliina.boattracker.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            setupActionBarWithNavController(navController())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupActionBarWithNavController(navController())
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController().navigateUp()
    }

    private fun navController() = findNavController(R.id.nav_host_fragment)
}
