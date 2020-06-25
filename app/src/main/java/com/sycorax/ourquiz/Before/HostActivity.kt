package com.sycorax.ourquiz.Before

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.sycorax.API_URL
import com.sycorax.ourquiz.During.WaitingForPlayersActivity
import com.sycorax.ourquiz.R
import com.sycorax.ourquiz.StringRequestFactory
import com.sycorax.ourquiz.VolleyRequestQueueFactory

class HostActivity (
    val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
    val stringRequestFactory: StringRequestFactory = StringRequestFactory()
)

    : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
    }

//
//    fun onResponse(response: String)
//    {
//            val intent = Intent(this, WaitingForPlayersActivity::class.java)
//            intent.putExtra("QUIZ_ID", quizId);
//            intent.putExtra("HOST", true);
//            startActivity(intent)
//    }


    fun host(view: View) {
        val textView: TextView = findViewById(R.id.textView)
        textView.setText("Connecting...")
        val quizIdInputView: EditText = findViewById(R.id.editText)

        val queue = queueFactory.create(this)
        val quizId = quizIdInputView.text
        val url = API_URL + "create?quizId="+ quizId

        val stringRequest = stringRequestFactory.create(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                val intent = Intent(this, WaitingForPlayersActivity::class.java)
                intent.putExtra("QUIZ_ID", quizId);
                intent.putExtra("HOST", true);
                startActivity(intent)
            },
            Response.ErrorListener { textView.text = "That didn't work!" })


        queue.add(stringRequest)
    }
}
