package com.sycorax.ourquiz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.R.id.questionText

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


class SubmitQuestionActivity(
    val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
    val requestWithBodyFactory: RequestWithBodyFactory = RequestWithBodyFactory()
    ) : AppCompatActivity() {
    data class SubmissionBody(val quizId: String, val question: Question)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_question)
    }

    fun submit(view: View) {
//        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
//        val correctAnswerButtonId = radioGroup.checkedRadioButtonId
//        val radioButtonIds = listOf(R.id.A, R.id.B, R.id.C, R.id.D)
//        val correctAnswer = radioButtonIds.indexOf(correctAnswerButtonId)
//        val answerTexts: List<String> = listOf(R.id.TextA, R.id.TextB, R.id.TextC, R.id.TextD).map { (findViewById<EditText>(it)).text.toString()  }
        val questionText = findViewById<EditText>(R.id.questionText).text.toString()


        val quizId: String = intent.extras.get("QUIZ_ID").toString();
        val playerName: String = intent.extras.get("PLAYER_NAME").toString();

        val url = "http://10.0.2.2:8090/submit"

        val queue = queueFactory.create(this)

        val question = Question(questionText, playerName)
        val submissionBody = SubmissionBody(quizId, question)
        val requestBody = Klaxon().toJsonString(submissionBody)

        val putRequest = requestWithBodyFactory.create(
            requestBody,
            Request.Method.PUT,
            url,
            Response.Listener { response ->
                                // response
                if (response == "OK") {
                    val intent = Intent(this, WaitingForPlayersActivity::class.java)
                    intent.putExtra("QUIZ_ID", quizId);
                    intent.putExtra("PLAYER_NAME", playerName);
                    startActivity(intent)
                }
                Log.d("Response", response)
            },
            Response.ErrorListener {
                                // error
                Log.d("Error.Response", "error")
            }

        )

        queue.add(putRequest)
    }
}
