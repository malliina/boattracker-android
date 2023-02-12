package com.malliina.boattracker.ui.map

import android.graphics.Color
import com.malliina.boattracker.Speed
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*

class Styles {
    companion object {
        val instance = Styles()
    }

    val trackColor: Expression = interpolate(
        linear(),
        get(Speed.key),
        stop(5, color(Color.rgb(0, 255, 150))),
        stop(10, color(Color.rgb(50, 150, 50))),
        stop(15, color(Color.rgb(100, 255, 50))),
        stop(20, color(Color.rgb(255, 255, 0))),
        stop(25, color(Color.rgb(255, 213, 0))),
        stop(28, color(Color.rgb(255, 191, 0))),
        stop(30, color(Color.rgb(255, 170, 0))),
        stop(32, color(Color.rgb(255, 150, 0))),
        stop(33, color(Color.rgb(255, 140, 0))),
        stop(35, color(Color.rgb(255, 128, 0))),
        stop(37, color(Color.rgb(255, 85, 0))),
        stop(38, color(Color.rgb(255, 42, 0))),
        stop(39, color(Color.rgb(255, 21, 0))),
        stop(40, color(Color.rgb(255, 0, 0)))
    )
}
