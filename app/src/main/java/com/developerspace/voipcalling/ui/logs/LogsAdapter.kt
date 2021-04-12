package com.developerspace.voipcalling.ui.logs

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.models.*
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_settings.UISettings
import com.developerspace.voipcalling.R
import com.developerspace.voipcalling.databinding.LogsListRowBinding
import java.util.*

/**
 * Purpose - ConversationListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of conversations. It helps to organize the list data in recyclerView.
 * It also help to perform search operation on list of conversation.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */
class LogsAdapter : RecyclerView.Adapter<LogsAdapter.LogsViewHolder?>, Filterable {
    private var context: Context

    private var logsList: MutableList<BaseMessage> = ArrayList()
    private var filterLogsList: MutableList<BaseMessage>? = ArrayList()
    private var fontUtils: FontUtils

    /***
     * @param context is a object of Context.
     * @param LogsList is list of logs used in this adapter.
     */
    constructor(context: Context, logsList: MutableList<BaseMessage>) {
        this.logsList = logsList
        filterLogsList = logsList
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }

    /**
     * It is a constructor which is used to initialize wherever we needed.
     *
     * @param context
     */
    constructor(context: Context) {
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val logsListRowBinding: LogsListRowBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.logs_list_row,
                parent,
                false
            )
        return LogsViewHolder(logsListRowBinding)
    }

    /**
     *
     * @param logsViewHolder is a object of LogsViewHolder.
     * @param position is a position of item in recyclerView.
     *
     */
    override fun onBindViewHolder(logsViewHolder: LogsViewHolder, position: Int) {
        val baseMessage = filterLogsList!![position]
        var avatar: String? = null
        val name: String
        var callMessageText = ""
        var isIncoming: Boolean
        var isVideo: Boolean
        var isMissed = false
        var uid :String? = null

        var call = baseMessage as Call
        logsViewHolder.logsListRowBinding.call = call
        val initiatorUser = call.callInitiator as User
        val receiverUser = call.callReceiver as User
        if (initiatorUser.uid == CometChat.getLoggedInUser().uid) {
            val callName = receiverUser.name
            logsViewHolder.logsListRowBinding.callSenderName.text = callName
            avatar = receiverUser.avatar
            name = receiverUser.name
            if (call.callStatus == CometChatConstants.CALL_STATUS_UNANSWERED ||
                call.callStatus == CometChatConstants.CALL_STATUS_CANCELLED) {
                callMessageText = context.resources.getString(R.string.missed_call)
                isMissed = true
            } else if (call.callStatus == CometChatConstants.CALL_STATUS_REJECTED) {
                callMessageText = context.resources.getString(R.string.rejected_call)
            } else callMessageText = context.resources.getString(R.string.outgoing)
            uid = receiverUser.uid
            isIncoming = false
        } else {
            val callName = initiatorUser.name
            logsViewHolder.logsListRowBinding.callSenderName.text = callName
            avatar = initiatorUser.avatar
            name = initiatorUser.name
            if (call.callStatus == CometChatConstants.CALL_STATUS_UNANSWERED ||
                call.callStatus == CometChatConstants.CALL_STATUS_CANCELLED) {
                callMessageText = context.resources.getString(R.string.missed_call)
                isMissed = true
            } else if (call.callStatus == CometChatConstants.CALL_STATUS_REJECTED) {
                callMessageText = context.resources.getString(R.string.rejected_call)
            } else callMessageText = context.resources.getString(R.string.incoming)
            uid = call.sender.uid
            isIncoming = true
        }

        if (call.type == CometChatConstants.CALL_TYPE_VIDEO) {
            callMessageText =
                callMessageText + " " + context.resources.getString(com.cometchat.pro.uikit.R.string.video_call)
            isVideo = true
        } else {
            callMessageText =
                callMessageText + " " + context.resources.getString(com.cometchat.pro.uikit.R.string.audio_call)
            isVideo = false
        }
        if (isVideo) {
            logsViewHolder.logsListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(
                com.cometchat.pro.uikit.R.drawable.ic_videocam_24dp,
                0,
                0,
                0
            )
        } else {
            if (isIncoming && isMissed) {
                logsViewHolder.logsListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(
                    com.cometchat.pro.uikit.R.drawable.ic_call_missed_incoming_24dp,
                    0,
                    0,
                    0
                )
            } else if (isIncoming && !isMissed) {
                logsViewHolder.logsListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(
                    com.cometchat.pro.uikit.R.drawable.ic_call_incoming_24dp,
                    0,
                    0,
                    0
                )
            } else if (!isIncoming && isMissed) {
                logsViewHolder.logsListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(
                    com.cometchat.pro.uikit.R.drawable.ic_call_missed_outgoing_24dp,
                    0,
                    0,
                    0
                )
            } else {
                logsViewHolder.logsListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(
                    com.cometchat.pro.uikit.R.drawable.ic_call_outgoing_24dp,
                    0,
                    0,
                    0
                )
            }
        }
        logsViewHolder.logsListRowBinding.calltimeTv.setText(Utils.getLastMessageDate(call.initiatedAt))

        logsViewHolder.logsListRowBinding.callMessage.text = callMessageText

        logsViewHolder.logsListRowBinding.callSenderName.typeface =
            fontUtils.getTypeFace(FontUtils.robotoRegular)
        logsViewHolder.logsListRowBinding.callMessage.typeface =
            fontUtils.getTypeFace(FontUtils.robotoMedium)
        logsViewHolder.logsListRowBinding.calltimeTv.typeface =
            fontUtils.getTypeFace(FontUtils.robotoRegular)

        logsViewHolder.logsListRowBinding.callIv.setImageResource(R.drawable.ic_baseline_call_24)

        logsViewHolder.logsListRowBinding.callSenderAvatar.setBackgroundColor(
            Color.parseColor(
                UISettings.getColor()
            )
        )

        if (avatar != null && avatar.isNotEmpty()) {
            logsViewHolder.logsListRowBinding.callSenderAvatar.setAvatar(avatar)
        } else {
            logsViewHolder.logsListRowBinding.callSenderAvatar.setInitials(name)
        }

        logsViewHolder.logsListRowBinding.root.setTag(
            R.string.call,
            call
        )
    }

    override fun getItemCount(): Int {
        return filterLogsList!!.size
    }

    /**
     *
     * @param logs is a list of BaseMessage which will be updated in adapter.
     */
    fun updateList(logs: List<BaseMessage?>) {
        for (i in logs.indices) {
            if (filterLogsList!!.contains(logs[i])) {
                val index = filterLogsList!!.indexOf(logs[i])
                filterLogsList!!.remove(logs[i])
                filterLogsList!!.add(index, logs[i]!!)
            } else {
                filterLogsList!!.add(logs[i]!!)
            }
        }
        notifyDataSetChanged()
    }

    /**
     *
     * @param logs is a object of BaseMessage.
     *
     */
    fun remove(logs: BaseMessage) {
        val position = filterLogsList!!.indexOf(logs)
        filterLogsList!!.remove(logs)
        notifyItemRemoved(position)
    }


    /**
     * @param logs is an object of BaseMessage. It will be added to filterLogsList.
     *
     */
    fun add(logs: BaseMessage) {
        if (filterLogsList != null) filterLogsList!!.add(logs)
    }

    /**
     * This method is used to reset the adapter by clearing filterLogsList.
     */
    fun resetAdapterList() {
        filterLogsList!!.clear()
        notifyDataSetChanged()
    }

    /**
     * It is used to perform search operation in filterLogsList.
     *
     * @return
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val searchKeyword = charSequence.toString()
                filterLogsList = if (searchKeyword.isEmpty()) {
                    logsList
                } else {
                    val tempFilter: MutableList<BaseMessage> = ArrayList()
                    for (logs in filterLogsList!!) {
                        logs as Call
                        val initiator = logs.callInitiator as User
                        val receiver = logs.callReceiver as User
                        if (initiator.name.contains(searchKeyword,ignoreCase = true)) {
                            tempFilter.add(logs)
                        } else if (receiver.name.contains(searchKeyword,ignoreCase = true)) {
                            tempFilter.add(logs)
                        }
                    }
                    tempFilter
                }
                val filterResults = FilterResults()
                filterResults.values = filterLogsList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterLogsList = filterResults.values as MutableList<BaseMessage>
                notifyDataSetChanged()
            }
        }
    }

    data class LogsViewHolder(var logsListRowBinding: LogsListRowBinding) :
        RecyclerView.ViewHolder(logsListRowBinding.root)
}