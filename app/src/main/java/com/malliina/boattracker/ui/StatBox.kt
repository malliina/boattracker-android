package com.malliina.boattracker.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.malliina.boattracker.R

class StatBox : ConstraintLayout {
    private lateinit var labelView: TextView
    private lateinit var valueView: TextView

    constructor(ctx: Context) : super(ctx) {
        init()
    }
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        init()
    }
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.stat_box, this)
        labelView = findViewById(R.id.stat_label)
        valueView = findViewById(R.id.stat_value)
    }

    var label: String
        get() = labelView.text.toString()
        set(newValue) { this.labelView.text = newValue }

    var value: String
        get() = valueView.text.toString()
        set(newValue) { this.valueView.text = newValue }

    fun fill(label: String, value: String) {
        this.label = label
        this.value = value
    }
}
