package com.malliina.boattracker.ui.callouts

import android.content.Context
import android.util.AttributeSet
import com.malliina.boattracker.FairwayLang
import com.malliina.boattracker.R

class FairwayAreaCallout : BoatCallout {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    fun fill(data: FairwayArea, lang: FairwayLang) {
        fillText(R.id.fairway_area_owner_text, data.owner.value)
        fillText(R.id.fairway_area_type_label, lang.fairwayType)
        fillText(R.id.fairway_area_type_text, data.fairwayType.translate(lang.types))
        fillText(R.id.fairway_area_depth_label, lang.fairwayDepth)
        fillText(R.id.fairway_area_depth_text, data.fairwayDepth.formatMeters())
        fillText(R.id.fairway_area_harrow_depth_label, lang.harrowDepth)
        fillText(R.id.fairway_area_harrow_depth_text, data.harrowDepth.formatMeters())
    }

    //    init {
//        inflate(context, R.layout.fairway_area_symbol, this)
//    }
}
