package com.malliina.boattracker

import com.squareup.moshi.Moshi

class Json {
    companion object {
        val instance = Json()
        val moshi: Moshi get() = instance.moshi
    }

    val moshi: Moshi = Moshi.Builder().add(PrimitiveAdapter()).build()
}
