package com.sycorax.ourquiz

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.join_activity.*

class JoinActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.join_activity)
    }

    fun join(view: View) {
        val textView: TextView = findViewById(R.id.textView)
        textView.setText("Connecting...")
        val quizIdInputView: EditText = findViewById(R.id.editText)

        val nameInputView: EditText = findViewById(R.id.nameField)
        val name = nameInputView.text

        val queue = Volley.newRequestQueue(this)
        val quizId = quizIdInputView.text
        val url = "http://10.0.2.2:8090/join?" +
                "quizId=" + quizId + "&" +
                "name="+name

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                textView.text = response
                if (response.equals("ok", true)) {
                    val intent = Intent(this, SubmitQuestionActivity::class.java)
                    intent.putExtra("QUIZ_ID", quizId);
                    intent.putExtra("PLAYER_NAME", name);
                    startActivity(intent)
                }
            },
            Response.ErrorListener { textView.text = "That didn't work!" })


        queue.add(stringRequest)
    }
}
