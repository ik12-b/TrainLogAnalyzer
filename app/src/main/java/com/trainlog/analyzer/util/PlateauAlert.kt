package com.trainlog.analyzer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object PlateauAlert {
    private const val CHANNEL_ID = "trainlog_plateau"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Plateau alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifikasi saat relative improvement di bawah ambang" }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(ch)
        }
    }

    fun notifyIfNeeded(
        context: Context,
        runName: String,
        isPlateau: Boolean,
        relPct: Double,
        threshold: Double
    ) {
        if (!isPlateau) return
        ensureChannel(context)
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Plateau: $runName")
            .setContentText("Rel. improvement ${"%.2f".format(relPct)}% < ambang ${"%.2f".format(threshold)}%")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(runName.hashCode(), notif)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS may be denied on API 33+
        }
    }
}
