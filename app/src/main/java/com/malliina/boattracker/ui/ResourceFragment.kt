package com.malliina.boattracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.Lang
import com.malliina.boattracker.UserInfo
import com.malliina.boattracker.UserSettings

abstract class ResourceFragment(private val layoutResource: Int): Fragment() {
    protected val settings: UserSettings get() = UserSettings.instance
    protected val token: IdToken? get() = settings.token
    protected val lang: Lang get() = settings.lang!!
    protected val user: UserInfo get() = settings.user!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutResource, container, false)
    }
}
