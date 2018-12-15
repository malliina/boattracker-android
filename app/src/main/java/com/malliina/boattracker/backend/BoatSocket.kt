package com.malliina.boattracker.backend

import com.malliina.boattracker.CoordsData
import com.malliina.boattracker.FullUrl
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.TrackName
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import org.json.JSONObject
import timber.log.Timber

interface SocketDelegate {
    fun onCoords(coords: CoordsData)
}

class BoatSocket(url: FullUrl, headers: Map<String, String>, private val delegate: SocketDelegate) {
    companion object {
        val url = Env.socketsUrl

        fun token(token: IdToken?, track: TrackName?, delegate: SocketDelegate): BoatSocket {
            val socketUrl = if (track == null) url else url.append("?track=$track")
            return BoatSocket(socketUrl, HttpClient.headers(token), delegate)
        }
    }

    fun onMessage(message: JSONObject) {
        val event = message.getString("event")
        when (event) {
            "coords" -> onCoords(CoordsData.parse(message.getJSONObject("body")))
            else -> Unit
        }
    }

    private fun onCoords(coords: CoordsData) {
        Timber.i("Got ${coords.coords.size} coords.")
        delegate.onCoords(coords)
    }

    private val listener = object: WebSocketAdapter() {
        override fun onConnected(websocket: WebSocket?, headers: MutableMap<String, MutableList<String>>?) {
            Timber.i("Connected to '$url'.")
            super.onConnected(websocket, headers)
        }

        override fun onTextMessage(websocket: WebSocket?, text: String?) {
            try {
                val json = JSONObject(text)
                onMessage(json)
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
        Timber.tag("BoatSocket")
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
