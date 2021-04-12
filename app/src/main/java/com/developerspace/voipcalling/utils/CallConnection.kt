package com.developerspace.voipcalling.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatStartCallActivity
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants

@RequiresApi(Build.VERSION_CODES.M)
class CallConnection(context: Context, call: Call) : Connection() {

    val TAG = "CallConnection"
    private var cometchatCall : Call = call
    private var connectionContext : Context = context

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        Log.e(TAG, "onCallAudioStateChange:" + state.toString())
    }

    override fun onStateChanged(state: Int) {
        super.onStateChanged(state)
        Log.e(TAG, "onStateChanged: $state" )
    }

    override fun onDisconnect() {
        super.onDisconnect()
        destroyConnection()
        Log.e(TAG,"onDisconnect")
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL, "Missed"))
        if (CometChat.getActiveCall()!=null)
            onDisconnect(CometChat.getActiveCall())
    }

    fun onDisconnect(call : Call) {
        Log.e(TAG,"onDisconnect Call: $call")
        CometChat.rejectCall(call.sessionId,CometChatConstants.CALL_STATUS_CANCELLED,
                object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(p0: Call?) {
                Log.e(TAG, "onSuccess: reject")
          }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(connectionContext,"Unable to end call due to ${p0?.code}",
                        Toast.LENGTH_LONG).show()
            }
        })
    }
    override fun onHold() {
        super.onHold()
    }

    override fun onAnswer(videoState: Int) {
        super.onAnswer(videoState)
    }

    override fun onStopDtmfTone() {
        super.onStopDtmfTone()
        Log.e(TAG, "onStopDtmfTone: " )
    }

    override fun onCallEvent(event: String?, extras: Bundle?) {
        super.onCallEvent(event, extras)
        Log.e(TAG, "onCallEvent: $event" )
    }

    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
    }

    override fun onAnswer() {
        if (cometchatCall.sessionId != null) {
            CometChat.acceptCall(cometchatCall.sessionId, object : CallbackListener<Call>() {
                override fun onSuccess(call: Call) {
                    destroyConnection()
                    val acceptIntent = Intent(connectionContext, CometChatStartCallActivity::class.java)
                    acceptIntent.putExtra(UIKitConstants.IntentStrings.SESSION_ID, call.sessionId)
                    acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    connectionContext.startActivity(acceptIntent)
                }

                override fun onError(e: CometChatException) {
                    destroyConnection()
                    Toast.makeText(connectionContext, "Call cannot be answered due to " + e.code, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    fun destroyConnection() {
        setDisconnected(DisconnectCause(DisconnectCause.REMOTE, "Rejected"))
        Log.e(TAG, "destroyConnection" )
        super.destroy()
    }

    override fun onReject() {
        Log.e(TAG, "onReject: " )
        if (cometchatCall.sessionId != null) {
            CometChat.rejectCall(cometchatCall.sessionId, CometChatConstants.CALL_STATUS_REJECTED, object : CallbackListener<Call?>() {
                override fun onSuccess(call: Call?) {
                    Log.e(TAG, "onSuccess: reject")
                    destroyConnection()
                  }

                override fun onError(e: CometChatException) {
                    destroyConnection()
                    Log.e(TAG, "onErrorReject: " + e.message)
                    Toast.makeText(connectionContext, "Call cannot be rejected due to" + e.code, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    override fun onAbort() {
        super.onAbort()
        Log.e(TAG,"OnAbort")
    }

    fun onOutgoingReject() {
        Log.e(TAG,"onDisconnect")
        destroyConnection()
        setDisconnected(DisconnectCause(DisconnectCause.REMOTE, "REJECTED"))
    }

    override fun onReject(rejectReason: Int) {
        Log.e(TAG, "onReject: $rejectReason")
        super.onReject(rejectReason)
    }

    override fun onReject(replyMessage: String?) {
        Log.e(TAG, "onReject: $replyMessage" )
        super.onReject(replyMessage)
    }
}