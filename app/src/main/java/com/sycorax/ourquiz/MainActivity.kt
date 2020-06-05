package com.sycorax.ourquiz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity(val intentFactory: IntentFactory = IntentFactory()) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun join(view: View) {
        val intent = intentFactory.create(this, JoinActivity::class.java)
        startActivity(intent)
    }
    fun host(view: View) {
        val intent = intentFactory.create(this, HostActivity::class.java)
        startActivity(intent)
    }
}
