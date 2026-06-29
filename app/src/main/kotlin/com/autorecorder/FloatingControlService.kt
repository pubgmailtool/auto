package com.autorecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

class FloatingControlService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        buildFloatingView()
        addFloatingView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            runCatching { windowManager.removeView(floatingView) }
        }
    }

    private fun startForegroundNotification() {
        val channelId = "floating_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "悬浮控制面板", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("自动录制运行中")
                .setContentText("悬浮控制面板已启动")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("自动录制运行中")
                .setContentText("悬浮控制面板已启动")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        }
        startForeground(1, notification)
    }

    private fun buildFloatingView() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#DD1A237E"))
            setPadding(10, 6, 10, 6)
        }

        fun makeBtn(label: String, bgColor: Int, onClick: () -> Unit) =
            Button(this).apply {
                text = label
                textSize = 11f
                setTextColor(Color.WHITE)
                setBackgroundColor(bgColor)
                setPadding(16, 8, 16, 8)
                setOnClickListener { onClick() }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.setMargins(4, 0, 4, 0) }
            }

        layout.addView(makeBtn("🔴录制", Color.parseColor("#C62828")) { onRecord() })
        layout.addView(makeBtn("⏹停止", Color.parseColor("#424242")) { onStop() })
        layout.addView(makeBtn("▶回放", Color.parseColor("#1B5E20")) { onPlay() })
        layout.addView(makeBtn("🏠桌面", Color.parseColor("#0D47A1")) { onHome() })

        // 拖动
        var initX = 0; var initY = 0
        var initTouchX = 0f; var initTouchY = 0f
        var moved = false

        layout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initX = params.x; initY = params.y
                    initTouchX = event.rawX; initTouchY = event.rawY
                    moved = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initTouchX).toInt()
                    val dy = (event.rawY - initTouchY).toInt()
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                        moved = true
                        params.x = initX + dx
                        params.y = initY + dy
                        windowManager.updateViewLayout(floatingView, params)
                    }
                    true
                }
                else -> false
            }
        }

        floatingView = layout
    }

    private fun addFloatingView() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0; y = 150
        }
        windowManager.addView(floatingView, params)
    }

    private fun onRecord() {
        val svc = AutoAccessibilityService.instance
            ?: return toast("请先开启无障碍服务")
        if (svc.isPlaying) return toast("回放中，请先停止")
        svc.startRecording()
        toast("🔴 开始录制")
    }

    private fun onStop() {
        val svc = AutoAccessibilityService.instance
            ?: return toast("无障碍服务未运行")
        when {
            svc.isRecording -> { svc.stopRecording(); toast("⏹ 录制已保存") }
            svc.isPlaying   -> { svc.stopPlayback();  toast("⏹ 回放已停止") }
            else            -> toast("当前没有进行中的操作")
        }
    }

    private fun onPlay() {
        val svc = AutoAccessibilityService.instance
            ?: return toast("请先开启无障碍服务")
        if (svc.isRecording) return toast("录制中，请先停止")
        svc.startPlayback()
        toast("▶ 开始回放")
    }

    private fun onHome() {
        val svc = AutoAccessibilityService.instance ?: return toast("请先开启无障碍服务")
        if (svc.isRecording) {
            svc.recordHome()
            toast("🏠 已插入回桌面动作")
        } else {
            svc.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
