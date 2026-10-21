package io.github.norbertweb.bluebird.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.norbertweb.bluebird.R
import io.github.norbertweb.bluebird.i18n.BluebirdLanguage
import io.github.norbertweb.bluebird.i18n.BluebirdLanguages

@Composable
internal fun LanguageSettings(a: ScreenArgs) {
    val context = LocalContext.current
    val savedTag = BluebirdLanguages.selectedTag(context)
    var pendingTag by remember(savedTag) { mutableStateOf(savedTag ?: "system") }
    var searchQuery by remember { mutableStateOf("") }
    val languages = BluebirdLanguages.all.filter {
        it.nativeName.contains(searchQuery, ignoreCase = true) ||
            it.englishName.contains(searchQuery, ignoreCase = true) ||
            it.tag.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.language_choose),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = a.textColor
        )
        Text(
            text = stringResource(R.string.language_restart_notice),
            style = MaterialTheme.typography.bodyMedium,
            color = a.textColor.copy(alpha = 0.72f)
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(stringResource(R.string.language_search)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Card(colors = CardDefaults.cardColors(containerColor = a.surfaceBg)) {
            LanguageChoice(
                title = stringResource(R.string.language_system_default),
                subtitle = stringResource(R.string.language_system_subtitle),
                selected = pendingTag == "system",
                onClick = { pendingTag = "system" },
                textColor = a.textColor
            )
        }
        Text(
            text = stringResource(R.string.language_results_count, languages.size),
            style = MaterialTheme.typography.labelMedium,
            color = a.textColor.copy(alpha = 0.68f)
        )
        if (languages.isEmpty()) {
            Text(
                text = stringResource(R.string.language_no_results),
                style = MaterialTheme.typography.bodyMedium,
                color = a.textColor.copy(alpha = 0.72f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        languages.forEach { language ->
            Card(colors = CardDefaults.cardColors(containerColor = a.surfaceBg)) {
                LanguageChoice(
                    title = language.nativeName,
                    subtitle = language.englishName,
                    selected = pendingTag == language.tag,
                    onClick = { pendingTag = language.tag },
                    textColor = a.textColor
                )
            }
        }
        Button(
            onClick = {
                BluebirdLanguages.saveSelection(context, pendingTag.takeUnless { it == "system" })
                (context.findActivity())?.recreate()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.language_apply))
        }
    }
}

@Composable
private fun LanguageChoice(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = textColor, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
            Text(subtitle, color = textColor.copy(alpha = 0.68f), style = MaterialTheme.typography.bodySmall)
        }
        RadioButton(selected = selected, onClick = onClick)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
