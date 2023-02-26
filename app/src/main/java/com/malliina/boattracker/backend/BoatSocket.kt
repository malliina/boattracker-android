package com.malliina.boattracker.backend

import android.content.Context
import com.malliina.boattracker.*
import com.malliina.boattracker.auth.Google
import com.malliina.boattracker.backend.HttpClient.Companion.Authorization
import com.neovisionaries.ws.client.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface SocketDelegate {
    fun onCoords(newCoords: CoordsData)
    fun onVessels(vessels: List<Vessel>)
    fun onNewToken(user: UserInfo)
}

@JsonClass(generateAdapter = true)
data class EventName(val event: String)

fun <T> JsonAdapter<T>.read(json: String): T {
    return this.fromJson(json)
        ?: throw JsonDataException("Moshi returned null when reading '$json'.")
}

fun <T> JsonAdapter<T>.readUrl(json: String, url: FullUrl): T {
    return this.fromJson(json)
        ?: throw JsonDataException("Moshi returned null for response from '$url': '$json'.")
}

class BoatSocket(
    val url: FullUrl,
    headers: Map<String, String>,
    private val delegate: SocketDelegate,
    ctx: Context
) {
    companion object {
        private val baseUrl = Env.socketsUrl

        fun token(
            token: IdToken?,
            track: TrackName?,
            delegate: SocketDelegate,
            ctx: Context
        ): BoatSocket {
            val socketUrl = if (track == null) baseUrl else baseUrl.append("?track=$track")
            Timber.i("Setting socketUrl to '$socketUrl'.")
            return BoatSocket(socketUrl, HttpClient.headers(token), delegate, ctx)
        }
    }

    private val google = Google.instance.client(ctx.applicationContext)
    private val sf: WebSocketFactory = WebSocketFactory()

    // var because it's recreated on reconnects
    private var socket = sf.createSocket(url.url, 10000)
    private val listener = object : WebSocketAdapter() {
        override fun onTextMessage(websocket: WebSocket?, text: String?) {
            try {
                text?.let { onMessage(it) }
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
        }
    }

    init {
        socket.addListener(listener)
        headers.forEach { (k, v) -> socket.addHeader(k, v) }
    }

    fun onMessage(message: String) {
//        Timber.i("Message '$message'.")
        when (Adapters.event.fromJson(message)?.event) {
            "coords" -> {
                val coords = Adapters.coords.read(message).body
                delegate.onCoords(coords)
            }
            "vessels" -> {
                val vessels = Adapters.vessels.read(message).body
                delegate.onVessels(vessels.vessels)
            }
            else -> {
            }
        }
    }

    suspend fun connectWithRetry() {
        withContext(Dispatchers.IO) {
            try {
                connect()
            } catch (e: Exception) {
                Timber.w(e, "Unable to connect to '$url'. Refreshing token and retrying...")
                val userInfo = Google.instance.signInSilently(google)
                socket.removeHeaders(Authorization)
                socket = socket.recreate()
                HttpClient.headers(userInfo.idToken).forEach { (k, v) -> socket.addHeader(k, v) }
                delegate.onNewToken(userInfo)
                connect()
            }
        }
    }

    private suspend fun connect(): WebSocket? =
        suspendCancellableCoroutine { cont ->
            Timber.i("Connecting to '$url'...")
            val connectCallback = object : WebSocketAdapter() {
                override fun onConnected(
                    websocket: WebSocket?,
                    headers: MutableMap<String, MutableList<String>>?
                ) {
                    Timber.i("Connected to '$url'.")
                    socket.removeListener(this)
                    cont.resume(websocket)
                }

                override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
                    Timber.w("Unable to connect to '$url'.")
                    val e = exception ?: Exception("Unable to connect to '$url'.")
                    socket.removeListener(this)
                    cont.resumeWithException(e)
                }
            }
            socket.addListener(connectCallback)
            socket.connectAsynchronously()
        }

    fun disconnect() {
        val wasOpen = socket.isOpen
        socket.removeListener(listener)
        socket.disconnect()
        if (wasOpen) {
            Timber.i("Disconnected from '$url'.")
        }
    }
}
