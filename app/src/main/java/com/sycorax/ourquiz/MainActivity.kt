package com.sycorax.ourquiz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun join(view: View) {
        val intent = Intent(this, JoinActivity::class.java)
        startActivity(intent)
    }
    fun host(view: View) {
        val intent = Intent(this, HostActivity::class.java)
        startActivity(intent)
    }
}
