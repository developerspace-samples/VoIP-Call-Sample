package com.developerspace.voipcalling.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.UsersRequest
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.developerspace.voipcalling.MainScreen
import com.developerspace.voipcalling.UserDetailScreen

class CallBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val ourCode = "*000#"
        val dialedNumber = resultData

        if (dialedNumber.contains(ourCode)) {

            // My app will bring up, so cancel the dialer broadcast
            resultData = null

            //Search for number in userslist
            val phoneNumber = dialedNumber.replace(ourCode,"")
            val userRequest = UsersRequest.UsersRequestBuilder().setTags(listOf(phoneNumber))
                    .setLimit(10).build()
            userRequest.fetchNext(object : CometChat.CallbackListener<List<User>>() {
                override fun onSuccess(p0: List<User>?) {
                    if (p0?.isNotEmpty()!!) {
                        val user = p0[0]
                        val intent = Intent(context, UserDetailScreen::class.java)
                        intent.putExtra("avatar", user.avatar)
                        intent.putExtra("name", user.name)
                        intent.putExtra("uid", user.uid)
                        intent.putExtra("number", user.statusMessage)
                        context?.startActivity(intent)
                    }
                }

                override fun onError(p0: CometChatException?) {
                    Toast.makeText(context,"Unable to find the user ${p0?.code}",
                            Toast.LENGTH_LONG).show()
                    val intent = Intent(context,MainScreen::class.java)
                    context?.startActivity(intent)
                }
            })
        }
    }
}