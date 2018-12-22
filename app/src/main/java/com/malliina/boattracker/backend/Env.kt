package com.malliina.boattracker.backend

import com.malliina.boattracker.FullUrl

class Env {
    companion object {
        private const val BackendDomain = "www.boat-tracker.com"
        val baseUrl = FullUrl.https(BackendDomain, "")
        val socketsUrl = FullUrl.wss(BackendDomain, "/ws/updates")
    }
}
