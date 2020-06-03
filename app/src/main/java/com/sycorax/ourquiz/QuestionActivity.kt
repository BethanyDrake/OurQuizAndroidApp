package com.sycorax.ourquiz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon

class QuestionActivity : AppCompatActivity() {


    private fun getQuestion(quizId:String){
        Log.wtf("bbb", "gettingt question" )

        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET,
            "http://10.0.2.2:8090/currentQuestion?quizId=" +quizId,
            Response.Listener<String> { response ->
                val parsedResponse = Klaxon().parse<Question>(response)
                if (parsedResponse == null){
                    Log.wtf("bbb", "failed to get question" )
                } else{
                    Log.wtf("bbb", "got question" )
                    populateUiWithQuestionDetails(parsedResponse)
                }
            },
            Response.ErrorListener { Log.wtf("bbb", "failed to get question 2" )})
        queue.add(stringRequest)


    }

    private fun populateUiWithQuestionDetails(question: Question) {
        findViewById<TextView>(R.id.questionText).text = question.questionText
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        val quizId  = intent.extras.get("QUIZ_ID")
        getQuestion(quizId.toString())
    }
}
