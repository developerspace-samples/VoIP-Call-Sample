package com.developerspace.voipcalling

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.developerspace.voipcalling.utils.PushNotificationService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*

class SplashActivity : AppCompatActivity() {

    var googleSignInClient : GoogleSignInClient? = null

    lateinit var auth: FirebaseAuth

    val RC_SIGN_IN = 12

    val TAG = "SplashActivity"

    private val cometchatKey = CometChatConfig.KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = Firebase.auth

        logo.setShape("circle")

        if (auth.currentUser!=null && CometChat.getLoggedInUser()!=null) {
            finish()
            startActivity(Intent(this,MainScreen::class.java))
        }
        Handler().postDelayed({
            inputLayout.visibility = View.VISIBLE },4000)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        signInBtn?.setOnClickListener {
            if (phoneNumberEdt.text.trim().isNotEmpty() &&
                    PhoneNumberUtils.isGlobalPhoneNumber(phoneNumberEdt.text.toString()))
                signIn()
            else
                phoneNumberEdt.error = "Fill this Field"
        }
    }

    private fun isValidMobile(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }
    private fun signIn() {
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id + "\n" + account.idToken)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            CometChat.login(user.uid, cometchatKey, object : CometChat.CallbackListener<User>() {
                override fun onSuccess(p0: User?) {
                    var token = PushNotificationService().token
                    if (PushNotificationService().token == null) {
                        FirebaseInstanceId.getInstance().instanceId
                                .addOnCompleteListener(object :
                                        OnCompleteListener<InstanceIdResult?> {
                                    override fun onComplete(task: Task<InstanceIdResult?>) {
                                        if (!task.isSuccessful) {
                                            Toast.makeText(
                                                    this@SplashActivity,
                                                    task.exception!!.localizedMessage,
                                                    Toast.LENGTH_LONG
                                            ).show()
                                            return
                                        }
                                        token = task.result!!.token
                                        registerPushNotification(token!!)
                                    }
                                })
                    } else {
                        registerPushNotification(token!!)
                    }
                    startActivity(Intent(this@SplashActivity, MainScreen::class.java))
                }

                override fun onError(e: CometChatException?) {
                    //Need to confirm the error code
                    if (e?.code.equals("ERR_UID_NOT_FOUND"))
                        createCometChatUser(user)
                    else
                        Toast.makeText(applicationContext, e?.message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun registerPushNotification(token: String) {
        CometChat.registerTokenForPushNotification(token, object : CallbackListener<String?>() {
            override fun onSuccess(s: String?) {
                Toast.makeText(this@SplashActivity, s, Toast.LENGTH_LONG).show()
                Log.e("onSuccessPN: ", s!!)
            }

            override fun onError(e: CometChatException) {
                Log.e("onErrorPN: ", e.message!!)
                Toast.makeText(this@SplashActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        })
        finish()
        startActivity(Intent(this@SplashActivity, MainScreen::class.java))
    }

    private fun createCometChatUser(user: FirebaseUser) {
        val cometchatUser = User()
        cometchatUser.uid = user.uid
        cometchatUser.name = user.displayName
        cometchatUser.avatar = user.photoUrl?.toString()
        cometchatUser.statusMessage = phoneNumberEdt.text.toString()
        cometchatUser.tags = listOf(phoneNumberEdt.text.toString())
        CometChat.createUser(
                cometchatUser,
                cometchatKey,
                object : CometChat.CallbackListener<User>() {
                    override fun onSuccess(p0: User?) {
                        updateUI(auth.currentUser)
                    }

                    override fun onError(e: CometChatException?) {
                        Toast.makeText(
                                applicationContext, "CometChat Create User Error:" + e?.code,
                                Toast.LENGTH_LONG
                        ).show()
                    }
                })
    }
}