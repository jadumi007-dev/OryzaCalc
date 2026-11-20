package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(RiceNotificationHelper.EXTRA_TITLE)
            ?: "ধান চাষ ক্যালকুলেটর রিমাইন্ডার"
        val body = intent.getStringExtra(RiceNotificationHelper.EXTRA_BODY)
            ?: "আজ আপনার ধানের জমিতে সার বা সেচ ব্যবস্থাপনার নির্ধারিত কাজ রয়েছে।"
        val id = intent.getIntExtra(
            RiceNotificationHelper.EXTRA_NOTIFICATION_ID,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        )
        RiceNotificationHelper.showNotification(context, id, title, body)
    }
}
