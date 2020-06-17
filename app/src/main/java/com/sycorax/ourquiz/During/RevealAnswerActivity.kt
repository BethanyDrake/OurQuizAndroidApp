package com.sycorax.ourquiz.During

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.*

data class RevealAnswerResponse(val answerText: String, val yourAnswer: String)


class RevealAnswerActivity(
    val requestFactory: StringRequestFactory = StringRequestFactory(),
    val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
    val intentHelper: IntentHelper = IntentHelper(),
    val pollerFactory: PollerFactory = PollerFactory(),
    val intentFactory: IntentFactory = IntentFactory()
    ) : AppCompatActivity() {

    var poller : Poller? = null;


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

    private fun revealAnswer(){
        val request = requestFactory.create(
            Request.Method.GET,
            "http://10.0.2.2:8090/correctAnswer?" +
                    "quizId=" + intentHelper.getQuizId(intent) +
                    "&questionNumber=" + intentHelper.getCurrentQuestion(intent) +
                    "&playerName=" + intentHelper.getPlayerName(intent),
            getResponseListener(),
            Response.ErrorListener {  }

        )

        val queue = queueFactory.create(this)
        queue.add(request)
    }

    fun getOnGetStage() :Response.Listener<String>{
        return Response.Listener<String>{
            response ->
            val parsedResponse = Klaxon().parse<StatusResponse>(response)
            if (parsedResponse != null && parsedResponse.questionNumber> intentHelper.getCurrentQuestion(intent)) {
                poller?.stop()
                val newIntent = intentFactory.create(this, QuestionActivity::class.java)
                startActivity(newIntent)
            }
        }
    }

    private fun startPolling() {
        val request = requestFactory.create(
            Request.Method.GET,
            "http://10.0.2.2:8090/stage?" +
                    "quizId=" + intentHelper.getQuizId(intent),
            getOnGetStage(),
            Response.ErrorListener {  }

        )
        poller?.start(listOf(request))

    }

    fun innerOnCreate() {
        poller = pollerFactory.create(this)
        revealAnswer()
        startPolling()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reveal_answer)
        innerOnCreate()

        Log.wtf("name", "RevealAnswerActivity: " + intentHelper.getPlayerName(intent))
    }
}
