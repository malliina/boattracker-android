package com.malliina.boattracker.ui.language

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.malliina.boattracker.Lang
import com.malliina.boattracker.Language
import com.malliina.boattracker.ui.Margins

@Composable
fun LanguagesView(lang: Lang, vm: LanguagesInterface) {
    val selected = vm.language.observeAsState()
    val profileLang = lang.profile
    val items = listOf(
        LanguageItem(Language.Swedish, profileLang.swedish),
        LanguageItem(Language.Finnish, profileLang.finnish),
        LanguageItem(Language.English, profileLang.english)
    )
    Column(
        Modifier
            .fillMaxWidth()
            .padding(Margins.s),
        verticalArrangement = Arrangement.spacedBy(Margins.s)
    ) {
        items.forEachIndexed { index, item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(selected = item.language == selected.value) {
                        vm.changeLanguage(item.language)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.title)
                RadioButton(
                    selected = item.language == selected.value,
                    onClick = { vm.changeLanguage(item.language) }
                )
            }
            if (index < items.lastIndex) {
                Divider()
            }
        }
    }
}

@Preview
@Composable
fun LanguagesPreview() {
    Text("Hi")
}
