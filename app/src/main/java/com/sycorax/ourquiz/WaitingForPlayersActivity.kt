package com.sycorax.ourquiz

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
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

class WaitingForPlayersActivity : AppCompatActivity() {


    private fun parseList(stringList: String): List<String> {
        if (stringList == "null" || stringList.length <= 2) {
            return listOf<String>();
        }
        return stringList.subSequence(1,stringList.lastIndex).split(", ");
    }

    private fun displayPlayers(nameList: List<String>){
        val playerListView = findViewById<LinearLayout>(R.id.playerList)
        playerListView.removeAllViews()
        nameList.forEach {
            val textView = TextView(this)
            textView.text = it
            playerListView.addView(textView)
        }
    }

    private fun displayPlayersWithQuestions(nameList: List<String>){
        val playerListView = findViewById<LinearLayout>(R.id.playersWithQuestionList)
        playerListView.removeAllViews()
        nameList.forEach {
            val textView = TextView(this)
            textView.text = it +" " +  "✅"
            playerListView.addView(textView)
        }
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_for_players)
        addHostFragment()

        val queue = Volley.newRequestQueue(this)

        val quizId  = intent.extras.get("QUIZ_ID")
        var pendingRequests = 0;
        var requests = mutableListOf<StringRequest>()
        val waitABitThenUpdate= {
            pendingRequests = requests.count()
            Handler().postDelayed(
                {
                    requests.forEach {queue.add(it)}
                }, 1000)

        }

        val onRequestComplete = {

            pendingRequests-=1
            Log.wtf("aaa", "pendingRequests: "+ pendingRequests)
            if (pendingRequests <=0){
                waitABitThenUpdate()
            }
        }

        val requestFinishedListener: RequestFinishedListener<Any> = RequestFinishedListener{
            onRequestComplete()
        }


        val stringRequest = StringRequest(
            Request.Method.GET,
            "http://10.0.2.2:8090/listParticipantsWho?quizId=" +quizId + "&&hasSubmittedQuestion=false",
            Response.Listener<String> { response ->
                //Log.wtf("playersWithoutQuestions", response)
                displayPlayers(parseList(response))
            },
            Response.ErrorListener { Log.wtf("error", "a" )})

        val getPlayersWithQuestions = StringRequest(
            Request.Method.GET,
            "http://10.0.2.2:8090/listParticipantsWho?quizId=" +quizId + "&&hasSubmittedQuestion=true",
            Response.Listener<String> { response ->
                //Log.wtf("playersWithQuestions", response)
                displayPlayersWithQuestions(parseList(response))
            },
            Response.ErrorListener { Log.wtf("error", "a" )})

        val hasStarted = StringRequest(
            Request.Method.GET,
            "http://10.0.2.2:8090/hasStarted?quizId=" +quizId,
            Response.Listener<String> { response ->
                Log.wtf("has started", response)
                if (response == "true") {
                    queue.removeRequestFinishedListener(requestFinishedListener)
                    val intent = Intent(this, QuestionActivity::class.java)
//                intent.putExtra("QUIZ_ID", intent.extras.get("QUIZ_ID") as String)
//                intent.putExtra("PLAYER_NAME", intent.extras.get("PLAYER_NAME") as String)
//                intent.putExtra("HOST", intent.extras.get("HOST") as Boolean)
                    startActivity(intent)
                }

            },
            Response.ErrorListener { Log.wtf("error", "a" )})

        requests.add(stringRequest)
        requests.add(getPlayersWithQuestions)
        requests.add(hasStarted)

        waitABitThenUpdate()
        queue.addRequestFinishedListener(requestFinishedListener)

    }
}
