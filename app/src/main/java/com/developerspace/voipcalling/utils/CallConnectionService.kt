package com.developerspace.voipcalling.utils

import android.os.Build
import android.telecom.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.cometchat.pro.core.Call

@RequiresApi(Build.VERSION_CODES.M)
class CallConnectionService : ConnectionService() {


    companion object {
        var conn : CallConnection? = null
    }
    override fun onCreateIncomingConnection(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?): Connection {
        val bundle = request!!.extras
        val sessionID = bundle.getString("SESSIONID")
        val name = bundle.getString("NAME")
        val receiverType = bundle.getString("RECEIVERTYPE")
        val callType = bundle.getString("CALLTYPE")
        val receiverID = bundle.getString("RECEIVERID")
        val call = Call(receiverID!!, receiverType, callType)
        call.sessionId = sessionID
        conn = CallConnection(applicationContext, call)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            conn?.connectionProperties = Connection.PROPERTY_SELF_MANAGED
        }
        conn?.setCallerDisplayName(name, TelecomManager.PRESENTATION_ALLOWED)
        conn?.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        conn?.setInitializing()
        conn?.setActive()
        return conn!!
    }

    override fun onCreateIncomingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
        Log.e("onCreateIncomingFailed:",request.toString())
        Toast.makeText(applicationContext,"onCreateIncomingConnectionFailed",Toast.LENGTH_LONG).show();
    }

    override fun onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
        Log.e("onCreateOutgoingFailed:",request.toString())
        Toast.makeText(applicationContext,"onCreateOutgoingConnectionFailed",Toast.LENGTH_LONG).show();
    }

    override fun onCreateOutgoingConnection(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?): Connection {
        val bundle = request!!.extras
        val sessionID = bundle.getString("SESSIONID")
        val name = bundle.getString("NAME")
        val receiverType = bundle.getString("RECEIVERTYPE")
        val callType = bundle.getString("CALLTYPE")
        val receiverID = bundle.getString("RECEIVERID")
        Log.e("onCreateOutgoingConn","${bundle.toString()} \n $sessionID $name $receiverID $receiverType $callType")
        val call = Call(receiverID!!, receiverType, callType)
        call.sessionId = sessionID
        conn = CallConnection(applicationContext, call)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            conn?.connectionProperties = Connection.PROPERTY_SELF_MANAGED
        }
        conn?.setCallerDisplayName(name, TelecomManager.PRESENTATION_ALLOWED)
        conn?.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        conn?.setInitializing()
        conn?.setActive()
        return conn!!
    }
}