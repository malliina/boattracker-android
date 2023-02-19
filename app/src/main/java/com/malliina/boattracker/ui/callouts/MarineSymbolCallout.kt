package com.malliina.boattracker.ui.callouts

// class MarineSymbolCallout : BoatCallout {
//    constructor(ctx: Context) : super(ctx)
//    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
//    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
//        ctx,
//        attrs,
//        defStyleAttr
//    )
//
//    fun fill(data: MarineSymbol, l: Lang) {
//        val lang = l.mark
//        fillText(R.id.mark_name_text, data.name(l.language)?.value ?: "")
//        fillText(R.id.mark_type_label, lang.aidType)
//        fillText(R.id.mark_type_text, data.aidType.translate(lang.aidTypes))
//
//        fillOrHide(
//            R.id.mark_construction_label,
//            R.id.mark_construction_text,
//            lang.construction,
//            data.construction?.translate(lang.structures)
//        )
//        fillOrHide(
//            R.id.mark_nav_label,
//            R.id.mark_nav_text,
//            lang.navigation,
//            if (data.navMark.isKnown()) data.navMark.translate(lang.navTypes) else null
//        )
//        fillText(R.id.mark_nav_label, lang.navigation)
//        fillText(R.id.mark_nav_text, data.navMark.translate(lang.navTypes))
//        fillOrHide(
//            R.id.mark_location_label,
//            R.id.mark_location_text,
//            lang.location,
//            data.location(l.language)?.value
//        )
//        fillText(R.id.mark_owner_label, lang.owner)
//        fillText(R.id.mark_owner_text, data.owner)
//    }
// }
