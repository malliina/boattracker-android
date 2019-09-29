package com.malliina.boattracker.ui.profile

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.malliina.boattracker.MessagesLang
import com.malliina.boattracker.R
import com.malliina.boattracker.TrackLang
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
//        initLabel(duration, R.string.duration)
        distance = findViewById(R.id.distance)
//        initLabel(distance, R.string.distance)
        topSpeed = findViewById(R.id.topSpeed)
//        initLabel(topSpeed, R.string.topSpeed)
        avgSpeed = findViewById(R.id.avgSpeed)
//        initLabel(avgSpeed, R.string.avgSpeed)
        waterTemp = findViewById(R.id.waterTemp)
//        initLabel(waterTemp, R.string.waterTemp)
        date = findViewById(R.id.date)
//        initLabel(date, R.string.date)
    }

//    private fun initLabel(box: StatBox, labelRes: Int) {
//        box.label = context.getString(labelRes)
//    }

    fun fillLabels(trackLang: TrackLang) {
        duration.label = trackLang.duration
        distance.label = trackLang.distance
        topSpeed.label = trackLang.topSpeed
        avgSpeed.label = trackLang.avgSpeed
        waterTemp.label = trackLang.waterTemp
        date.label = trackLang.date
    }

    fun fill(track: TrackRef, messages: MessagesLang) {
        val na = messages.notAvailable
        duration.value = track.duration.formatted()
        distance.value = track.distanceMeters.formatted()
        topSpeed.value = track.topSpeed?.formatted() ?: na
        avgSpeed.value = track.avgSpeed?.formatted() ?: na
        waterTemp.value = track.avgWaterTemp?.formatted() ?: na
        date.value = track.times.start.date
        state = LoadState.Loaded
    }
}
