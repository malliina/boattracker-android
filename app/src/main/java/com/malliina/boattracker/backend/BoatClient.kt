package com.malliina.boattracker.backend

import android.content.Context
import com.malliina.boattracker.BoatUser
import com.malliina.boattracker.ClientConf
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackRef

class BoatClient(val http: HttpClient) {
    companion object {
        fun build(ctx: Context, token: IdToken): BoatClient {
            val http = HttpClient.getInstance(ctx)
            http.token = token
            return BoatClient(http)
        }

        fun basic(ctx: Context): BoatClient {
            val http = HttpClient.getInstance(ctx)
            return BoatClient(http)
        }
    }

    suspend fun me(): BoatUser {
        val response = http.getData(Env.baseUrl.append("/users/me"))
        return BoatUser.parse(response.getJSONObject("user"))
    }

    suspend fun conf(): ClientConf {
        val response = http.getData(Env.baseUrl.append("/conf"))
        return ClientConf.parse(response)
    }

    suspend fun tracks(): List<TrackRef> {
        val response = http.getData(Env.baseUrl.append("/tracks"))
        return TrackRef.parseList(response)
    }
}
