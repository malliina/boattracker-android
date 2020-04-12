package com.malliina.boattracker.ui.callouts

import android.content.Context
import android.util.AttributeSet
import com.malliina.boattracker.Lang
import com.malliina.boattracker.R
import com.malliina.boattracker.Vessel

class VesselCallout : BoatCallout {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    fun fill(vessel: Vessel, l: Lang) {
        val lang = l.ais
        fillText(R.id.vessel_name, vessel.name)
        fillOrHide(
            R.id.vessel_destination_label,
            R.id.vessel_destination_text,
            lang.destination,
            vessel.destination
        )
        fillText(R.id.vessel_speed_label, l.track.speed)
        fillText(R.id.vessel_speed_text, vessel.sog.formatKn())
        fillText(R.id.vessel_draft_label, lang.draft)
        fillText(R.id.vessel_draft_text, vessel.draft.formatMeters())
        fillText(R.id.vessel_time, vessel.time.dateTime)
    }
}
