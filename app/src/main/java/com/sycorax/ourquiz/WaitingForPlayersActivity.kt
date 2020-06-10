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
        Log.wtf("bbbbb", "" + context)
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
        Log.wtf("aaa", "pendingRequests: "+ pendingRequests)
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
    val answeredQuestion: Boolean,
    val quizId: String
) {
    private fun displayPlayers(nameList: List<String>) {
        //val playerListView = context.findViewById<LinearLayout>(R.id.playerList)
        playerListView.removeAllViews()
        nameList.forEach {
            val textView = TextView(context)
            var mark = ""
            if (answeredQuestion) mark = " ✅";
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
            "http://10.0.2.2:8090/listParticipantsWho?quizId=" + quizId + "&&hasSubmittedQuestion=" + answeredQuestion,
            getListener(),
            errorListener
        )
    }
}

class WaitingForPlayersActivity : AppCompatActivity {

    var poller: Poller? = null
    val requestFactory: StringRequestFactory
    constructor(poller: Poller, requestFactory: StringRequestFactory){
        this.poller = poller
        this.requestFactory = requestFactory
    }

    constructor(){
        //poller = Poller(this)
        requestFactory = StringRequestFactory()
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

    private fun addHostFragment(){
        if (intent.extras.get("HOST") == true) {
            supportFragmentManager.beginTransaction()
                .add(R.id.hostSectionFragmentContainer, WaitingForPlayersHostSection.newInstance())
                .commit()
        }
    }

    private fun getHasStartedRequestListener(poller: Poller, quizId: String): Response.Listener<String> {
        return  Response.Listener<String> { response ->
            Log.wtf("has started", response)
            if (response == "true") {
                poller.stop()
                val intent = Intent(this, QuestionActivity::class.java)
                intent.putExtra("QUIZ_ID", quizId.toString())
//                intent.putExtra("PLAYER_NAME", intent.extras.get("PLAYER_NAME") as String)
//                intent.putExtra("HOST", intent.extras.get("HOST") as Boolean)
                startActivity(intent)
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
            "http://10.0.2.2:8090/hasStarted?quizId=" +quizId,
            getHasStartedRequestListener(poller!!, quizId.toString()),
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
