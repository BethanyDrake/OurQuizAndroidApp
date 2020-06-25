package com.sycorax.ourquiz.During

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.beust.klaxon.Klaxon
import com.sycorax.API_URL
import com.sycorax.ourquiz.*
import io.mockk.*
import io.mockk.MockKSettings.relaxed
import net.bytebuddy.agent.VirtualMachine
import org.junit.Test

class RevealAnswerActivityTest {

    fun createRevealAnswersActivity(
        requestFactory: StringRequestFactory = mockk(relaxed = true),
        queueFactory: VolleyRequestQueueFactory = mockk(relaxed = true),
        intentHelper: IntentHelper = mockk(relaxed = true),
        pollerFactory: PollerFactory = mockk(relaxed = true),
        intentFactory: IntentFactory = mockk(relaxed = true)
    ): RevealAnswerActivity {
        val activity = spyk(RevealAnswerActivity(
            requestFactory,
            queueFactory,
            intentHelper,
            pollerFactory,
            intentFactory
        ))
        every {
            activity.intent
        } returns mockk()
        every {activity.startActivity(any())} returns Unit

        addViews(activity)

        return activity;
    }

    fun addViews(activity: AppCompatActivity) {
        every { activity.findViewById<FrameLayout>(R.id.hostSection) } returns mockk(relaxed = true)
        every { activity.findViewById<FrameLayout>(R.id.yourAnswerSection) } returns mockk(relaxed = true)
    }

    @Test
    fun `starts polling for when the question is revealed`() {

        val mPoller = mockk<Poller>(relaxed = true)
        val mPollerFactory = mockk<PollerFactory>()
        val requestFactory: StringRequestFactory = mockk(relaxed = true)
        val intentHelper = createMockIntentHelper("a-quiz-id")

        val getStageRequest: StringRequest = mockk("getStageRequest")

        every {
            mPollerFactory.create(any())
        } returns mPoller

        every {
            requestFactory.create(
                Request.Method.GET,
                API_URL + "stage?quizId=a-quiz-id",
                any(),
                any()) } returns getStageRequest

        val activity = createRevealAnswersActivity(pollerFactory = mPollerFactory, intentHelper = intentHelper, requestFactory = requestFactory)
        activity.innerOnCreate()


        verify {
            requestFactory.create(
                Request.Method.GET,
                API_URL + "stage?quizId=a-quiz-id",
                any(),
                any()) }

        verify { mPoller.start(listOf(getStageRequest)) }

    }

    @Test
    fun `stops polling and opens next question when we're up to the next question`() {
        val mPoller = mockk<Poller>(relaxed = true)
        val mPollerFactory = mockk<PollerFactory>(relaxed = true)
        every { mPollerFactory.create(any()) } returns mPoller
        val intentFactory: IntentFactory = mockk(relaxed = true)

        val questionActivityIntent: Intent = mockk("questionActivityIntent", relaxed = true)
        every { intentFactory.create(any(), QuestionActivity::class.java)  } returns questionActivityIntent

        val intentHelper = createMockIntentHelper(stage = 0)

        val activity = createRevealAnswersActivity(pollerFactory = mPollerFactory, intentFactory = intentFactory, intentHelper = intentHelper)
        activity.innerOnCreate()

        val response = StatusResponse(1, false)
        activity.getOnGetStage().onResponse(Klaxon().toJsonString(response))

        verify { mPoller.stop() }
        verify {activity.startActivity(questionActivityIntent)}
    }

    @Test
    fun `opens the question we're up to` () {

        val intentFactory: IntentFactory = mockk(relaxed = true)

        val questionActivityIntent: Intent = mockk("questionActivityIntent", relaxed = true)
        every { intentFactory.create(any(), QuestionActivity::class.java)  } returns questionActivityIntent

        val intentHelper = createMockIntentHelper(stage = 0)

        val activity = createRevealAnswersActivity( intentFactory = intentFactory, intentHelper = intentHelper)
        activity.innerOnCreate()

        val response = StatusResponse(7, false)
        activity.getOnGetStage().onResponse(Klaxon().toJsonString(response))

        verify { questionActivityIntent.putExtra("STAGE", 7) }
        verify {activity.startActivity(questionActivityIntent)}
    }

    @Test
    fun `doesn't stops polling or open next question if we're still on the same question`() {
        val mPoller = mockk<Poller>(relaxed = true)
        val mPollerFactory = mockk<PollerFactory>(relaxed = true)
        every { mPollerFactory.create(any()) } returns mPoller
        val intentFactory: IntentFactory = mockk(relaxed = true)

        val questionActivityIntent: Intent = mockk("questionActivityIntent")
        every { intentFactory.create(any(), QuestionActivity::class.java)  } returns questionActivityIntent

        val intentHelper = createMockIntentHelper(stage = 0)

        val activity = createRevealAnswersActivity(pollerFactory = mPollerFactory, intentFactory = intentFactory, intentHelper = intentHelper)
        activity.innerOnCreate()

        val response = StatusResponse(0, true)
        activity.getOnGetStage().onResponse(Klaxon().toJsonString(response))

        verify (exactly = 0){ mPoller.stop() }
        verify (exactly = 0){activity.startActivity(any())}
    }



    @Test
    fun `requests the answer for this question of this quiz`(){

        val mRequestFactory = mockk<StringRequestFactory>(relaxed = true)


        val intentHelper = createMockIntentHelper(stage= 1, quizId = "a-quiz-id", playerName = "a-name")
        val activity = createRevealAnswersActivity(requestFactory = mRequestFactory, intentHelper = intentHelper)

        val mListener = mockk<Response.Listener<String>>("response listener")
        every { activity.getResponseListener()} returns mListener


        activity.innerOnCreate()

        verify { mRequestFactory.create(
            Request.Method.GET,
            match { it.endsWith("/correctAnswer?quizId=a-quiz-id&questionNumber=1&playerName=a-name&isHost=false") },
            mListener,
            any()) }

    }

    @Test
    fun `requests the answer for this question of this quiz -- as host`(){

        val mRequestFactory = mockk<StringRequestFactory>(relaxed = true)


        val intentHelper = createMockIntentHelper(stage= 1, quizId = "a-quiz-id", playerName = "a-name", isHost = true)
        val activity = createRevealAnswersActivity(requestFactory = mRequestFactory, intentHelper = intentHelper)

        val mListener = mockk<Response.Listener<String>>("response listener")
        every { activity.getResponseListener()} returns mListener


        activity.innerOnCreate()

        verify { mRequestFactory.create(
            Request.Method.GET,
            match { it.endsWith("/correctAnswer?quizId=a-quiz-id&questionNumber=1&playerName=a-name&isHost=true") },
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

    @Test
    fun `when I am host -- next question button is visible`() {
        val intentHelper = createMockIntentHelper(isHost = true)

        val activity = createRevealAnswersActivity(intentHelper = intentHelper)
        val mHostSection = mockk<FrameLayout>(relaxed = true)
        every { activity.findViewById<FrameLayout>(R.id.hostSection) } returns mHostSection
        activity.innerOnCreate()

        verify{ mHostSection.visibility = View.VISIBLE }

    }

    @Test
    fun `when I am host -- hides the your answer section`() {
        val intentHelper = createMockIntentHelper(isHost = true)

        val activity = createRevealAnswersActivity(intentHelper = intentHelper)
        val mYourAnswerSection = mockk<FrameLayout>(relaxed = true)
        every { activity.findViewById<FrameLayout>(R.id.yourAnswerSection) } returns mYourAnswerSection
        activity.innerOnCreate()

        verify{ mYourAnswerSection.visibility = View.GONE }
    }

    @Test
    fun `when I click next question - it tells api to go to the next question, from the current question`() {

        val mRequestFactory = mockk<StringRequestFactory>(relaxed = true)


        val intentHelper = createMockIntentHelper(stage= 1, quizId = "a-quiz-id", playerName = "a-name")
        val activity = createRevealAnswersActivity(requestFactory = mRequestFactory, intentHelper = intentHelper)

        activity.innerOnCreate()
        activity.onClickNextQuestion(mockk())

        verify { mRequestFactory.create(
            Request.Method.PUT,
            match { it.endsWith("/nextQuestion?quizId=a-quiz-id&currentQuestion=1") },
            any(),
            any()) }

    }
}
