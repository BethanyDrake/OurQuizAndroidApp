package com.sycorax.ourquiz

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_host.*

class HostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
    }

    fun host(view: View) {
        val textView: TextView = findViewById(R.id.textView)
        textView.setText("Connecting...")
        val quizIdInputView: EditText = findViewById(R.id.editText)

        val queue = Volley.newRequestQueue(this)
        val url = "http://10.0.2.2:8090/create?quizId="+ quizIdInputView.text

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                textView.text = response
            },
            Response.ErrorListener { textView.text = "That didn't work!" })


        queue.add(stringRequest)
    }
}
