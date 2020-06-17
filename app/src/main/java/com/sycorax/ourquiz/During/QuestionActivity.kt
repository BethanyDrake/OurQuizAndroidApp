package com.sycorax.ourquiz.During

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.*

class QuestionActivity(
    val requestFactory: StringRequestFactory = StringRequestFactory(),
    val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
    val intentFactory: IntentFactory = IntentFactory(),
    val submitAnswerService: SubmitAnswerService = SubmitAnswerService(),
    val logger: Logger = Logger(),
    val intentHelper: IntentHelper = IntentHelper()
) : AppCompatActivity() {


    fun getListener() : Response.Listener<String> {
        return Response.Listener<String> { response ->
            val parsedResponse = Klaxon().parse<Question>(response)
            if (parsedResponse == null){
            } else{
                populateUiWithQuestionDetails(parsedResponse)
            }
        }
    }

    fun getOnFinishedSubmittingAnswer(): ()->Unit{
        return {

            val newIntent = intentFactory.create(this, WaitingForPlayersActivity::class.java)

            intentHelper.copyExtrasFromIntent(intent, newIntent)
            newIntent.putExtra("STAGE", 0)

            startActivity(newIntent)
        }
    }

    private fun getQuestion(quizId:String){

        val queue = queueFactory.create(this)
        val stringRequest = requestFactory.create(
            Request.Method.GET,
            "http://10.0.2.2:8090/currentQuestion?quizId=" +quizId,
            getListener(),
            Response.ErrorListener { Log.wtf("bbb", "failed to get question" )})

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

    private fun getQuizId() : String {
        return intent.extras.get("QUIZ_ID").toString()
    }

    private fun getPlayerName() : String {
        return intent.extras.get("PLAYER_NAME").toString()
    }
//    fun getQuestionNumber():Int {
//        val stageExtra = intent.extras.get("STAGE")
//        if (stageExtra is Int && stageExtra >= 0) {
//            return stageExtra
//        }
//        throw Exception("Expected extra: STAGE: Int, with value >= 0 . Instead found: $stageExtra")
//    }

    private fun getSelectedAnswer() : Int {
        val radioButtonIds = listOf(
            R.id.A,
            R.id.B,
            R.id.C,
            R.id.D
        )
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)

        return radioButtonIds.indexOf(radioGroup.checkedRadioButtonId)
    }

    fun onSelectAnswer(view: View) {
        val body = SubmitAnswerBody(
            getQuizId(),
            getPlayerName(),
            0,
            getSelectedAnswer()
        )

        try{
            submitAnswerService.submitAnswer(this, getOnFinishedSubmittingAnswer(),body)
        }catch (e:Exception){
            logger.d("io", "failed to submit answer")
            return
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
