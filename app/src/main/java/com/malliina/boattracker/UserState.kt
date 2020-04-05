package com.malliina.boattracker

import com.malliina.boattracker.ui.map.MapState

class UserState {
    companion object {
        val instance = UserState()
    }

    var mapState: MapState? = null
    val user: UserInfo? get() = mapState?.user
    val token: IdToken? get() = user?.idToken

    fun clear() {
        mapState = null
    }
}
