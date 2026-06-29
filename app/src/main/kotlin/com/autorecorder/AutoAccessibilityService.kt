package com.autorecorder

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.*

class AutoAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutoAccessibilityService? = null
    }

    var isRecording = false
        private set
    var isPlaying = false
        private set

    private val recordedSteps = mutableListOf<ActionStep>()
    private var lastActionTime = 0L
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isRecording || isPlaying) return
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val node = event.source ?: return
            val rect = Rect()
            node.getBoundsInScreen(rect)
            node.recycle()

            val now = System.currentTimeMillis()
            val delay = if (lastActionTime == 0L) 0L else now - lastActionTime
            recordedSteps.add(ActionStep(
                type = "click",
                x = rect.centerX().toFloat(),
                y = rect.centerY().toFloat(),
                delay = delay
            ))
            lastActionTime = now
        }
    }

    fun startRecording() {
        recordedSteps.clear()
        lastActionTime = 0L
        isRecording = true
    }

    fun stopRecording() {
        isRecording = false
        ScriptStore.save(this, recordedSteps.toList())
    }

    fun recordHome() {
        if (!isRecording) return
        val now = System.currentTimeMillis()
        val delay = if (lastActionTime == 0L) 0L else now - lastActionTime
        recordedSteps.add(ActionStep(type = "home", delay = delay))
        lastActionTime = now
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun startPlayback() {
        if (isPlaying) return
        val steps = ScriptStore.load(this)
        if (steps.isEmpty()) return
        isPlaying = true
        serviceScope.launch {
            play(steps)
            isPlaying = false
        }
    }

    fun stopPlayback() {
        isPlaying = false
        serviceScope.coroutineContext.cancelChildren()
    }

    private suspend fun play(steps: List<ActionStep>) {
        for (step in steps) {
            if (!isPlaying) break
            if (step.delay > 0) delay(step.delay)
            when (step.type) {
                "click" -> {
                    val x = step.x ?: continue
                    val y = step.y ?: continue
                    performClick(x, y)
                }
                "home" -> performGlobalAction(GLOBAL_ACTION_HOME)
            }
            delay(200)
        }
    }

    private fun performClick(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 80L))
            .build()
        dispatchGesture(gesture, null, null)
    }
}
