package com.developerspace.voipcalling

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.developerspace.voipcalling.utils.CallHandler
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.activity_user_detail_screen.*

class UserDetailScreen : AppCompatActivity() {
    val CALL_PERMISSIONS = {Manifest.permission.CALL_PHONE;Manifest.permission.MANAGE_OWN_CALLS}

    val callCountReference = FirebaseDatabase.getInstance().reference
        .child(CometChat.getLoggedInUser().uid)

    var callCount = 0L

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail_screen)

        var avatarUrl = when {
            intent.hasExtra("avatar") -> intent.getStringExtra("avatar")
            else -> ""
        }
        var name = when {
            intent.hasExtra("name") -> intent.getStringExtra("name")
            else -> "Sample User"
        }
        var uid = when {
            intent.hasExtra("uid") -> intent.getStringExtra("uid")
            else -> ""
        }
        var number = when {
            intent.hasExtra("number") -> intent.getStringExtra("number")
            else -> ""
        }

        callCountReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("callCount")) {
                    Log.e( "onDataChange: ",snapshot.child("callCount").value.toString())
                    callCount = snapshot.child("callCount").value as Long
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("onCancelled: ", "${error.code} \n ${error.message}")
            }
        })

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            user_avatar.setBackgroundColor(getColor(R.color.white))
        }
        if (avatarUrl.isNullOrEmpty())
            user_avatar.setInitials(name!!)
        else
            user_avatar.setAvatar(avatarUrl)
        user_name.text = name

        audio_call_fab.setOnClickListener {
            if ((checkSelfPermission(Manifest.permission.CALL_PHONE)
                            == PackageManager.PERMISSION_GRANTED) ||
                    (checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS)
                            == PackageManager.PERMISSION_GRANTED)) {
                val callManager = CallHandler(applicationContext)
                callManager.init()
                val call = Call(
                        uid!!, CometChatConstants.RECEIVER_TYPE_USER,
                        CometChatConstants.CALL_TYPE_AUDIO
                )
                CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
                    override fun onSuccess(p0: Call?) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            val mapReference = mutableMapOf<String, Any>()
                            mapReference.put("callCount", callCount + 1)
                            callCountReference.updateChildren(mapReference).addOnCompleteListener {
                                Log.e( "onSuccess: ","$callCount" )
                            }

                            callManager.startOutgoingCall(p0!!)
                        }
                    }

                    override fun onError(p0: CometChatException?) {
                        Snackbar.make(
                            audio_call_fab.rootView,
                            "Not able to initiate a call due to :${p0?.code}",
                            Snackbar.LENGTH_LONG
                        ).show()

                    }
                })
            }
//            CallUtils.initiateCall(
//                applicationContext, uid,
//                CometChatConstants.RECEIVER_TYPE_USER,
//                CometChatConstants.CALL_TYPE_AUDIO
//            )
        }

        video_call_fab.setOnClickListener {
            if ((checkSelfPermission(Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) ||
                (checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS)
                        == PackageManager.PERMISSION_GRANTED)) {
                val callManager = CallHandler(applicationContext)
                callManager.init()
                val call = Call(
                    uid!!, CometChatConstants.RECEIVER_TYPE_USER,
                    CometChatConstants.CALL_TYPE_VIDEO
                )
                CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
                    override fun onSuccess(p0: Call?) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            val mapReference = mutableMapOf<String, Any>()
                            mapReference.put("callCount", callCount + 1)
                            callCountReference.updateChildren(mapReference).addOnCompleteListener {
                                Log.e( "onSuccess: ","$callCount" )
                            }
                            callManager.startOutgoingCall(p0!!)
                        }
                    }

                    override fun onError(p0: CometChatException?) {
                        Snackbar.make(
                            audio_call_fab.rootView,
                            "Not able to initiate a call due to :${p0?.code}",
                            Snackbar.LENGTH_LONG
                        ).show()

                    }
                })
            }
//            CallUtils.initiateCall(applicationContext,uid,
//                CometChatConstants.RECEIVER_TYPE_USER,
//                CometChatConstants.CALL_TYPE_VIDEO)
        }

        chat_fab.setOnClickListener {
            val intent = Intent(this@UserDetailScreen, CometChatMessageListActivity::class.java)
            intent.putExtra(UIKitConstants.IntentStrings.UID, uid)
            intent.putExtra(UIKitConstants.IntentStrings.NAME, name)
            intent.putExtra(UIKitConstants.IntentStrings.AVATAR, avatarUrl)
            intent.putExtra(
                UIKitConstants.IntentStrings.TYPE,
                CometChatConstants.RECEIVER_TYPE_USER
            )
            startActivity(intent)
        }
    }
}