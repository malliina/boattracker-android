package com.malliina.boattracker

import com.malliina.boattracker.ui.map.UserTrack

class UserState {
    companion object {
        val instance = UserState()
    }

    var userTrack: UserTrack? = null
    val user: UserInfo? get() = userTrack?.user
    val token: IdToken? get() = user?.idToken

    fun update(user: UserInfo) {
        userTrack = UserTrack(user, userTrack?.track)
    }

    fun clear() {
        userTrack = null
    }
}
