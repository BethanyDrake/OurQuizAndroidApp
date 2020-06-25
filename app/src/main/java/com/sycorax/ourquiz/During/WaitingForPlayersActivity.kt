package com.sycorax.ourquiz.During

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import com.sycorax.API_URL
import com.sycorax.ourquiz.*
import com.sycorax.ourquiz.After.ResultsActivity
import com.sycorax.ourquiz.Before.MainActivity

data class StatusResponse(val questionNumber: Int, val revealed: Boolean)

fun parseList(stringList: String): List<String> {
    if (stringList == "null" || stringList.length <= 2) {
        return listOf();
    }
    return stringList.subSequence(1,stringList.lastIndex).split(", ");
}

class PollerFactory {
    fun create(context: Context): Poller {
        return Poller(context)
    }
}
class WaitingForPlayersActivity(
    val pollerFactory: PollerFactory = PollerFactory(),
    val requestFactory: StringRequestFactory = StringRequestFactory(),
    val intentFactory: IntentFactory = IntentFactory(),
    val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
    val intentHelper: IntentHelper = IntentHelper()
) : AppCompatActivity() {

    var poller: Poller? = null
    var queue: RequestQueue? = null

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

    fun startQuiz(view: View) {
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.PUT,
            API_URL + "start?quizId=" +intent.extras.get("QUIZ_ID"),
            Response.Listener<String> { response ->
            },
            Response.ErrorListener { Log.wtf("error", "a" )})
        queue.add(stringRequest)
    }

    fun revealAnswer(view: View) {
        if (queue == null) {
            queue = queueFactory.create(this)
        }

        val url = API_URL + "revealQuestion?quizId=" + intentHelper.getQuizId(intent) + "&questionNumber=" + intentHelper.getStage(intent)
        val request = requestFactory.create(Request.Method.PUT, url, Response.Listener<String> {}, Response.ErrorListener {})
        queue?.add(request)
    }

    private fun setVisibleButton(){
        val stage = intentHelper.getStage(intent)
        val startQuizButton = findViewById<Button>(R.id.startQuizButton)
        val revealAnswerButton = findViewById<Button>(R.id.revealAnswerButton)

        startQuizButton?.visibility = View.GONE
        revealAnswerButton?.visibility = View.GONE

        if (stage == -1) startQuizButton?.visibility = View.VISIBLE
        if (stage >= 0) revealAnswerButton?.visibility = View.VISIBLE
    }

    private fun addHostSection(){
        if (intentHelper.getAmHost(intent)) {
            val hostSection = findViewById<FrameLayout>(R.id.hostSection)
            hostSection.visibility = View.VISIBLE
            setVisibleButton()

        }
    }

    private fun revealAnswer() {
        poller?.stop()
        val newIntent = intentFactory.create(this, RevealAnswerActivity::class.java)
        intentHelper.copyExtrasFromIntent(intent, newIntent)

        startActivity(newIntent)
    }
    private fun openResults() {
        poller?.stop()
        val newIntent = intentFactory.create(this, ResultsActivity::class.java)
        intentHelper.copyExtrasFromIntent(intent, newIntent)
        newIntent.putExtra("STAGE", -1)
        startActivity(newIntent)
    }

    private fun continueToNextQuestion() {
        poller?.stop()

        val newIntent = if (intentHelper.getAmHost(intent) ){
            intentFactory.create(this, WaitingForPlayersActivity::class.java)
        } else {
            intentFactory.create(this, QuestionActivity::class.java)
        }
        intentHelper.copyExtrasFromIntent(intent, newIntent)
        newIntent.putExtra("STAGE", intentHelper.getStage(intent) + 1)
        startActivity(newIntent)
    }

    fun getHasStartedRequestListener(): Response.Listener<String> {
        return  Response.Listener<String> { response ->
            val currentStage = intentHelper.getStage(intent)

            val parsedReponse = Klaxon().parse<StatusResponse>(response) ?: StatusResponse(-1,false)
            val respondingStage: Int = parsedReponse.questionNumber
            if (currentStage == -1 && parsedReponse.revealed) {
                openResults()
            }


            if (currentStage == respondingStage && parsedReponse.revealed) {
                revealAnswer()
            }

            if (currentStage < respondingStage) {
                continueToNextQuestion()
            }
        }
    }

    val defaultErrorListener = Response.ErrorListener { Log.wtf("error", "a" )}

    fun innerOnCreate() {
        addHostSection()
        poller = pollerFactory.create(this)


        val playerListView = findViewById<LinearLayout>(R.id.playerList)
        val getPlayersWithoutQuestions = GetPlayersRequestFactory(
            requestFactory, this, playerListView, false, intentHelper.getQuizId(intent)
        ).create()

        val playersWithQuestionListView = findViewById<LinearLayout>(R.id.playersWithQuestionList)
        val getPlayersWithQuestions =  GetPlayersRequestFactory(
            requestFactory, this, playersWithQuestionListView, true, intentHelper.getQuizId(intent)
        ).create()


        val hasStarted = requestFactory.create(
            Request.Method.GET,
            API_URL + "stage?quizId=" + intentHelper.getQuizId(intent),
            getHasStartedRequestListener(),
            defaultErrorListener)

        val requests = listOf(getPlayersWithoutQuestions, getPlayersWithQuestions, hasStarted)
        poller!!.start(requests)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_for_players)
        innerOnCreate()
    }
    override fun onBackPressed() {
        val newIntent = intentFactory.create(this, MainActivity::class.java)
        startActivity(newIntent)
    }
}
