package com.malliina.boattracker.ui.callouts

import android.content.Context
import android.util.AttributeSet
import com.malliina.boattracker.R

class TrophyCallout : BoatCallout {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    fun fill(info: TopSpeedInfo) {
        fillText(R.id.trophy_speed_text, info.speed.formatted())
        fillText(R.id.trophy_datetime_text, info.dateTime)
    }
}