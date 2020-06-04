package com.sycorax.ourquiz

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.join_activity.*

open class JoinActivity (
    val volleyRequestQueueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
    val volleyStringRequestFactory: StringRequestFactory = StringRequestFactory()
)
    : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.join_activity)
    }

    var quizId = ""
    var name = ""


    fun onResponse(response: String) {
        textView.text = response
        if (response.equals("ok", true)) {
            val intent = Intent(this, SubmitQuestionActivity::class.java)
            intent.putExtra("QUIZ_ID", quizId);
            intent.putExtra("PLAYER_NAME", name);
            startActivity(intent)
        }
    }

    fun join(view: View) {
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "Connecting..."
        val quizIdInputView: EditText = findViewById(R.id.editText)

        val nameInputView: EditText = findViewById(R.id.nameField)
        val name:String = nameInputView.text.toString()

        val queue = volleyRequestQueueFactory.create(this)
        val quizId:String = quizIdInputView.text.toString()
        val url:String = "http://10.0.2.2:8090/join?" +
                "quizId=" + quizId + "&" +
                "name="+name

        val stringRequest = volleyStringRequestFactory.create(
            Request.Method.GET, url,
            Response.Listener<String> { response -> onResponse(response)
            },
            Response.ErrorListener { textView.text = "That didn't work!" })

        queue.add(stringRequest)
    }
}
