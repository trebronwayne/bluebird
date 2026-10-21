package io.github.norbertweb.bluebird.i18n

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Bluebird language catalog. Resource qualifiers map to Android locale tags.
 * Acholi uses the BCP-47 resource qualifier b+ach.
 */
data class BluebirdLanguage(val tag: String, val nativeName: String, val englishName: String)

object BluebirdLanguages {
    val all: List<BluebirdLanguage> = listOf(
    BluebirdLanguage("en", "English", "English"),
    BluebirdLanguage("lg", "Luganda", "Luganda"),
    BluebirdLanguage("ach", "Acholi", "Acholi"),
    BluebirdLanguage("sw", "Kiswahili", "Kiswahili"),
    BluebirdLanguage("fr", "Français", "French"),
    BluebirdLanguage("pt", "Português", "Portuguese"),
    BluebirdLanguage("am", "አማርኛ", "Amharic"),
    BluebirdLanguage("rw", "Kinyarwanda", "Kinyarwanda"),
    BluebirdLanguage("so", "Soomaali", "Somali"),
    BluebirdLanguage("ha", "Hausa", "Hausa"),
    BluebirdLanguage("es", "Español", "Spanish"),
    BluebirdLanguage("de", "Deutsch", "German"),
    BluebirdLanguage("tr", "Türkçe", "Turkish"),
    BluebirdLanguage("it", "Italiano", "Italian"),
    BluebirdLanguage("nl", "Nederlands", "Dutch"),
    BluebirdLanguage("pl", "Polski", "Polish"),
    BluebirdLanguage("ru", "Русский", "Russian"),
    BluebirdLanguage("uk", "Українська", "Ukrainian"),
    BluebirdLanguage("zh-CN", "简体中文", "Chinese (Simplified)"),
    BluebirdLanguage("ja", "日本語", "Japanese"),
    BluebirdLanguage("ko", "한국어", "Korean"),
    BluebirdLanguage("hi", "हिन्दी", "Hindi"),
    BluebirdLanguage("bn", "বাংলা", "Bengali"),
    BluebirdLanguage("ur", "اردو", "Urdu"),
    BluebirdLanguage("ar", "العربية", "Arabic")
    )

    const val PREFS = "bluebird_language"
    const val KEY_LANGUAGE_TAG = "language_tag"

    fun selectedTag(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE_TAG, null)

    fun saveSelection(context: Context, tag: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            if (tag.isNullOrBlank()) remove(KEY_LANGUAGE_TAG)
            else putString(KEY_LANGUAGE_TAG, tag)
        }.apply()
    }

    fun wrapContext(base: Context): Context {
        val tag = selectedTag(base) ?: return base
        val locale = Locale.forLanguageTag(tag)
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return base.createConfigurationContext(configuration)
    }
}
