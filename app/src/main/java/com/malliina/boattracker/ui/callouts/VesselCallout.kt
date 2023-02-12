package com.malliina.boattracker.ui.callouts

import android.content.Context
import android.util.AttributeSet
import com.malliina.boattracker.Lang
import com.malliina.boattracker.R
import com.malliina.boattracker.Vessel
import com.malliina.boattracker.ui.VesselInfo

class VesselCallout : BoatCallout {
    private lateinit var info: VesselInfo

    constructor(ctx: Context) : super(ctx) {
        init()
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        init()
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) :
        super(ctx, attrs, defStyleAttr) {
            init()
        }

    private fun init() {
        // This NPEs for some reason, so we do it in fill instead
//        info = findViewById(R.id.vessel_info)
    }

    fun fill(vessel: Vessel, l: Lang) {
        info = findViewById(R.id.vessel_info)
        info.fill(vessel, l)
    }
}
