package com.malliina.boattracker.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.fragment.app.Fragment
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.malliina.boattracker.BoatApp
import com.malliina.boattracker.Lang
import com.malliina.boattracker.R

class ProfileFrag : Fragment() {
    val app: BoatApp get() = requireActivity().application as BoatApp
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val l = app.settings.lang!!
        val lang = ProfileLinkLang(l.track.tracks, l.labels.statistics, l.track.boats)
        return ComposeView(requireContext()).apply {
            setContent {
                ProfileContent(
                    lang,
                    onNavigate = { dest -> findNavController().navigate(dest) })
            }
        }
    }
}

data class ProfileLinkLang(val tracks: String, val statistics: String, val boats: String)

@Composable
fun ProfileContent(lang: ProfileLinkLang, onNavigate: (NavDirections) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        val buttonModifier = Modifier.fillMaxWidth()
        val textModifier = Modifier.padding(16.dp, 4.dp)
        CTrackSummary()
        Button(
            onClick = { onNavigate(ProfileFragDirections.profile2ToTracks(lang.tracks)) },
            modifier = buttonModifier
        ) {
            Text("Tracks", textModifier)
        }
        Button(
            onClick = { onNavigate(ProfileFragDirections.profile2ToStatistics(lang.statistics)) },
            modifier = buttonModifier
        ) {
            Text("Statistics", textModifier)
        }
        Button(
            onClick = { onNavigate(ProfileFragDirections.profile2ToBoats(lang.boats)) },
            modifier = buttonModifier
        ) {
            Text("Boats", textModifier)
        }
    }
}

@Composable
fun CTrackSummary() {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CStatBox(label = "Distance", value = "21.29 km")
            CStatBox(label = "Duration", value = "13:04")
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CStatBox(label = "Distance", value = "21.29 km")
            CStatBox(label = "Duration", value = "13:04")
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CStatBox(label = "Distance", value = "21.29 km")
            CStatBox(label = "Duration", value = "13:04")
        }
    }
}

@Composable
fun CStatBox(label: String, value: String) {
    Column(
        Modifier
            .padding(8.dp)
    ) {
        val textModifier = Modifier
            .padding(8.dp, 4.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle1,
            modifier = textModifier,
            color = Color(R.color.darkGray),
            fontSize = 12.sp
        )
        Text(
            text = value,
            modifier = textModifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ProfileContent(
        ProfileLinkLang("Tracks", "Statistics", "Boats"),
        onNavigate = { dest -> println("Navigate") })
}
