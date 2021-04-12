package com.developerspace.voipcalling

import android.content.Intent
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_user_settings.*


class UserSettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)
        val user = CometChat.getLoggedInUser()
        userName.text = user.name
        userAvatar.setAvatar(user.avatar)
        userStatus.text = user.statusMessage

        val callCountReference = FirebaseDatabase.getInstance().reference
                .child(user.uid);
        callCountReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("callCount")) {
                    var count = snapshot.child("callCount").value as Long
                    callsCount.setCount(count.toInt())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e( "onCancelled: ","${error.code} \n ${error.message}" )
            }
        })
        callAccounts.setOnClickListener {
            val intent = Intent()
            intent.action = TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS
//                val componentName = ComponentName("com.android.server.telecom", "com.android.server.telecom.settings.EnableAccountPreferenceActivity")
//                intent.setComponent(componentName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        logoutBtn.setOnClickListener {
            CometChat.logout(object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) {
                    FirebaseAuth.getInstance().signOut()
                    finish()
                    startActivity(Intent(this@UserSettings,SplashActivity::class.java))
                }

                override fun onError(p0: CometChatException?) {
                    Toast.makeText(this@UserSettings,p0?.message,Toast.LENGTH_LONG).show()
                }
            })
        }
        deleteBtn.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            user.delete().addOnCompleteListener { p0 ->
                if (p0.isSuccessful) {
                    finish()
                    startActivity(Intent(this@UserSettings, SplashActivity::class.java))
                }
            }
        }
    }
}