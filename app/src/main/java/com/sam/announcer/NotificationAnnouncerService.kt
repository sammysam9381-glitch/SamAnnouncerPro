package com.sam.announcer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class NotificationAnnouncerService : NotificationListenerService(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private val lastSpoken = HashMap<String, Long>()

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.ENGLISH
            ttsReady = true
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        keepAlive()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            requestRebind(ComponentName(this, NotificationAnnouncerService::class.java))
        } catch (e: Exception) {
        }
    }

    private fun keepAlive() {
        try {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel("keepalive", "Sam Announcer", NotificationManager.IMPORTANCE_LOW)
            )
            val n = Notification.Builder(this, "keepalive")
                .setContentTitle("Sam Announcer chal raha hai")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build()
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(1, n)
            }
        } catch (e: Exception) {
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val prefs = Prefs(this)
        if (!prefs.enabled || !ttsReady) return
        if (sbn.packageName == packageName) return
        if (sbn.isOngoing) return
        if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

        val now = System.currentTimeMillis()
        val last = lastSpoken[sbn.packageName] ?: 0L
        if (now - last < 3000) return
        lastSpoken[sbn.packageName] = now

        tts?.speak(
            "${prefs.userName}, you have a message from ${appName(sbn.packageName)}",
            TextToSpeech.QUEUE_ADD, null, sbn.key
        )
    }

    private fun appName(pkg: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        pkg
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
