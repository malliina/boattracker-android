package com.malliina.boattracker.ui.tracks

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackName

class TracksViewModel(val app: Application): AndroidViewModel(app) {
    private val tag = "TracksViewModel"
    private lateinit var tracks: MutableLiveData<List<TrackName>>
    private val queue = Volley.newRequestQueue(app)

    fun getTracks(token: IdToken): LiveData<List<TrackName>> {
        if (!::tracks.isInitialized) {
            tracks = MutableLiveData()
            loadTracks(token)
        }
        return tracks
    }

    private fun loadTracks(token: IdToken) {
        val req = object : JsonObjectRequest(Request.Method.GET, "https://www.boat-tracker.com/tracks", null,
            Response.Listener { response ->
                val list = ArrayList<TrackName>()
                val arr = response.getJSONArray("tracks")
                for(i in 0..(arr.length() -1)) {
                    val item = arr.getJSONObject(i)
                    val track = item.getJSONObject("track")
                    list.add(TrackName(track.getString("trackName")))
                }
                tracks.value = list
            },
            Response.ErrorListener { error ->
                Log.w(tag, "Failed to load tracks. Token was $token", error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf("Authorization" to "bearer $token")
            }
        }
        queue.add(req)
    }
}
