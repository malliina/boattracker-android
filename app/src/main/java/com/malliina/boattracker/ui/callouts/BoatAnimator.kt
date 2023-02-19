package com.malliina.boattracker.ui.callouts

import android.animation.ValueAnimator
import com.mapbox.maps.MapboxMap

// Mostly copied and converted from mapbox-android-demo
class BoatAnimator(val map: MapboxMap) {
    private val cameraAnimationTime: Long = 1950

//    fun createLatLngAnimator(
//        currentPosition: Point,
//        targetPosition: Point
//    ): Animator? {
//        val latLngAnimator =
//            ValueAnimator.ofObject(LatLngEvaluator(), currentPosition, targetPosition)
//        latLngAnimator.duration = cameraAnimationTime
//        latLngAnimator.interpolator = FastOutSlowInInterpolator()
//        latLngAnimator.addUpdateListener { animation ->
//            map.moveCamera(
//                CameraUpdateFactory.newLatLng(animation.animatedValue as LatLng)
//            )
//        }
//        return latLngAnimator
//    }

//    fun createZoomAnimator(
//        currentZoom: Double,
//        targetZoom: Double
//    ): Animator? {
//        val zoomAnimator =
//            ValueAnimator.ofFloat(currentZoom.toFloat(), targetZoom.toFloat())
//        zoomAnimator.duration = cameraAnimationTime
//        zoomAnimator.interpolator = FastOutSlowInInterpolator()
//        zoomAnimator.addUpdateListener { animation ->
//            map.moveCamera(CameraUpdateFactory.zoomTo(animation.valueDouble()))
//        }
//        return zoomAnimator
//    }

//    fun createBearingAnimator(
//        currentBearing: Double,
//        targetBearing: Double
//    ): Animator? {
//        val bearingAnimator =
//            ValueAnimator.ofFloat(currentBearing.toFloat(), targetBearing.toFloat())
//        bearingAnimator.duration = cameraAnimationTime
//        bearingAnimator.interpolator = FastOutSlowInInterpolator()
//        bearingAnimator.addUpdateListener { animation ->
//            map.moveCamera(CameraUpdateFactory.bearingTo(animation.valueDouble()))
//        }
//        return bearingAnimator
//    }

//    fun createTiltAnimator(
//        currentTilt: Double,
//        targetTilt: Double
//    ): Animator? {
//        val tiltAnimator =
//            ValueAnimator.ofFloat(currentTilt.toFloat(), targetTilt.toFloat())
//        tiltAnimator.duration = cameraAnimationTime
//        tiltAnimator.interpolator = FastOutSlowInInterpolator()
//        tiltAnimator.addUpdateListener { animation ->
//            map.moveCamera(CameraUpdateFactory.tiltTo(animation.valueDouble()))
//        }
//        return tiltAnimator
//    }

//    private class LatLngEvaluator : TypeEvaluator<LatLng> {
//        private val latLng = LatLng()
//        override fun evaluate(
//            fraction: Float,
//            startValue: LatLng,
//            endValue: LatLng
//        ): LatLng {
//            latLng.latitude = (
//                startValue.latitude +
//                    (endValue.latitude - startValue.latitude) * fraction
//                )
//            latLng.longitude = (
//                startValue.longitude +
//                    (endValue.longitude - startValue.longitude) * fraction
//                )
//            return latLng
//        }
//    }
}

fun ValueAnimator.valueDouble() = (this.animatedValue as Float).toDouble()
