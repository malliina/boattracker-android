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

    private val queue: RequestQueue = Volley.newRequestQueue(ctx.applicationContext)
    private val google = Google.instance.client(ctx.applicationContext)

    var token: IdToken? = null

    // https://jankotlin.wordpress.com/2017/10/16/volley-for-lazy-kotliniers/
    suspend fun getData(url: FullUrl): JSONObject = makeWithRetry(RequestConf.get(url, token))

    suspend fun postData(url: FullUrl, data: JSONObject): JSONObject =
        makeWithRetry(RequestConf(Request.Method.POST, url, token, data))

    private suspend fun makeWithRetry(conf: RequestConf): JSONObject =
        try {
            makeRequest(conf)
        } catch(e: ResponseException) {
            if (e.isTokenExpired()) {
                Timber.i("JWT is expired. Obtaining a new token and retrying...")
                val userInfo = Google.instance.signInSilently(google)
                token = userInfo.idToken
                makeRequest(conf.copy(token = userInfo.idToken))
            } else {
                throw e
            }
        }

    private suspend fun makeRequest(conf: RequestConf): JSONObject = suspendCancellableCoroutine { cont ->
        RequestWithHeaders(conf, cont).also {
            queue.add(it)
        }
    }

    class RequestWithHeaders(private val conf: RequestConf, cont: CancellableContinuation<JSONObject>)
        : JsonObjectRequest(conf.method, conf.url.url, conf.payload,
        Response.Listener { cont.resume(it) },
        Response.ErrorListener { error ->
            val exception = ResponseException(error)
            try {
                val errors = exception.errors()
                Timber.e("Request failed with errors $errors.")
                // This try-catch is only for error logging purposes; the error must be handled by the caller later
            } catch(e: Exception) {}
            cont.resumeWithException(exception)
        }) {
        private val csrf =
            if (conf.method == Request.Method.POST) mapOf("Csrf-Token" to "nocheck", "Content-Type" to "application/json")
            else emptyMap()
        override fun getHeaders(): Map<String, String> = headers(conf.token).plus(csrf)
    }
}

data class RequestConf(val method: Int,
                       val url: FullUrl,
                       val token: IdToken?,
                       val payload: JSONObject?) {
    companion object {
        fun get(url: FullUrl, token: IdToken?): RequestConf =
            RequestConf(Request.Method.GET, url, token, null)
    }
}
