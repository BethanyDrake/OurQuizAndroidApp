package com.sycorax.ourquiz

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.RequestQueue.RequestFinishedListener
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.w3c.dom.Text

class Poller(context: Context) {
    /*
        On start it adds all the requests.
        Once all the request are complete, it waits a little while, then re-adds them.

        On stop it no longer re-adds the requests after they are completed.
     */

    private var pendingRequests = 0
    private var requests: List<StringRequest> = listOf()

    private val queue: RequestQueue
    init {
        queue =  Volley.newRequestQueue(context)
    }

    private fun waitAndThenDo () {
        pendingRequests = requests.count()
        Handler().postDelayed(
            {
                requests.forEach {queue.add(it)}
            }, 1000)

    }

    private fun onRequestComplete(){

        pendingRequests-=1
        //Log.wtf("aaa", "pendingRequests: "+ pendingRequests)
        if (pendingRequests <=0){
            waitAndThenDo()
        }
    }

    private val requestFinishedListener: RequestFinishedListener<Any> = RequestFinishedListener {
        onRequestComplete()
    }

    fun start(requests: List<StringRequest>) {
        this.requests = requests
        waitAndThenDo()
        queue.addRequestFinishedListener(requestFinishedListener)
    }

    fun stop() {
        queue.removeRequestFinishedListener(requestFinishedListener)
    }
}

fun parseList(stringList: String): List<String> {
    if (stringList == "null" || stringList.length <= 2) {
        return listOf();
    }
    return stringList.subSequence(1,stringList.lastIndex).split(", ");
}

class GetPlayersRequestFactory(
    val requestFactory: StringRequestFactory,
    val context: Context,
    val playerListView: LinearLayout,
    val waiting: Boolean,
    val quizId: String
) {
    private fun displayPlayers(nameList: List<String>) {
        //val playerListView = context.findViewById<LinearLayout>(R.id.playerList)
        playerListView.removeAllViews()
        nameList.forEach {
            val textView = TextView(context)
            var mark = ""
            if (!waiting) mark = " ✅";
            textView.text = it + mark
            playerListView.addView(textView)
        }
    }

    private fun getListener(): Response.Listener<String> {
        return Response.Listener<String> { response ->
            //Log.wtf("playersWithoutQuestions", response)
            displayPlayers(parseList(response))
        }

    }

    val errorListener = Response.ErrorListener { Log.wtf("error", "a") }


    fun create(): StringRequest {
        return requestFactory.create(
            Request.Method.GET,
            "http://10.0.2.2:8090/listParticipantsWho?quizId=" + quizId + "&&waiting=" + waiting,
            getListener(),
            errorListener
        )
    }
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

    private fun amHost(): Boolean {
       return intent.extras.get("HOST") == true
    }

    private fun addHostFragment(){
        if (amHost()) {
            supportFragmentManager.beginTransaction()
                .add(R.id.hostSectionFragmentContainer, WaitingForPlayersHostSection.newInstance())
                .commit()
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
        addHostFragment()
        if (poller == null) poller = Poller(this)

        val quizId  = intent.extras.get("QUIZ_ID")

        val playerListView = findViewById<LinearLayout>(R.id.playerList)
        val getPlayersWithoutQuestions = GetPlayersRequestFactory(
            requestFactory, this, playerListView, false, quizId.toString()).create()

        val playersWithQuestionListView = findViewById<LinearLayout>(R.id.playersWithQuestionList)
        val getPlayersWithQuestions =  GetPlayersRequestFactory(
            requestFactory, this, playersWithQuestionListView, true, quizId.toString()).create()


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
