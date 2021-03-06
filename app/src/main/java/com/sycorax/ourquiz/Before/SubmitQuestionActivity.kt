package com.sycorax.ourquiz.Before

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.beust.klaxon.Klaxon
import com.sycorax.API_URL
import com.sycorax.ourquiz.During.WaitingForPlayersActivity
import com.sycorax.ourquiz.IntentFactory
import com.sycorax.ourquiz.Question
import com.sycorax.ourquiz.R
import com.sycorax.ourquiz.VolleyRequestQueueFactory

class RequestWithBody(val body:String, method:Int, url: String, responseListener: Response.Listener<String>,  errorListener: Response.ErrorListener )
    : StringRequest(method, url, responseListener, errorListener) {
    override fun getBody(): ByteArray {
        return body.toByteArray(Charsets.UTF_8)
    }

    override fun getBodyContentType(): String {
        return "application/json; charset=utf-8"
    }
}

class RequestWithBodyFactory {
    fun create(body:String, method: Int, url:String, responseListener: Response.Listener<String>, errorListener: Response.ErrorListener): RequestWithBody {
        return RequestWithBody(body, method, url, responseListener, errorListener)
    }
}

data class QuestionSubmissionBody(val quizId: String, val question: Question)

open class SubmitQuestionActivity(
    val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
    val requestWithBodyFactory: RequestWithBodyFactory = RequestWithBodyFactory(),
    val intentFactory: IntentFactory = IntentFactory()
    ) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_question)
    }

    fun getRequestListener(): Response.Listener<String> {
        val requestListener = Response.Listener<String> { response ->
            if (response == "OK") {
                val newIntent = intentFactory.create(this, WaitingForPlayersActivity::class.java)
                newIntent.putExtra("QUIZ_ID", intent.extras?.get("QUIZ_ID").toString())
                newIntent.putExtra("PLAYER_NAME", intent.extras?.get("PLAYER_NAME").toString())
                startActivity(newIntent)
            }
        }
        return requestListener
    }

    val onError =  Response.ErrorListener {
        Log.d("Error.Response", "error")
    }


    private fun getCorrectAnswer() : Int {
        val radioButtonIds = listOf(
            R.id.A,
            R.id.B,
            R.id.C,
            R.id.D
        )
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)

        return radioButtonIds.indexOf(radioGroup.checkedRadioButtonId)
    }



    fun submit(view: View) {
        val answerTexts: List<String> = listOf(
            R.id.TextA,
            R.id.TextB,
            R.id.TextC,
            R.id.TextD
        ).map { (findViewById<EditText>(it)).text.toString()  }
        val questionText = findViewById<EditText>(R.id.questionText).text.toString()

        val quizId = intent.extras?.get("QUIZ_ID").toString();
        val playerName = intent.extras?.get("PLAYER_NAME").toString();

        val url = API_URL + "submit"

        val queue = queueFactory.create(this)

        val question = Question(questionText, playerName, answerTexts, getCorrectAnswer())
        val submissionBody = QuestionSubmissionBody(quizId, question)
        val requestBody = Klaxon().toJsonString(submissionBody)
        val putRequest = requestWithBodyFactory.create(
            requestBody,
            Request.Method.PUT,
            url,
            getRequestListener(),
            onError
        )

        queue.add(putRequest)
    }

    override fun onBackPressed() {
        val newIntent = intentFactory.create(this, MainActivity::class.java)
        startActivity(newIntent)
    }
}
