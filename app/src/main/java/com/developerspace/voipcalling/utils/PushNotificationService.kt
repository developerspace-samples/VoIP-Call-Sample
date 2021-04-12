package com.developerspace.voipcalling.utils

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.helpers.CometChatHelper
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.MediaMessage
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.Utils.getBitmapFromURL
import com.developerspace.voipcalling.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class PushNotificationService : FirebaseMessagingService() {

    var count = 0

    var token: String? = null

    var MESSAGE_REQUEST = 80;

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val payload =  remoteMessage.data
        val message = CometChatHelper.processMessage(JSONObject(payload["message"]!!))
        val title = payload["title"]
        val alert = payload["alert"]
        Log.e( "onMessageReceived: ", "$payload \n $message \n $title $alert")
        if (message is Call) {
            initiateCallService(message)
        } else {
            count++
            showMessageNotification(message, title!!, alert!!);
        }
    }

    override fun onNewToken(str: String) {
        super.onNewToken(str)
        token = str
    }

    private fun initiateCallService(call: Call) {
        try {
            var callManager: CallHandler? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("initiateCallService: ",call.toString())
                callManager = CallHandler(applicationContext)
                callManager.init()
                callManager.startIncomingCall(call)
            }
        } catch (e: Exception) {
            Log.e("initiateCallError:","${e.message}" )
            Toast.makeText(applicationContext, "Unable to receive call due to " + e.message, Toast.LENGTH_LONG)
        }
    }

    private fun showMessageNotification(baseMessage: BaseMessage, title: String, alert: String) {
        val messageIntent = Intent(applicationContext, CometChatMessageListActivity::class.java)
        messageIntent.putExtra(UIKitConstants.IntentStrings.TYPE, baseMessage.receiverType)
        if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
            messageIntent.putExtra(UIKitConstants.IntentStrings.NAME, baseMessage.sender.name)
            messageIntent.putExtra(UIKitConstants.IntentStrings.UID, baseMessage.sender.uid)
            messageIntent.putExtra(UIKitConstants.IntentStrings.AVATAR, baseMessage.sender.avatar)
            messageIntent.putExtra(UIKitConstants.IntentStrings.STATUS, baseMessage.sender.status)
        } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
            messageIntent.putExtra(UIKitConstants.IntentStrings.GUID, (baseMessage.receiver as Group).guid)
            messageIntent.putExtra(UIKitConstants.IntentStrings.NAME, (baseMessage.receiver as Group).name)
            messageIntent.putExtra(UIKitConstants.IntentStrings.GROUP_DESC, (baseMessage.receiver as Group).description)
            messageIntent.putExtra(UIKitConstants.IntentStrings.GROUP_TYPE, (baseMessage.receiver as Group).groupType)
            messageIntent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER, (baseMessage.receiver as Group).owner)
            messageIntent.putExtra(UIKitConstants.IntentStrings.MEMBER_COUNT, (baseMessage.receiver as Group).membersCount)
        }
        val messagePendingIntent = PendingIntent.getActivity(applicationContext,
                MESSAGE_REQUEST, messageIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val logoDrawable = applicationContext.resources.getDrawable(R.mipmap.ic_launcher,resources.newTheme())
        val bmpLogo = Bitmap.createBitmap(logoDrawable.intrinsicWidth,logoDrawable.intrinsicHeight,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmpLogo)
        logoDrawable.setBounds(0,0,canvas.width,canvas.height)
        logoDrawable.draw(canvas)

        val largeIcon : Bitmap = when {
            baseMessage.sender.avatar!=null && !baseMessage.sender.avatar.equals("null",ignoreCase = true)
            -> getBitmapFromURL(baseMessage.sender.avatar)
            else -> bmpLogo
        }
        val builder = NotificationCompat.Builder(this, "2")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(resources.getColor(R.color.colorPrimary))
                .setLargeIcon(largeIcon)
                .setGroup("GROUP_ID")
                .setContentIntent(messagePendingIntent)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
            builder.setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(getBitmapFromURL((baseMessage as MediaMessage).attachment.fileUrl)))
        }
        val summaryBuilder = NotificationCompat.Builder(this, "2")
                .setContentTitle(getString(R.string.app_name))
                .setContentText("$count messages")
                .setSmallIcon(R.drawable.cc)
                .setGroup("GROUP_ID")
                .setGroupSummary(true)
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(baseMessage.id, builder.build())
        notificationManager.notify(0, summaryBuilder.build())

    }
}