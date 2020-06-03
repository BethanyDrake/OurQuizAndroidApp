package com.sycorax.ourquiz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.Request
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_for_players)

        val queue = Volley.newRequestQueue(this)

        val quizId = intent.extras.get("QUIZ_ID")

        val stringRequest = StringRequest(
            Request.Method.GET,
            "http://10.0.2.2:8090/listParticipantsWho?quizId=" +quizId + "&&hasSubmittedQuestion=false",
            Response.Listener<String> { response ->
                Log.wtf("playersWithoutQuestions", response)
                displayPlayers(parseList(response))
            },
            Response.ErrorListener { Log.wtf("error", "a" )})

        val getPlayersWithQuestions = StringRequest(
            Request.Method.GET,
            "http://10.0.2.2:8090/listParticipantsWho?quizId=" +quizId + "&&hasSubmittedQuestion=true",
            Response.Listener<String> { response ->
                Log.wtf("playersWithQuestions", response)
                displayPlayersWithQuestions(parseList(response))
            },
            Response.ErrorListener { Log.wtf("error", "a" )})

        queue.add(stringRequest)
        queue.add(getPlayersWithQuestions)
        queue.addRequestFinishedListener<Any>{ queue.add(stringRequest) }


    }
}
