package io.github.vahan16.coinstellations.data

import com.russhwolf.settings.Settings

/**
 * Persisted preferences. Backed by platform-native storage via multiplatform-settings
 * (SharedPreferences / NSUserDefaults / java.util.prefs / localStorage). Falls back to
 * an in-memory store if a platform backend is unavailable.
 */
class AppSettings(private val settings: Settings = Settings()) {

    var apiKey: String?
        get() = settings.getStringOrNull(KEY_API)?.ifBlank { null }
        set(value) {
            if (value.isNullOrBlank()) settings.remove(KEY_API)
            else settings.putString(KEY_API, value.trim())
        }

    var currency: String
        get() = settings.getString(KEY_CURRENCY, "USD")
        set(value) = settings.putString(KEY_CURRENCY, value)

    var bubbleCount: Int
        get() = settings.getInt(KEY_COUNT, 50)
        set(value) = settings.putInt(KEY_COUNT, value)

    val hasApiKey: Boolean get() = apiKey != null

    private companion object {
        const val KEY_API = "coinstats_api_key"
        const val KEY_CURRENCY = "currency"
        const val KEY_COUNT = "bubble_count"
    }
}
