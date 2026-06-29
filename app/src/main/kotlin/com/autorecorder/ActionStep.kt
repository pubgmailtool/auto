package com.autorecorder

import org.json.JSONArray
import org.json.JSONObject

data class ActionStep(
    val type: String,
    val x: Float? = null,
    val y: Float? = null,
    val delay: Long = 0L
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("type", type)
        x?.let { put("x", it) }
        y?.let { put("y", it) }
        put("delay", delay)
    }

    companion object {
        fun fromJson(obj: JSONObject) = ActionStep(
            type  = obj.getString("type"),
            x     = if (obj.has("x")) obj.getDouble("x").toFloat() else null,
            y     = if (obj.has("y")) obj.getDouble("y").toFloat() else null,
            delay = if (obj.has("delay")) obj.getLong("delay") else 0L
        )
    }
}

fun List<ActionStep>.toJsonString(): String =
    JSONArray().also { arr -> forEach { arr.put(it.toJson()) } }.toString()

fun jsonStringToSteps(json: String): List<ActionStep> {
    val arr = JSONArray(json)
    return (0 until arr.length()).map { ActionStep.fromJson(arr.getJSONObject(it)) }
}
