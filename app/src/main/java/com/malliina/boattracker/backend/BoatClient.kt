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
    }

    object Adapters {
        val user: JsonAdapter<UserResponse> = moshi.adapter(UserResponse::class.java)
        val conf: JsonAdapter<ClientConf> = moshi.adapter(ClientConf::class.java)
        val track: JsonAdapter<TrackResponse> = moshi.adapter(TrackResponse::class.java)
        val tracks: JsonAdapter<TracksResponse> = moshi.adapter(TracksResponse::class.java)
        val stats: JsonAdapter<StatsResponse> = moshi.adapter(StatsResponse::class.java)
        val errors: JsonAdapter<Errors> = moshi.adapter(Errors::class.java)
        val coords: JsonAdapter<CoordsMessage> = moshi.adapter(CoordsMessage::class.java)
        val event: JsonAdapter<EventName> = moshi.adapter(EventName::class.java)
        val language: JsonAdapter<ChangeLanguage> = moshi.adapter(ChangeLanguage::class.java)
        val title: JsonAdapter<ChangeTitle> = moshi.adapter(ChangeTitle::class.java)
        val message: JsonAdapter<SimpleMessage> = moshi.adapter(SimpleMessage::class.java)
    }

    suspend fun me(): BoatUser = get("/users/me", Adapters.user).user

    suspend fun changeLanguage(to: Language): SimpleMessage =
        put("/users/me", ChangeLanguage(to.code), Adapters.language, Adapters.message)

    suspend fun changeTitle(to: TrackTitle, of: TrackName): TrackRef =
        put("/tracks/$of", ChangeTitle(to), Adapters.title, Adapters.track).track

    suspend fun conf(): ClientConf = get("/conf", Adapters.conf)

    suspend fun tracks(): List<TrackRef> = get("/tracks", Adapters.tracks).tracks

    suspend fun stats(): StatsResponse = get("/stats", Adapters.stats)

    private suspend fun <T, U> put(
        path: String,
        payload: T,
        write: JsonAdapter<T>,
        read: JsonAdapter<U>
    ): U =
        http.put(Env.baseUrl.append(path), payload, write, read)

    private suspend fun <T> get(path: String, adapter: JsonAdapter<T>): T =
        http.getJson(Env.baseUrl.append(path), adapter)
}
