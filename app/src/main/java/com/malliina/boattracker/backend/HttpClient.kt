package com.malliina.boattracker.backend

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.malliina.boattracker.FullUrl
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.ResponseException
import com.malliina.boattracker.auth.Google
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HttpClient(ctx: Context) {
    companion object {
        fun headers(token: IdToken?): Map<String, String> {
            val acceptPair = "Accept" to "application/vnd.musicpimp.v2+json"
            return if (token != null) mapOf("Authorization" to "bearer $token", acceptPair) else mapOf(acceptPair)
        }

        @Volatile
        private var INSTANCE: HttpClient? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: HttpClient(context).also {
                    INSTANCE = it
                }
            }
    }

    init {
        Timber.tag(javaClass.simpleName)
    }

    private val queue: RequestQueue = Volley.newRequestQueue(ctx.applicationContext)
    private val google = Google.instance.client(ctx.applicationContext)

    var token: IdToken? = null

    suspend fun getData(url: FullUrl): JSONObject {
        return try {
            readData(url)
        } catch(re: ResponseException) {
            if (re.isTokenExpired()) {
                Timber.i("JWT is expired. Obtaining a new token and retrying...")
                val userInfo = Google.instance.signInSilently(google)
                token = userInfo.idToken
                readData(url)
            } else {
                throw re
            }
        }
    }

    // https://jankotlin.wordpress.com/2017/10/16/volley-for-lazy-kotliniers/
    private suspend fun readData(url: FullUrl): JSONObject = suspendCancellableCoroutine { cont ->
        RequestWithHeaders(url, token, cont).also {
            queue.add(it)
        }
    }

    class RequestWithHeaders(url: FullUrl, private val token: IdToken?, cont: CancellableContinuation<JSONObject>)
        : JsonObjectRequest(Request.Method.GET, url.url, null,
        Response.Listener { cont.resume(it) },
        Response.ErrorListener { error -> cont.resumeWithException(ResponseException(error))}) {
        override fun getHeaders(): Map<String, String> = HttpClient.headers(token)
    }
}
