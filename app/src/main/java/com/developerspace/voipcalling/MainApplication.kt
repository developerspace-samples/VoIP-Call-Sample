package com.developerspace.voipcalling

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.widget.Toast
import com.cometchat.pro.core.AppSettings
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.uikit.ui_components.calls.call_manager.listener.CometChatCallListener
import com.developerspace.voipcalling.utils.CometChatCallHandler


class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val settings = AppSettings.AppSettingsBuilder().setRegion(CometChatConfig.REGION)
            .subscribePresenceForAllUsers().build()
        CometChat.init(
            applicationContext,
            CometChatConfig.APP_ID,
            settings,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) {
                    Toast.makeText(applicationContext, "CometChat Initialized", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onError(e: CometChatException?) {
                    Toast.makeText(
                        applicationContext,
                        "CometChat Failed" + e?.message,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })
        CometChatCallHandler().attachCallListener(BuildConfig.APPLICATION_ID,applicationContext)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name: CharSequence = getString(R.string.app_name)
        val description = "Notification Channel"
        val importance: Int = NotificationManager.IMPORTANCE_HIGH
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("2", name, importance)
            channel.setDescription(description)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        CometChatCallListener.removeCallListener(BuildConfig.APPLICATION_ID)
    }
}