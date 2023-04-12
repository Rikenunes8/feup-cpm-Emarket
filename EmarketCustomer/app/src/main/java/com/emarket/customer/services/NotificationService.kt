package com.emarket.customer.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.emarket.customer.R
import com.emarket.customer.activities.BasketActivity

class NotificationService {
    companion object {
        fun sendNotification(ctx: Context, title: String, message: String?, intent: Intent? = null) {
            val newIntent = intent ?: Intent(ctx, BasketActivity::class.java)
            //Get the notification manager
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val nc = NotificationChannel("EmarketChannel", "emarket_channel", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(nc)
            val pi = PendingIntent.getActivity(ctx, 0, newIntent, PendingIntent.FLAG_IMMUTABLE)

            //Create Notification Object
            val notification: Notification = Notification.Builder(ctx, nc.id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setContentTitle(title)
                .setContentText(message)
                .setChannelId(nc.id)
                .setAutoCancel(true)
                .build()
            //Send notification
            nm.notify(1, notification)
        }
    }
}