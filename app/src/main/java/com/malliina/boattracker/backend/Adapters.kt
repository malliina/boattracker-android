package com.malliina.boattracker.backend

import com.malliina.boattracker.*
import com.squareup.moshi.adapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

@OptIn(ExperimentalStdlibApi::class)
object Adapters {
    private val moshi: Moshi = Json.moshi

    val user: JsonAdapter<UserResponse> = moshi.adapter()
    val conf: JsonAdapter<ClientConf> = moshi.adapter()
    val track: JsonAdapter<TrackResponse> = moshi.adapter()
    val tracks: JsonAdapter<TracksResponse> = moshi.adapter()
    val stats: JsonAdapter<StatsResponse> = moshi.adapter()
    val errors: JsonAdapter<Errors> = moshi.adapter()
    val coords: JsonAdapter<CoordsMessage> = moshi.adapter()
    val vessels: JsonAdapter<VesselMessage> = moshi.adapter()
    val event: JsonAdapter<EventName> = moshi.adapter()
    val language: JsonAdapter<ChangeLanguage> = moshi.adapter()
    val title: JsonAdapter<ChangeTitle> = moshi.adapter()
    val message: JsonAdapter<SimpleMessage> = moshi.adapter()
}
