package com.malliina.boattracker.ui.callouts

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.mapbox.mapboxsdk.annotations.BubbleLayout

abstract class BoatCallout : BubbleLayout {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    protected fun fillText(id: Int, value: String) {
        findViewById<TextView>(id).text = value
    }

    protected fun fillOrHide(label: Int, text: Int, labelValue: String, textValue: String?) {
        val visibility = if (textValue == null) View.GONE else View.VISIBLE
        val labelView = findViewById<TextView>(label)
        val textView = findViewById<TextView>(text)
        labelView.visibility = visibility
        textView.visibility = visibility
        textValue?.let { value ->
            labelView.text = labelValue
            textView.text = value
        }
    }
}
