package com.malliina.boattracker.backend

import android.content.Context
import com.malliina.boattracker.BoatUser
import com.malliina.boattracker.ChangeLanguage
import com.malliina.boattracker.ChangeTitle
import com.malliina.boattracker.ClientConf
import com.malliina.boattracker.Errors
import com.malliina.boattracker.FullUrl
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.Language
import com.malliina.boattracker.SimpleMessage
import com.malliina.boattracker.StatsResponse
import com.malliina.boattracker.TrackName
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.TrackTitle
import com.malliina.boattracker.auth.Google
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.closeQuietly
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

open class HttpException(message: String, val request: Request) : Exception(message) {
    val httpUrl = request.url
    val url = FullUrl.build(request.url.toString())
}

class BodyException(request: Request) : HttpException("Invalid HTTP response body.", request)

open class StatusException(val code: Int, request: Request) :
    HttpException("Invalid status code $code from '${request.url}'.", request)

class ErrorsException(val errors: Errors, code: Int, request: Request) :
    StatusException(code, request) {
    val isTokenExpired: Boolean get() = errors.errors.any { e -> e.key == "token_expired" }
}

interface TokenSource {
    suspend fun fetchToken(): IdToken?

    companion object {
        val empty = object : TokenSource {
            override suspend fun fetchToken(): IdToken? = null
        }
    }
}

class GoogleTokenSource(appContext: Context) : TokenSource {
    private val google = Google.instance.client(appContext)

    override suspend fun fetchToken(): IdToken? = try {
        Google.instance.signInSilently(google).idToken
    } catch (e: Exception) {
        Timber.w(e, "Failed to fetch token")
        null
    }
}

class BoatHttpClient(private val tokenSource: TokenSource) {
    companion object {
        private const val Accept = "Accept"
        const val Authorization = "Authorization"
        private val MediaTypeJson = "application/vnd.boat.v2+json".toMediaType()

        fun headers(token: IdToken?): Map<String, String> {
            val acceptPair = Accept to MediaTypeJson.toString()
            return if (token != null) mapOf(Authorization to "Bearer $token", acceptPair) else mapOf(acceptPair)
        }
    }
    private var token: IdToken? = null

    private suspend fun fetchToken() =
        token ?: run {
            val t = tokenSource.fetchToken()
            token = t
            t
        }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun me(): BoatUser = get("/users/me", Adapters.user).user
    suspend fun conf(): ClientConf = get("/conf", Adapters.conf)
    suspend fun tracks(): List<TrackRef> = get("/tracks", Adapters.tracks).tracks
    suspend fun stats(): StatsResponse = get("/stats", Adapters.stats)
    suspend fun <T> get(path: String, adapter: JsonAdapter<T>): T {
        val request = authRequest(Env.baseUrl.append(path)).get().build()
        return execute(request, adapter)
    }
    suspend fun changeLanguage(to: Language): SimpleMessage =
        put("/users/me", ChangeLanguage(to.code), Adapters.language, Adapters.message)
    suspend fun changeTitle(to: TrackTitle, of: TrackName): TrackRef =
        put("/tracks/$of", ChangeTitle(to), Adapters.title, Adapters.track).track

    suspend fun <Req, Res> put(
        path: String,
        body: Req,
        writer: JsonAdapter<Req>,
        reader: JsonAdapter<Res>
    ): Res = withContext(Dispatchers.IO) {
        val url = Env.baseUrl.append(path)
        val requestBody = writer.toJson(body).toRequestBody(MediaTypeJson)
        execute(authRequest(url).put(requestBody).build(), reader)
    }

    private suspend fun <T> execute(request: Request, reader: JsonAdapter<T>): T =
        try {
            executeOnce(request, reader)
        } catch (e: ErrorsException) {
            if (e.isTokenExpired) {
                Timber.i("JWT is expired. Obtaining a new token and retrying...")
                val newToken = tokenSource.fetchToken()
                if (newToken != null) {
                    token = newToken
                    val newAttempt =
                        request.newBuilder().header(Authorization, "Bearer $newToken").build()
                    executeOnce(newAttempt, reader)
                } else {
                    Timber.w("Token expired and unable to renew token. Failing request to '${request.url}'.")
                    throw e
                }
            } else {
                throw e
            }
        }

    private suspend fun <T> executeOnce(request: Request, reader: JsonAdapter<T>): T =
        withContext(Dispatchers.IO) {
            make(client.newCall(request)).use { response ->
                val body = response.body
                if (response.isSuccessful) {
                    body?.let { b ->
                        reader.fromJson(b.source())  ?: throw JsonDataException("Moshi returned null for response body from '${request.url}'.")
                    } ?: run {
                        throw BodyException(request)
                    }
                } else {
                    val errors = body?.let { b ->
                        try { Adapters.errors.read(b.string()) }
                        catch (e: Exception) { null }
                    }
                    errors?.let { throw ErrorsException(it, response.code, request) } ?: run {
                        throw StatusException(response.code, request)
                    }
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun make(call: Call): Response = suspendCancellableCoroutine { cont ->
        val callback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                cont.resume(response) {
                    // If we have a response but we're cancelled while resuming, we need to
                    // close() the unused response
                    if (response.body != null) {
                        response.closeQuietly()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }
        }
        call.enqueue(callback)
        cont.invokeOnCancellation {
            try {
                call.cancel()
            } catch (t: Throwable) {
                // Ignore cancel exception
            }
        }
    }

    private suspend fun authRequest(url: FullUrl) =
        newRequest(url, headers(fetchToken()))

    private fun newRequest(url: FullUrl, headers: Map<String, String>): Request.Builder {
        val builder = Request.Builder().url(url.url)
        for ((k, v) in headers) {
            builder.header(k, v)
        }
        return builder
    }
}
