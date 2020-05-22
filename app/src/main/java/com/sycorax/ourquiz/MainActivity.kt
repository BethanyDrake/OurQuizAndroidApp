package com.sycorax.ourquiz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun connect(view: View) {
        val textView: TextView = findViewById(R.id.textView)
        textView.setText("Connecting...")
        val queue = Volley.newRequestQueue(this)
        val url = "http://10.0.2.2:8090/hello"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                textView.text = "Response is: ${response}"
            },
            Response.ErrorListener { textView.text = "That didn't work!" })


        queue.add(stringRequest)
    }
}
