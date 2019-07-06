package com.malliina.boattracker.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.malliina.boattracker.R

class StatBox: ConstraintLayout {
    lateinit var label: TextView
    lateinit var valueView: TextView

    constructor(ctx: Context): super(ctx) {
        init()
    }
    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) {
        init()
    }
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.stat_box, this)
        label = findViewById(R.id.stat_label)
        valueView = findViewById(R.id.stat_value)
    }

    fun label(labelValue: String) {
        this.label.text = labelValue
    }

    var value: String
        get() = valueView.text.toString()
        set(newValue) { this.valueView.text = newValue }

    fun fill(label: String, value: String) {
        this.label.text = label
        this.valueView.text = value
    }
}
