package com.malliina.boattracker.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.malliina.boattracker.Lang
import com.malliina.boattracker.R
import com.malliina.boattracker.Vessel

class VesselInfo : ConstraintLayout {
    private lateinit var nameText: TextView
    private lateinit var destinationLabel: TextView
    private lateinit var destinationText: TextView
    private lateinit var speedLabel: TextView
    private lateinit var speedText: TextView
    private lateinit var draftLabel: TextView
    private lateinit var draftText: TextView
    private lateinit var timeText: TextView

    constructor(ctx: Context) : super(ctx) {
        init()
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        init()
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) :
        super(ctx, attrs, defStyleAttr) {
            init()
        }

    private fun init() {
        View.inflate(context, R.layout.vessel_info, this)
        nameText = findViewById(R.id.vessel_name)
        destinationLabel = findViewById(R.id.vessel_destination_label)
        destinationText = findViewById(R.id.vessel_destination_text)
        speedLabel = findViewById(R.id.vessel_speed_label)
        speedText = findViewById(R.id.vessel_speed_text)
        draftLabel = findViewById(R.id.vessel_draft_label)
        draftText = findViewById(R.id.vessel_draft_text)
        timeText = findViewById(R.id.vessel_time)
    }

    fun fill(vessel: Vessel, l: Lang) {
        val lang = l.ais
        nameText.text = vessel.name
        fillOrHide(destinationLabel, destinationText, lang.destination, vessel.destination)
        speedLabel.text = l.track.speed
        speedText.text = vessel.sog.formatKn()
        draftLabel.text = lang.draft
        draftText.text = vessel.draft.formatMeters()
        timeText.text = vessel.time.dateTime
    }

    private fun fillOrHide(labelView: TextView, textView: TextView, labelValue: String, textValue: String?) {
        val visibility = if (textValue == null) View.GONE else View.VISIBLE
        labelView.visibility = visibility
        textView.visibility = visibility
        textValue?.let { value ->
            labelView.text = labelValue
            textView.text = value
        }
    }
}
