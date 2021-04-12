package com.developerspace.voipcalling.ui.logs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.BaseMessage
import java.util.*
import kotlin.collections.ArrayList

class LogsViewModel : ViewModel() {

    var messagesRequest : MessagesRequest? = null

    private val _logsList = MutableLiveData<List<BaseMessage?>>()

    val logsList: LiveData<List<BaseMessage?>> = _logsList

    fun fetchCalls() {
        if (messagesRequest == null) {
            messagesRequest =
                MessagesRequest.MessagesRequestBuilder()
                    .setCategories(listOf(CometChatConstants.CATEGORY_CALL))
                    .setLimit(30).build()
        }

        messagesRequest?.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage?>?>() {

            override fun onSuccess(p0: List<BaseMessage?>?) {
                Log.e( "onSuccess: ",p0?.toString()!! )
                Collections.reverse(p0)
                _logsList.value = filterList(p0)
            }

            override fun onError(e: CometChatException) {
                Log.e("onError: ", e.message!!)

            }
        })
    }

    private fun filterList(p0: List<BaseMessage?>): List<BaseMessage?>? {
        val tempList = ArrayList<BaseMessage>()
        for (message in p0) {
            val call = message as Call
            if (call.callStatus!=CometChatConstants.CALL_STATUS_INITIATED)
                tempList.add(call)
        }
        return tempList
    }
}