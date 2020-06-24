package com.sycorax.ourquiz.After

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.Before.MainActivity
import com.sycorax.ourquiz.IntentHelper
import com.sycorax.ourquiz.R
import com.sycorax.ourquiz.StringRequestFactory
import com.sycorax.ourquiz.VolleyRequestQueueFactory

data class PlayerScore(
    val name: String,
    val correctAnswers: Int
)
data class QuizResult(
    val quizId:String,
    val totalQuestions: Int,
    val playerScores: List<PlayerScore>
)

class ResultsActivity(val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
                      val stringRequestFactory: StringRequestFactory = StringRequestFactory(),
                      val intentHelper: IntentHelper = IntentHelper()
) : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        val queue = queueFactory.create(this)
        val url = "http://10.0.2.2:8090/quizResults?quizId=" + intentHelper.getQuizId(intent)
        val request = stringRequestFactory.create(Request.Method.GET, url, Response.Listener<String> {
            response ->
                display(Klaxon().parse<QuizResult>(response)!!)
        }, Response.ErrorListener { Log.wtf("aaaaa", "error") }
        )

        queue.add(request)


    }

    private fun display(result: QuizResult) {
        val resultsView = findViewById<LinearLayout>(R.id.resultsView)
        resultsView.removeAllViews()
        val rankedPlayers = result.playerScores.sortedByDescending { it.correctAnswers }
        rankedPlayers.forEach {
            val textView = TextView(this)

            textView.text = it.name + " (" + it.correctAnswers + "/" + result.totalQuestions + ")"
            resultsView.addView(textView)
        }
    }

    fun onClickBack(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val newIntent = Intent(this, MainActivity::class.java)
        startActivity(newIntent)
    }
}
