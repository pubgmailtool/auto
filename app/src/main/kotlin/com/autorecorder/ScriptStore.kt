package com.autorecorder

import android.content.Context

object ScriptStore {
    private const val PREF_NAME = "auto_recorder_prefs"
    private const val KEY_SCRIPT = "script_json"

    fun save(context: Context, steps: List<ActionStep>) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_SCRIPT, steps.toJsonString()).apply()
    }

    fun load(context: Context): List<ActionStep> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SCRIPT, null) ?: return emptyList()
        return try { jsonStringToSteps(json) } catch (e: Exception) { emptyList() }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_SCRIPT).apply()
    }
}
