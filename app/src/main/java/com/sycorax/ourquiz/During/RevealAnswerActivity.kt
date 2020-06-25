package com.sycorax.ourquiz.During

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.beust.klaxon.Klaxon
import com.sycorax.API_URL
import com.sycorax.ourquiz.*
import com.sycorax.ourquiz.After.ResultsActivity
import com.sycorax.ourquiz.Before.MainActivity

data class RevealAnswerResponse(val answerText: String, val yourAnswer: String)


class RevealAnswerActivity(
    val requestFactory: StringRequestFactory = StringRequestFactory(),
    val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
    val intentHelper: IntentHelper = IntentHelper(),
    val pollerFactory: PollerFactory = PollerFactory(),
    val intentFactory: IntentFactory = IntentFactory()
    ) : AppCompatActivity() {

    var poller : Poller? = null
    var queue : RequestQueue? = null


    override fun onPostResume() {
        super.onPostResume()
        poller?.resume()
    }

    override fun onPause() {
        super.onPause()
        poller?.stop()
    }

    override fun onStop() {
        super.onStop()
        poller?.stop()
    }

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
            API_URL + "correctAnswer?" +
                    "quizId=" + intentHelper.getQuizId(intent) +
                    "&questionNumber=" + intentHelper.getCurrentQuestion(intent) +
                    "&playerName=" + intentHelper.getPlayerName(intent)+
                    "&isHost=" + intentHelper.getAmHost(intent),
            getResponseListener(),
            Response.ErrorListener {  }

        )

        queue?.add(request)
    }

    fun getOnGetStage() :Response.Listener<String>{
        return Response.Listener<String>{
            response ->
            val parsedResponse = Klaxon().parse<StatusResponse>(response)
//            Log.wtf("aaaa", " " + parsedResponse)
            if (parsedResponse != null && parsedResponse.questionNumber == -1 && parsedResponse.revealed) {
                //open end screen
                Log.wtf("aaaa", "last question")
                poller?.stop()
                val newIntent = intentFactory.create(this, ResultsActivity::class.java)
                intentHelper.copyExtrasFromIntent(intent, newIntent)
                newIntent.putExtra("STAGE", parsedResponse.questionNumber)

                startActivity(newIntent)
            }
            if (parsedResponse != null && parsedResponse.questionNumber> intentHelper.getCurrentQuestion(intent)) {
                //Log.wtf("next", "moving to next question")
                poller?.stop()
                val newIntent = intentFactory.create(this, QuestionActivity::class.java)
                intentHelper.copyExtrasFromIntent(intent, newIntent)
                newIntent.putExtra("STAGE", parsedResponse.questionNumber)

                startActivity(newIntent)
            }
        }
    }

    private fun startPolling() {
        val request = requestFactory.create(
            Request.Method.GET,
            API_URL + "stage?" +
                    "quizId=" + intentHelper.getQuizId(intent),
            getOnGetStage(),
            Response.ErrorListener {  }

        )
        poller?.start(listOf(request))

    }

    fun onClickNextQuestion(view: View) {
        val request = requestFactory.create(
            Request.Method.PUT,
            API_URL + "nextQuestion?" +
                    "quizId=" + intentHelper.getQuizId(intent) +
                    "&currentQuestion=" + intentHelper.getCurrentQuestion(intent),
            Response.Listener {  },
            Response.ErrorListener {  }
        )
        queue?.add(request)
    }

    private fun showHostSectionsOnly(){

        val hostSection = findViewById<FrameLayout>(R.id.hostSection)
        hostSection.visibility = View.VISIBLE

        val yourAnswerSection = findViewById<View>(R.id.yourAnswerSection)
        yourAnswerSection.visibility = View.GONE


    }

    fun innerOnCreate() {
        poller = pollerFactory.create(this)
        queue = queueFactory.create(this)
        revealAnswer()
        startPolling()
        if (intentHelper.getAmHost(intent)) {
            showHostSectionsOnly()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reveal_answer)
        innerOnCreate()
    }

    override fun onBackPressed() {
        val newIntent = intentFactory.create(this, MainActivity::class.java)
        startActivity(newIntent)
    }
}
