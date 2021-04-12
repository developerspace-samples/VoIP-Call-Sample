package com.developerspace.voipcalling

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_components.users.user_list.CometChatUserList
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main_screen.*

class MainScreen : AppCompatActivity() {

    private val CALL_REQUEST = 1

    private val callPermissions = arrayOf(
            Manifest.permission.MANAGE_OWN_CALLS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        if (Utils.hasPermissions(this@MainScreen, *callPermissions)) {
            Toast.makeText(this,"Permission Allowed",Toast.LENGTH_LONG).show()
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(callPermissions, CALL_REQUEST)
            }
        }
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_logs, R.id.navigation_users
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        settings.setOnClickListener {
            var settingsIntent = Intent(this,UserSettings::class.java)
            startActivity(settingsIntent)
        }
        CometChatUserList.setItemClickListener(object : OnItemClickListener<User>() {
            override fun OnItemClick(user: User?, position: Int) {
                val intent = Intent(this@MainScreen, UserDetailScreen::class.java)
                intent.putExtra("name", user?.name)
                intent.putExtra("avatar", user?.avatar)
                intent.putExtra("uid", user?.uid)
                startActivity(intent)
            }
        })
    }
}