package com.sam.announcer

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("announcer", Context.MODE_PRIVATE)

    var userName: String
        get() = sp.getString("name", "Sam") ?: "Sam"
        set(v) = sp.edit().putString("name", v.trim().ifEmpty { "Sam" }).apply()

    var enabled: Boolean
        get() = sp.getBoolean("enabled", true)
        set(v) = sp.edit().putBoolean("enabled", v).apply()
}
