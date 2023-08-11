package com.khoavna.loadingapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager


class MyApplication : Application() {

    companion object {
        const val CHANNEL_ID = "CHANNEL_ID_DOWNLOAD"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name  = "Channel DownLoad"
        val description = "Channel DownLoad Description"
        val importance = NotificationManager.IMPORTANCE_MIN
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}