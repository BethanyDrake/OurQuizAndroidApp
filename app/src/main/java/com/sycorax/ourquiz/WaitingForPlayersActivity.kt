package com.sycorax.ourquiz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
        return stringList.subSequence(1,stringList.lastIndex-1).split(", ");
    }

    private fun formatList(nameList: List<String>): String{
        var formattedList = "";
        nameList.forEach { formattedList += it + "\n"}

        return formattedList
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_for_players)

        val queue = Volley.newRequestQueue(this)

        val quizId = intent.extras.get("QUIZ_ID")
        val url = "http://10.0.2.2:8090/listParticipants?quizId=" +quizId
        val participantsTextView = findViewById<TextView>(R.id.participantsView)

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                participantsTextView.text = formatList(parseList(response))
            },
            Response.ErrorListener { participantsTextView.text = "error" })

        queue.add(stringRequest)
        queue.addRequestFinishedListener<Any>{ queue.add(stringRequest) }


    }
}
