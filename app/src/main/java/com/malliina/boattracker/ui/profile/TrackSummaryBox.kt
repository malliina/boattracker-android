package com.malliina.boattracker.ui.profile

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.malliina.boattracker.R
import com.malliina.boattracker.TrackRef
import com.malliina.boattracker.ui.StatBox

enum class LoadState {
    NotLoaded, Loading, Loaded
}

class TrackSummaryBox: ConstraintLayout {
    private lateinit var duration: StatBox
    private lateinit var distance: StatBox
    private lateinit var topSpeed: StatBox
    private lateinit var avgSpeed: StatBox
    private lateinit var waterTemp: StatBox
    private lateinit var date: StatBox
    private var state: LoadState = LoadState.NotLoaded

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
        View.inflate(context, R.layout.track_summary, this)
        duration = findViewById(R.id.duration)
        distance = findViewById(R.id.distance)
        topSpeed = findViewById(R.id.topSpeed)
        avgSpeed = findViewById(R.id.avgSpeed)
        waterTemp = findViewById(R.id.waterTemp)
        date = findViewById(R.id.date)
    }

    fun fill(track: TrackRef) {
        val na = context.getString(R.string.na)
        fillStat(duration, R.string.duration, track.duration.formatted())
        fillStat(distance, R.string.distance, track.distance.formatted())
        fillStat(topSpeed, R.string.topSpeed, track.topSpeed?.formatted() ?: na)
        fillStat(avgSpeed, R.string.avgSpeed, track.avgSpeed?.formatted() ?: na)
        fillStat(waterTemp, R.string.waterTemp, track.avgWaterTemp?.formatted() ?: na)
        fillStat(date, R.string.date, track.formatStart())
        state = LoadState.Loaded
    }

    private fun fillStat(box: StatBox, labelRes: Int, value: String) {
        box.fill(context.getString(labelRes), value)
    }
}
