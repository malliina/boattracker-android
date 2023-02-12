package com.malliina.boattracker.ui.attributions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.malliina.boattracker.AppAttribution
import com.malliina.boattracker.FullUrl
import com.malliina.boattracker.Link
import com.malliina.boattracker.ui.Margins
import com.malliina.boattracker.ui.theme.MapboxBlue

@Composable
fun AttributionsView(attributions: List<AppAttribution>) {
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        attributions.forEachIndexed { index, attribution ->
            val links = attribution.links
            Text(attribution.title, fontSize = 18.sp, modifier = Modifier.padding(Margins.s))
            attribution.text?.let {
                Text(it, modifier = Modifier.padding(Margins.s))
            }
            links.forEachIndexed { linkIndex, link ->
                val tag = "URL-$linkIndex"
                val urlStr = link.url.url
                val annotatedString = buildAnnotatedString {
                    pushStyle(
                        style = SpanStyle(
                            color = MapboxBlue,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    append(urlStr)
                    addStringAnnotation(
                        tag = tag,
                        annotation = urlStr,
                        start = 0,
                        end = urlStr.length
                    )
                }
                ClickableText(annotatedString, Modifier.padding(Margins.s)) { offset ->
                    annotatedString.getStringAnnotations(tag = tag, start = offset, end = offset).firstOrNull()?.let { annotation ->
                        uriHandler.openUri(annotation.item)
                    }
                }
            }
            if (index < attributions.lastIndex) {
                Divider()
            }
        }
    }
}

@Preview
@Composable
fun AttributionsPreview() {
    val attribution = AppAttribution("Sjökortsmaterial", "Källa: Trafikverket", listOf(Link("Länk", FullUrl.https("google.com", ""))))
    AttributionsView(attributions = listOf(attribution))
}
