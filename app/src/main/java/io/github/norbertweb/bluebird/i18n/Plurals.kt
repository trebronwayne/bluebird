package io.github.norbertweb.bluebird.i18n

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Locale-correct plural text. The quantity is passed as the first (and only) format
 * argument, so plural strings use %1$d. Each locale defines the CLDR categories it needs
 * (e.g. Russian/Polish/Ukrainian: one/few/many/other, Arabic: zero/one/two/few/many/other).
 */
@Composable
fun pluralText(@PluralsRes id: Int, count: Number): String {
    val n = count.toInt()
    return LocalContext.current.resources.getQuantityString(id, n, n)
}
