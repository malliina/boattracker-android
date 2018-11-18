package com.malliina.boattracker

data class Email(val email: String) {
    override fun toString(): String {
        return email
    }
}

data class IdToken(val token: String) {
    override fun toString(): String {
        return token
    }
}

data class UserInfo(val email: Email, val idToken: IdToken)

data class TrackName(val name: String) {
    override fun toString(): String {
        return name
    }
}
