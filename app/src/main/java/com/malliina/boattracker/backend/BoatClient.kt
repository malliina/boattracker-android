package com.malliina.boattracker.backend

import android.content.Context
import com.malliina.boattracker.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

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
        private val moshi: Moshi = Json.moshi
        val userAdapter: JsonAdapter<UserResponse> = moshi.adapter(UserResponse::class.java)
        val confAdapter: JsonAdapter<ClientConf> = moshi.adapter(ClientConf::class.java)
        val tracksAdapter: JsonAdapter<TracksResponse> = moshi.adapter(TracksResponse::class.java)
        val statsAdapter: JsonAdapter<StatsResponse> = moshi.adapter(StatsResponse::class.java)
        val errorsAdapter: JsonAdapter<Errors> = moshi.adapter(Errors::class.java)
        val coordsAdapter: JsonAdapter<CoordsMessage> = moshi.adapter(CoordsMessage::class.java)
        val eventAdapter: JsonAdapter<EventName> = moshi.adapter(EventName::class.java)
        val languageAdapter: JsonAdapter<ChangeLanguage> = moshi.adapter(ChangeLanguage::class.java)
        val messageAdapter: JsonAdapter<SimpleMessage> = moshi.adapter(SimpleMessage::class.java)
    }

    suspend fun me(): BoatUser {
        return http.getJson(Env.baseUrl.append("/users/me"), userAdapter).user
    }

    suspend fun changeLanguage(to: Language): SimpleMessage =
        http.put(Env.baseUrl.append("/users/me"), ChangeLanguage(to.code), languageAdapter, messageAdapter)

    suspend fun conf(): ClientConf = http.getJson(Env.baseUrl.append("/conf"), confAdapter)

    suspend fun tracks(): List<TrackRef> = http.getJson(Env.baseUrl.append("/tracks"), tracksAdapter).tracks

    suspend fun stats(): StatsResponse = http.getJson(Env.baseUrl.append("/stats"), statsAdapter)
}
