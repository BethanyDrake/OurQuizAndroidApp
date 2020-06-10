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

class QuestionActivity(
    val requestFactory: StringRequestFactory = StringRequestFactory(),
    val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory()
) : AppCompatActivity() {


    fun getListener() : Response.Listener<String> {
        return Response.Listener<String> { response ->
            val parsedResponse = Klaxon().parse<Question>(response)
            if (parsedResponse == null){
               // Log.wtf("bbb", "failed to get question" )
            } else{
                //Log.wtf("bbb", "got question" )
                populateUiWithQuestionDetails(parsedResponse)
            }
        }
    }


    private fun getQuestion(quizId:String){
        //Log.wtf("bbb", "gettingt question" )

        val queue = queueFactory.create(this)
        val stringRequest = requestFactory.create(
            Request.Method.GET,
            "http://10.0.2.2:8090/currentQuestion?quizId=" +quizId,
            getListener(),
            Response.ErrorListener { Log.wtf("bbb", "failed to get question 2" )})

        queue.add(stringRequest)


    }

    private fun populateUiWithQuestionDetails(question: Question) {
        findViewById<TextView>(R.id.questionText).text = question.questionText

        if (question.answers.size == 4) {
            findViewById<TextView>(R.id.A).text = question.answers[0]
            findViewById<TextView>(R.id.B).text = question.answers[1]
            findViewById<TextView>(R.id.C).text = question.answers[2]
            findViewById<TextView>(R.id.D).text = question.answers[3]
        }

     }

    fun innerOnCreate() {
        val quizId  = intent.extras.get("QUIZ_ID")
        getQuestion(quizId.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)
        innerOnCreate()
    }
}
