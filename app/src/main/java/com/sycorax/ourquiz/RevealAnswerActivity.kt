package com.sycorax.ourquiz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.beust.klaxon.Klaxon

data class RevealAnswerResponse(val answerText: String, val yourAnswer: String)


class RevealAnswerActivity(val requestFactory: StringRequestFactory = StringRequestFactory(), val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory()) : AppCompatActivity() {


    fun getResponseListener(): Response.Listener<String> {
        return Response.Listener<String>{response ->
            try {
                val parsedResponse = Klaxon().parse<RevealAnswerResponse>(response)
                val correctAnswerView = findViewById<TextView>(R.id.correctAnswer)
                correctAnswerView.text = parsedResponse?.answerText

                var mark = ""
                if(parsedResponse?.answerText == parsedResponse?.yourAnswer) {
                    mark = "✅"
                }else {
                    mark = "❌"
                }
                val yourAnswerView = findViewById<TextView>(R.id.yourAnswer)
                yourAnswerView.text = parsedResponse?.yourAnswer + " " + mark
            }catch (e: Exception) {
                Log.e("revealAnswer", "failed to reveal answer: "+ response)
            }

        }
    }

    fun innerOnCreate() {

        val request = requestFactory.create(
            Request.Method.GET,
            "http://10.0.2.2:8090/correctAnswer?" +
                    "quizId=" + getQuizId(intent) +
                    "&questionNumber=" + getCurrentQuestion(intent) +
                    "&playerName=" + getPlayerName(intent),
            getResponseListener(),
            Response.ErrorListener {  }

        )

        val queue = queueFactory.create(this)
        queue.add(request)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reveal_answer)
        innerOnCreate()

        Log.wtf("name", "RevealAnswerActivity: " + getPlayerName(intent))
    }
}
