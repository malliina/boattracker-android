package com.malliina.boattracker.ui.callouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.malliina.boattracker.ui.Colors
import com.malliina.boattracker.ui.FontSize
import com.malliina.boattracker.ui.Margins

data class InfoItem(val key: String, val value: String)

data class PopupContent(
    val title: String?,
    val items: List<InfoItem>,
    val footer: String?
) {
    companion object {
        fun nonEmpty(title: String?, items: List<InfoItem?>, footer: String?) =
            PopupContent(title, items.filterNotNull(), footer)
        val preview = PopupContent(
            "Trafikverket",
            listOf(InfoItem("Namn", "Porkala route"), InfoItem("Djup", "5.0 m")),
            "Idag"
        )
        val previewLong = PopupContent(
            "Trafikverket",
            listOf(InfoItem("Namn", "Porkala route med en väldigt lång beskrivning som stretchar popupen"), InfoItem("Djup", "5.0 m")),
            "Idag"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopupView(content: PopupContent, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(Margins.normal)) {
        Column(
            Modifier
                .width(IntrinsicSize.Min)
                .background(Color.White)
                .padding(Margins.s),
            verticalArrangement = Arrangement.spacedBy(Margins.s)
        ) {
            content.title?.let { title ->
                Text(
                    title,
                    Modifier
                        .fillMaxWidth(),
                    fontSize = FontSize.large,
                    textAlign = TextAlign.Center
                )
            }
            content.items.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Margins.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.key,
                        Modifier.widthIn(80.dp),
                        color = Colors.darkGray,
                        fontSize = FontSize.small
                    )

                    Text(
                        item.value,
                        Modifier.widthIn(100.dp),
                        fontSize = FontSize.small
                    )
                }
            }
            content.footer?.let { footer ->
                Text(
                    footer,
                    Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
fun PopupPreview() {
    PopupView(PopupContent.preview) {}
}

@Preview
@Composable
fun PopupPreview2() {
    PopupView(PopupContent.previewLong) {}
}
