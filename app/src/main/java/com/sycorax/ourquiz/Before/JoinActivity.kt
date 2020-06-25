package com.sycorax.ourquiz.Before

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.sycorax.API_URL
import com.sycorax.ourquiz.*
import com.sycorax.ourquiz.During.WaitingForPlayersActivity
import com.sycorax.ourquiz.R.id.textView
import kotlinx.android.synthetic.main.join_activity.*

open class JoinActivity (
    val volleyRequestQueueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
    val volleyStringRequestFactory: StringRequestFactory = StringRequestFactory(),
    val intentFactory: IntentFactory = IntentFactory()
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
            val intent = intentFactory.create(this, SubmitQuestionActivity::class.java)
            intent.putExtra("QUIZ_ID", quizId);
            intent.putExtra("PLAYER_NAME", name);
            intent.putExtra("STAGE", -1);
            startActivity(intent)
        }

        if (response.equals("OK - already joined")) {
            val intent = intentFactory.create(this, WaitingForPlayersActivity::class.java)
            intent.putExtra("QUIZ_ID", quizId);
            intent.putExtra("PLAYER_NAME", name);
            intent.putExtra("STAGE", -1);
            startActivity(intent)
        }
    }

    fun join(view: View) {
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "Connecting..."
        val quizIdInputView: EditText = findViewById(R.id.editText)

        val nameInputView: EditText = findViewById(R.id.nameField)
        name = nameInputView.text.toString()

        val queue = volleyRequestQueueFactory.create(this)
        quizId = quizIdInputView.text.toString()

        val url:String = API_URL + "join?" +
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
