package com.malliina.boattracker.backend

import com.malliina.boattracker.CoordsData
import com.malliina.boattracker.FullUrl
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackName
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import timber.log.Timber

interface SocketDelegate {
    fun onCoords(newCoords: CoordsData)
}

data class CoordsMessage(val body: CoordsData)

data class EventName(val event: String)

fun <T> JsonAdapter<T>.read(json: String): T {
    return this.fromJson(json) ?: throw JsonDataException("Moshi returned null when reading '$json'.")
}

fun <T> JsonAdapter<T>.readUrl(json: String, url: FullUrl): T {
    return this.fromJson(json) ?: throw JsonDataException("Moshi returned null for response from '$url': '$json'.")
}

class BoatSocket(val url: FullUrl, headers: Map<String, String>, private val delegate: SocketDelegate) {
    companion object {
        private val baseUrl = Env.socketsUrl

        fun token(token: IdToken?, track: TrackName?, delegate: SocketDelegate): BoatSocket {
            val socketUrl = if (track == null) baseUrl else baseUrl.append("?track=$track")
            Timber.i("Setting socketUrl to $socketUrl")
            return BoatSocket(socketUrl, HttpClient.headers(token), delegate)
        }
    }

    fun onMessage(message: String) {
        when (BoatClient.eventAdapter.fromJson(message)?.event) {
            "coords" -> {
                val coords = BoatClient.coordsAdapter.read(message).body
                onCoords(coords)
            }
            else -> {
            }
        }
    }

    private fun onCoords(newCoords: CoordsData) {
        Timber.i("Got ${newCoords.coords.size} coords.")
        delegate.onCoords(newCoords)
    }

    private val listener = object: WebSocketAdapter() {
        override fun onConnected(websocket: WebSocket?, headers: MutableMap<String, MutableList<String>>?) {
            Timber.i("Connected to '$url'.")
            super.onConnected(websocket, headers)
        }

        override fun onTextMessage(websocket: WebSocket?, text: String?) {
            try {
                text?.let { onMessage(it) }
//                val json = JSONObject(text)
//                onMessage(json)
            } catch (e: Exception) {
                Timber.e(e, "JSON error.")
            }
        }

        override fun onDisconnected(
            websocket: WebSocket?,
            serverCloseFrame: WebSocketFrame?,
            clientCloseFrame: WebSocketFrame?,
            closedByServer: Boolean
        ) {
            Timber.i("Disconnected from '$url'.")
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
        }
    }

    private val sf: WebSocketFactory = WebSocketFactory()
    private val socket = sf.createSocket(url.url, 10000)

    init {
        socket.addListener(listener)
        headers.forEach { (k, v) -> socket.addHeader(k, v) }
    }

    fun connect() {
        Timber.i("Connecting to '$url'...")
        socket.connectAsynchronously()
    }

    fun disconnect() {
        socket.removeListener(listener)
        socket.disconnect()
    }
}
