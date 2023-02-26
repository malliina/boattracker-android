package com.malliina.boattracker.ui.map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.malliina.boattracker.BuildConfig
import com.malliina.boattracker.databinding.MapFragmentContainerBinding
import com.malliina.boattracker.ui.ComposeFragment
import com.malliina.boattracker.ui.Margins
import com.malliina.boattracker.ui.callouts.PopupContent
import com.malliina.boattracker.ui.callouts.PopupView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import timber.log.Timber

@Composable
fun HomeMapView(viewModel: MapViewModel) {
    ComposeMapView(viewModel)
}

class WrappedMap : ComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ComposeMapFragment()
            }
        }
    }
}

@Composable
fun ComposeMapFragment() {
    AndroidViewBinding(MapFragmentContainerBinding::inflate)
}

@Composable
fun ComposeMapView(viewModel: MapViewModel) {
    val ctx = LocalContext.current
    val conf by viewModel.conf.observeAsState()
    val helsinki = Point.fromLngLat(24.9, 60.14)
    val map = rememberMap(helsinki)
    val annotations = remember {
        map.viewAnnotationManager
    }
    var popupView: View? by remember { mutableStateOf(null) }
//    LaunchedEffect(conf) {
//        conf?.let { c ->
//            val uri = c.map.styleUrl
//            Timber.i("Loading $uri...")
//            map?.getMapboxMap()?.loadStyleUri(c.map.styleUrl) { style ->
//
//            }
//        }
//    }
    val options = viewAnnotationOptions {
        geometry(helsinki)
        anchor(ViewAnnotationAnchor.BOTTOM)
    }
    Box {
        AndroidView(
            factory = { context ->
                map
            },
            update = { mapView ->
//                annotations.addViewAnnotation(previewPopup(ctx), options)
                Timber.i("Map update")
            }
        )
        Button(onClick = {
            annotations.addViewAnnotation(previewPopup(ctx), options)
        }, Modifier.padding(Margins.large)) {
            Text("Settings")
        }
    }
}

@Composable
fun rememberMap(point: Point): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(
            context,
            MapInitOptions(
                context,
                ResourceOptions.Builder().accessToken(BuildConfig.MapboxAccessToken).build(),
                cameraOptions = CameraOptions.Builder().center(point).zoom(10.0).build()
            )
        ).apply {
            getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        }
    }
    return mapView
}

fun previewPopup(ctx: Context): ComposeView =
    ComposeView(ctx).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            PopupView(PopupContent.preview) {
                // annotations.removeAllViewAnnotations()
            }
        }
        val layout = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams = FrameLayout.LayoutParams(layout)
    }
