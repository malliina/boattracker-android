package com.malliina.boattracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.malliina.boattracker.*

abstract class ResourceFragment(private val layoutResource: Int) : Fragment() {
    val app: BoatApp get() = requireActivity().application as BoatApp
    protected val userState: UserState get() = UserState.instance
    protected val token: IdToken? get() = userState.token
    protected val lang: Lang get() = app.settings.lang!!
    protected val user: UserInfo get() = userState.user!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutResource, container, false)
    }
}
