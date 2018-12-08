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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HttpClient(ctx: Context) {
    companion object {
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

    suspend fun getData(url: FullUrl): JSONObject {
        return try {
            readData(url)
        } catch(re: ResponseException) {
            if (re.isTokenExpired()) {
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
        override fun getHeaders(): Map<String, String> {
            return if (token != null) mapOf("Authorization" to "bearer $token")
            else mapOf()
        }
    }
}
