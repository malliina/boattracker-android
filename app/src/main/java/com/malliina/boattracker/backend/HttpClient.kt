package com.malliina.boattracker.backend

import android.content.Context
import com.malliina.boattracker.IdToken

class HttpClient(ctx: Context) {
    companion object {
        const val Authorization = "Authorization"

        fun headers(token: IdToken?): Map<String, String> {
            val acceptPair = "Accept" to "application/vnd.boat.v2+json"
            return if (token != null) mapOf(Authorization to "bearer $token", acceptPair) else mapOf(acceptPair)
        }
    }
}
