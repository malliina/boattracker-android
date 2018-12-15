package com.malliina.boattracker.ui

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.malliina.boattracker.R

class LabeledStat: ConstraintLayout {
    lateinit var label: TextView
    lateinit var value: TextView

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
        View.inflate(context, R.layout.labeled_stat, this)
        label = findViewById(R.id.stat_label)
        value = findViewById(R.id.stat_value)
    }

    fun fill(label: String, value: String) {
        this.label.text = label
        this.value.text = value
    }
}
