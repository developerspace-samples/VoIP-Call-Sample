package com.developerspace.voipcalling.ui.logs

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.ClickListener
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerTouchListener
import com.developerspace.voipcalling.R
import com.developerspace.voipcalling.UserDetailScreen
import com.developerspace.voipcalling.utils.CallHandler
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_user_detail_screen.*
import kotlinx.android.synthetic.main.fragment_logs.*
import kotlinx.android.synthetic.main.no_call_layout.*

class LogsFragment : Fragment() {

    private lateinit var logsViewModel: LogsViewModel

    lateinit var logsAdapter : LogsAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        logsViewModel =
            ViewModelProvider(this).get(LogsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_logs, container, false)
        val searchBox: EditText = root.findViewById(R.id.search_box)
        val refreshBtn : ImageView = root.findViewById(R.id.refresh_btn)
        val logsList : RecyclerView = root.findViewById(R.id.logs_list)
        logsList.layoutManager = LinearLayoutManager(context)
        logsAdapter = LogsAdapter(requireContext())
        logsList.adapter = logsAdapter
        logsViewModel.fetchCalls()
        logsViewModel.logsList.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty())
                no_call_layout.visibility = View.GONE
            logsAdapter.updateList(it)
        })

        searchBox.setOnEditorActionListener(TextView.OnEditorActionListener { textView: TextView, i: Int, keyEvent: KeyEvent? ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                if (textView.text.isNotEmpty())
                    logsAdapter.filter.filter(textView.text.toString())
                else {
                    logsViewModel.messagesRequest = null
                    logsViewModel.fetchCalls()
                }
                return@OnEditorActionListener true
            }
            false
        })

        val rotateAnimation = ObjectAnimator.ofFloat(refreshBtn, "rotation", 0f, 360f);
        rotateAnimation.duration = 1000
        refreshBtn.setOnClickListener {
            searchBox.setText("")
            rotateAnimation.start()
            logsAdapter.resetAdapterList()
            logsViewModel.messagesRequest = null
            logsViewModel.fetchCalls()
        }
        logsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!logsList.canScrollVertically(1)) {
                    logsViewModel.fetchCalls()
                }
            }
        })
        logsList.addOnItemTouchListener(RecyclerTouchListener(context, logsList, object : ClickListener() {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onClick(var1: View?, var2: Int) {
                var call = var1?.getTag(R.string.call) as Call
                var uid: String? = null
                var name: String? = null
                var avatar: String?  = null
                var number:String? = null

                if ((call.callInitiator as User).uid == CometChat.getLoggedInUser().uid) {
                    var user = call.callReceiver as User
                    uid = user.uid
                    name = user.name
                    avatar = user.avatar
                    number = user.statusMessage
                } else {
                    var user = call.callInitiator as User
                    uid = user.uid
                    name = user.name
                    avatar = user.avatar
                    number = user.statusMessage
                }
                var type = CometChatConstants.RECEIVER_TYPE_USER

                val userDetailIntent = Intent(context, UserDetailScreen::class.java)
                userDetailIntent.putExtra("uid",uid)
                userDetailIntent.putExtra("name",name)
                userDetailIntent.putExtra("avatar",avatar)
                userDetailIntent.putExtra("number",number)
                startActivity(userDetailIntent)

                var1.findViewById<ImageView>(R.id.call_iv).setOnClickListener {
                    if ((context?.checkSelfPermission(Manifest.permission.CALL_PHONE)
                                    == PackageManager.PERMISSION_GRANTED) ||
                            (context?.checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS)
                                    == PackageManager.PERMISSION_GRANTED)) {
                        val callManager = CallHandler(context!!)
                        callManager.init()

                        CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
                            override fun onSuccess(p0: Call?) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
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
                }
            }
        }))
        return root
    }
}