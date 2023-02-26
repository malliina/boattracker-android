package com.malliina.boattracker.ui.boats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.viewModels
import com.malliina.boattracker.Boat
import com.malliina.boattracker.SettingsLang
import com.malliina.boattracker.ui.ComposeFragment
import com.malliina.boattracker.ui.Margins
import com.malliina.boattracker.ui.tracks.StatBoxView

class ComposeBoats : ComposeFragment() {
    private val viewModel: BoatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BoatsView(viewModel, lang.settings)
        }
    }
}

@Composable
fun BoatsView(vm: BoatsViewModel, lang: SettingsLang) {
    val user by vm.boats.observeAsState()
    var notificationsEnabled by remember { mutableStateOf(vm.notificationsEnabled) }
    Column(Modifier.fillMaxSize().padding(horizontal = Margins.normal), verticalArrangement = Arrangement.spacedBy(Margins.normal)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(lang.notifications)
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { isChecked ->
                    vm.toggleNotifications(isChecked)
                    notificationsEnabled = vm.notificationsEnabled
                }
            )
        }
        Text(lang.notificationsText)
        Column(verticalArrangement = Arrangement.spacedBy(Margins.normal)) {
            (user?.boats ?: emptyList()).forEach { boat ->
                BoatItem(boat, lang)
            }
        }
        Text(lang.tokenText)
    }
}

@Composable
fun BoatItem(content: Boat, lang: SettingsLang) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        StatBoxView(lang.boat, content.name.name)
        StatBoxView(lang.token, content.token.token)
    }
}

@Preview
@Composable
fun BoatsPreview() {
    Text("Hi")
}
