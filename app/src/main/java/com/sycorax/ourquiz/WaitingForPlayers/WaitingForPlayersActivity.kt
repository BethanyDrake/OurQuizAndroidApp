package com.sycorax.ourquiz.WaitingForPlayers

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.sycorax.ourquiz.*

fun parseList(stringList: String): List<String> {
    if (stringList == "null" || stringList.length <= 2) {
        return listOf();
    }
    return stringList.subSequence(1,stringList.lastIndex).split(", ");
}

class WaitingForPlayersActivity : AppCompatActivity {

    var poller: Poller? = null
    val requestFactory: StringRequestFactory
    private var intentFactory: IntentFactory

    constructor(poller: Poller, requestFactory: StringRequestFactory, intentFactory: IntentFactory){
        this.poller = poller
        this.requestFactory = requestFactory
        this.intentFactory = intentFactory
    }

    constructor(){
        //poller = Poller(this)
        requestFactory = StringRequestFactory()
        intentFactory = IntentFactory()
    }

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
            "http://10.0.2.2:8090/start?quizId=" +intent.extras.get("QUIZ_ID"),
            Response.Listener<String> { response ->
                Log.wtf("started quiz", response)
            },
            Response.ErrorListener { Log.wtf("error", "a" )})
        queue.add(stringRequest)
    }

    fun revealAnswer(view: View) {
        Log.wtf("button press", "revealed answer")
    }

    private fun amHost(): Boolean {
       return intent.extras.get("HOST") == true
    }

    private fun setVisibleButton(){
        val stage = getCurrentStage()
        Log.wtf("updating", "setting visible button for stage: " + stage)
        val startQuizButton = findViewById<Button>(R.id.startQuizButton)
        val revealAnswerButton = findViewById<Button>(R.id.revealAnswerButton)

        startQuizButton?.visibility = View.GONE
        revealAnswerButton?.visibility = View.GONE

        if (stage == -1) startQuizButton?.visibility = View.VISIBLE
        if (stage >= 0) revealAnswerButton?.visibility = View.VISIBLE
    }

    private fun addHostSection(){
        if (amHost()) {
            Log.wtf("updating", "adding host section")
            val hostSection = findViewById<FrameLayout>(R.id.hostSection)
            hostSection.visibility = View.VISIBLE
            setVisibleButton()

        }
    }

    private fun getCurrentStage (): Int {
        val stage = intent.extras?.get("STAGE")
        if (stage is Int) {
            return stage
        }
        return -1
    }

    private fun getQuizId (): String {
        return intent.extras?.get("QUIZ_ID").toString()
    }
    private fun getPlayerName () : String {
        val playerName = intent.extras.get("PLAYER_NAME")
        if (playerName is String) {
            return playerName
        }
        Log.e("extra error", "playerName: " +(playerName) )
        return ""
    }

    fun getHasStartedRequestListener(): Response.Listener<String> {
       // Log.wtf("bbb", "gettingHasStartedListener for stage: " + getCurrentStage() )

        return  Response.Listener<String> { response ->
//            Log.wtf("has started", response)
            val currentStage = getCurrentStage()
            val respondingStage: Int = response.toInt()

            if (currentStage < respondingStage) {
                poller?.stop()

                if (amHost() ){
                    val newIntent = intentFactory.create(this, WaitingForPlayersActivity::class.java)
                    newIntent.putExtra("QUIZ_ID", getQuizId ())
                    newIntent.putExtra("STAGE", getCurrentStage()+1)
                    newIntent.putExtra("HOST", true)
                    startActivity(newIntent)

                } else {
                    val newIntent = intentFactory.create(this, QuestionActivity::class.java)
                    newIntent.putExtra("QUIZ_ID", getQuizId())
                    newIntent.putExtra("PLAYER_NAME", getPlayerName())
//                intent.putExtra("HOST", intent.extras.get("HOST") as Boolean)
                    startActivity(newIntent)
                }

            }
        }
    }

    val defaultErrorListener = Response.ErrorListener { Log.wtf("error", "a" )}

    fun innerOnCreate() {
        addHostSection()
        if (poller == null) poller = Poller(this)

        val quizId  = intent.extras.get("QUIZ_ID")

        val playerListView = findViewById<LinearLayout>(R.id.playerList)
        val getPlayersWithoutQuestions = GetPlayersRequestFactory(
            requestFactory, this, playerListView, false, quizId.toString()
        ).create()

        val playersWithQuestionListView = findViewById<LinearLayout>(R.id.playersWithQuestionList)
        val getPlayersWithQuestions =  GetPlayersRequestFactory(
            requestFactory, this, playersWithQuestionListView, true, quizId.toString()
        ).create()


        val hasStarted = requestFactory.create(
            Request.Method.GET,
            "http://10.0.2.2:8090/stage?quizId=" +quizId,
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
}
