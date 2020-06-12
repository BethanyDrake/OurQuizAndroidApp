package com.sycorax.ourquiz.WaitingForPlayers

import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.sycorax.ourquiz.StringRequestFactory


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

