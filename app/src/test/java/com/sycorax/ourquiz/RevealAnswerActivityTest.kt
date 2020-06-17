package com.sycorax.ourquiz

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.WaitingForPlayers.WaitingForPlayersActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class RevealAnswerActivityTest {

    @Test
    fun `requests the answer for this question of this quiz`(){

        val mRequestFactory = mockk<StringRequestFactory>(relaxed = true)
        val activity = spyk(RevealAnswerActivity(mRequestFactory, mockk(relaxed = true)))
        val extras = mapOf(Pair("STAGE", 1), Pair("QUIZ_ID", "a-quiz-id"), Pair("PLAYER_NAME", "a-name"))
        every { activity.intent} returns createMockIntentWithExtras(extras)
        val mListener = mockk<Response.Listener<String>>("response listener")
        every { activity.getResponseListener()} returns mListener
        activity.innerOnCreate()

        verify { mRequestFactory.create(
            Request.Method.GET,
            match { it.endsWith("/correctAnswer?quizId=a-quiz-id&questionNumber=1&playerName=a-name") },
            mListener,
            any()) }

    }

    @Test
    fun `displays the correct answer and your answer -- with a tick if they match`(){
        val activity = spyk(RevealAnswerActivity());
        val mCorrectAnswer: TextView = mockk(relaxed = true)

        every {
            activity.findViewById<TextView>(R.id.correctAnswer)
        } returns mCorrectAnswer

        val mYourAnswer: TextView = mockk(relaxed = true)

        every {
            activity.findViewById<TextView>(R.id.yourAnswer)
        } returns mYourAnswer

        val correctAnswerText = "I am the right answer."
        val yourAnswerText = "I am the answer you submitted."
        val response = RevealAnswerResponse(correctAnswerText, yourAnswerText)

        activity.getResponseListener().onResponse(Klaxon().toJsonString(response))


        verify { mCorrectAnswer.setText(correctAnswerText) }
        verify { mYourAnswer.setText(match<String> { it.startsWith(yourAnswerText) } )}
    }

    @Test
    fun `when your answer was correct -- displays your answer with a tick `(){
        val activity = spyk(RevealAnswerActivity());
        val mCorrectAnswer: TextView = mockk(relaxed = true)

        every {
            activity.findViewById<TextView>(R.id.correctAnswer)
        } returns mCorrectAnswer

        val mYourAnswer: TextView = mockk(relaxed = true)

        every {
            activity.findViewById<TextView>(R.id.yourAnswer)
        } returns mYourAnswer

        val correctAnswerText = "I am the right answer."
        val yourAnswerText = correctAnswerText
        val response = RevealAnswerResponse(correctAnswerText, yourAnswerText)

        activity.getResponseListener().onResponse(Klaxon().toJsonString(response))

        verify { mYourAnswer.setText(yourAnswerText + " ✅") }
    }

    @Test
    fun `when your answer was wrong -- displays your answer with a cross `(){
        val activity = spyk(RevealAnswerActivity());
        val mCorrectAnswer: TextView = mockk(relaxed = true)

        every {
            activity.findViewById<TextView>(R.id.correctAnswer)
        } returns mCorrectAnswer

        val mYourAnswer: TextView = mockk(relaxed = true)

        every {
            activity.findViewById<TextView>(R.id.yourAnswer)
        } returns mYourAnswer

        val correctAnswerText = "I am the right answer."
        val yourAnswerText = "WRONG!!!"
        val response = RevealAnswerResponse(correctAnswerText, yourAnswerText)

        activity.getResponseListener().onResponse(Klaxon().toJsonString(response))

        verify { mYourAnswer.setText(yourAnswerText + " ❌") }
    }
}
