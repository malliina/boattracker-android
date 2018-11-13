package com.malliina.boattracker.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private lateinit var tracks: MutableLiveData<List<String>>

    fun getTracks(): LiveData<List<String>> {
        if (!::tracks.isInitialized) {
            tracks = MutableLiveData()
            loadTracks()
        }
        return tracks
    }

    private fun loadTracks() {
        tracks.value = listOf("ARGH", "B", "C")
    }
}
