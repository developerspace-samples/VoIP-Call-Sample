package com.developerspace.voipcalling.utils

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatCallActivity
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatStartCallActivity
import com.cometchat.pro.uikit.ui_resources.utils.CallUtils

/**
 * CometChatCallHandler used to handle callbacks for incoming and outgoing
 * audio & video calls.
 */
class CometChatCallHandler {
    var isInitialized = false

    /**
     * This method is used to attach cometchat call listener.
     * @param TAG is a unique Identifier
     * @param context is a object of Context.
     */
    fun attachCallListener(TAG: String?, context: Context?) {
        isInitialized = true
        CometChat.addCallListener(TAG!!, object : CometChat.CallListener() {
            override fun onIncomingCallReceived(call: Call) {
                Log.e("onIncomingCallReceive:","$call" )
                if (CometChat.getActiveCall() == null) {
//                    try {
//                        var callManager: CallHandler? = null
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            Log.e("initiateCall: ", call.toString())
//                            callManager = CallHandler(context!!)
//                            callManager.init()
//                            callManager.startIncomingCall(call)
//                        }
//                    } catch (e: Exception) {
//                        Toast.makeText(
//                            context,
//                            "Unable to receive call due to " + e.message,
//                            Toast.LENGTH_LONG
//                        )
//                    }
                    if (call.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        CallUtils.startCallIntent(
                            context, call.callInitiator as User, call.type,
                            false, call.sessionId
                        )
                    } else {
                        CallUtils.startGroupCallIntent(
                            context, call.receiver as Group, call.type,
                            false, call.sessionId
                        )
                    }
                } else {
                    CometChat.rejectCall(call.sessionId, CometChatConstants.CALL_STATUS_BUSY,
                        object : CallbackListener<Call>() {
                            override fun onSuccess(call: Call) {
                                Toast.makeText(
                                    context, "${call.sender.name} tried to call you",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            override fun onError(e: CometChatException) {
                                Toast.makeText(
                                    context, "Failed to reject the call due to  ${e.code}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        })
                }
            }

            override fun onOutgoingCallAccepted(call: Call) {
                if (CometChatStartCallActivity.activity == null) {
                    if (CometChatCallActivity.callActivity != null) {
                        CometChatCallActivity.cometChatAudioHelper.stop(false)
                        CallUtils.startCall(CometChatCallActivity.callActivity, call)
                    }
                } else {
                    CometChatStartCallActivity.activity.finish()
                    if (CometChatCallActivity.callActivity != null) {
                        CometChatCallActivity.cometChatAudioHelper.stop(false)
                        CallUtils.startCall(CometChatCallActivity.callActivity, call)
                    }
                }
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onOutgoingCallRejected(call: Call) {
                Log.e("onOutgoingCallReject:", call.toString())
                if (CallConnectionService.conn != null)
                    CallConnectionService.conn?.onOutgoingReject()
                else
                    Log.e("onOutgoingCallRejected:", "CallConnectionService is null")
                if (CometChatCallActivity.callActivity != null)
                    CometChatCallActivity.callActivity.finish()
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onIncomingCallCancelled(call: Call) {
                Log.e("onIncomingCallCancel:", call.toString())
                CallConnectionService.conn?.onDisconnect()
                if (CometChatCallActivity.callActivity != null) CometChatCallActivity.callActivity.finish()
            }
        })
    }

    /**
     * It is used to remove call listener from app.
     * @param TAG is a unique Identifier
     */
    fun removeCallListener(TAG: String?) {
        isInitialized = false
        CometChat.removeCallListener(TAG!!)
    }

    /**
     * This method is used to make a initiate a call.
     * @param context is a object of Context.
     * @param receiverId is a String, It is unique receiverId. It can be either uid of user or
     * guid of group
     * @param receiverType is a String, It can be either CometChatConstant.RECEIVER_TYPE_USER or
     * CometChatConstant.RECEIVER_TYPE_GROUP
     * @param callType is a String, It is call type which can be either CometChatConstant.CALL_TYPE_AUDIO
     * or CometChatConstant.CALL_TYPE_VIDEO
     *
     * @see CometChat.initiateCall
     */
    fun makeCall(context: Context?, receiverId: String?, receiverType: String?, callType: String?) {
        CallUtils.initiateCall(context, receiverId, receiverType, callType)
    }
}