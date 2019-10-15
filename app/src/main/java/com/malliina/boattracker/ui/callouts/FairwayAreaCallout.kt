package com.malliina.boattracker.ui.callouts

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.malliina.boattracker.Lang
import com.malliina.boattracker.LimitLang
import com.malliina.boattracker.R
import timber.log.Timber

class FairwayAreaCallout : BoatCallout {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    fun fill(data: FairwayArea, limits: LimitArea?, lang: Lang) {
        val fairwayLang = lang.fairway
        fillText(R.id.fairway_area_owner_text, data.owner.value)
        fillText(R.id.fairway_area_type_label, fairwayLang.fairwayType)
        fillText(R.id.fairway_area_type_text, data.fairwayType.translate(fairwayLang.types))
        fillText(R.id.fairway_area_depth_label, fairwayLang.fairwayDepth)
        fillText(R.id.fairway_area_depth_text, data.fairwayDepth.formatMeters())
        fillText(R.id.fairway_area_harrow_depth_label, fairwayLang.harrowDepth)
        fillText(R.id.fairway_area_harrow_depth_text, data.harrowDepth.formatMeters())
        val limitLang = lang.limits
        val limitsVisibility = if (limits == null) View.GONE else View.VISIBLE
        listOf(
            R.id.fairway_area_limits_label,
            R.id.fairway_area_limits_text,
            R.id.fairway_area_speed_limit_label,
            R.id.fairway_area_speed_limit_text,
            R.id.fairway_area_limit_name_label,
            R.id.fairway_area_limit_name_text
        ).forEach { id ->
            findViewById<View>(id).visibility = limitsVisibility
        }
        limits?.let { l ->
            fillText(R.id.fairway_area_limits_label, limitLang.limit)
            fillText(
                R.id.fairway_area_limits_text,
                l.types.joinToString { it.translate(limitLang.types) })
            fillOrHide(
                R.id.fairway_area_speed_limit_label,
                R.id.fairway_area_speed_limit_text,
                limitLang.magnitude,
                l.limit?.formatKmhInt()
            )
            fillText(R.id.fairway_area_limit_name_label, limitLang.fairwayName)
            fillText(R.id.fairway_area_limit_name_text, l.fairwayName)
        }

    }
}

class FairwayLimitCallout : BoatCallout {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    fun fill(data: LimitArea, lang: LimitLang) {
        fillText(R.id.fairway_limits_label, lang.limit)
        fillText(R.id.fairway_limits_text, data.types.joinToString { it.translate(lang.types) })
        fillOrHide(
            R.id.fairway_speed_limit_label,
            R.id.fairway_speed_limit_text,
            lang.magnitude,
            data.limit?.formatKmhInt()
        )
        fillText(R.id.fairway_limit_name_label, lang.fairwayName)
        fillText(R.id.fairway_limit_name_text, data.fairwayName)
    }
}
