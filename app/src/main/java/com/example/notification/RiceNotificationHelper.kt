package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.entity.ReminderMilestoneEntity
import com.example.domain.calculator.BanglaDate
import java.util.Calendar

object RiceNotificationHelper {

    const val CHANNEL_ID = "rice_farming_reminders"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_BODY = "extra_body"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String
    ) {
        createNotificationChannel(context)
        if (!hasNotificationPermission(context)) return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun scheduleMilestoneAlarms(
        context: Context,
        milestones: List<ReminderMilestoneEntity>
    ) {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nowMillis = System.currentTimeMillis()

        milestones.forEach { milestone ->
            if (milestone.isCompleted) return@forEach

            val dueDate = BanglaDate.fromEpochDay(milestone.dueDateEpochDay)
            val triggerCal = Calendar.getInstance().apply {
                set(dueDate.year, dueDate.month - 1, dueDate.day, 8, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val triggerMillis = triggerCal.timeInMillis
            if (triggerMillis <= nowMillis) return@forEach

            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra(EXTRA_TITLE, milestone.titleBn)
                putExtra(EXTRA_BODY, milestone.descriptionBn)
                putExtra(EXTRA_NOTIFICATION_ID, milestone.id.toInt())
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                milestone.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun cancelMilestoneAlarms(
        context: Context,
        milestones: List<ReminderMilestoneEntity>
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        milestones.forEach { milestone ->
            val intent = Intent(context, ReminderBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                milestone.id.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
