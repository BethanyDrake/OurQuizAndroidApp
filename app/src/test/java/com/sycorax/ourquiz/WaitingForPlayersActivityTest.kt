package com.sycorax.ourquiz

import android.content.Intent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.android.volley.Request
import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.WaitingForPlayers.Poller
import com.sycorax.ourquiz.WaitingForPlayers.PollerFactory
import com.sycorax.ourquiz.WaitingForPlayers.StatusResponse
import com.sycorax.ourquiz.WaitingForPlayers.WaitingForPlayersActivity
import io.mockk.*
import org.junit.Test

class WaitingForPlayersActivityTest {

    private fun createWaitingForPlayersActivity(
        mPollerFactory: PollerFactory = mockk(relaxed = true),
        mIntentHelper: IntentHelper = createMockIntentHelper(),
        mIntentFactory: IntentFactory = mockk(relaxed = true),
        mRequestFactory: StringRequestFactory = mockk(relaxed = true),
        mQueueFactory: VolleyRequestQueueFactory = mockk(relaxed = true)
        ): WaitingForPlayersActivity {

        val activity = spyk(
            WaitingForPlayersActivity(
                mPollerFactory,
                mRequestFactory,
                mIntentFactory,
                mQueueFactory,
                mIntentHelper
            )
        )
        every { activity.intent } returns mockk(relaxed = true)
        every { activity.startActivity(any()) } returns Unit
        addMockViews(activity)
        activity.innerOnCreate()
        return activity

    }

    private fun addMockViews(activitySpy: WaitingForPlayersActivity) {
        every { activitySpy.findViewById<LinearLayout>(any()) } returns mockk(relaxed = true)
        every { activitySpy.findViewById<FrameLayout>(R.id.hostSection) } returns mockk(relaxed = true)
        every { activitySpy.findViewById<Button>(R.id.startQuizButton) } returns mockk(relaxed = true)
        every { activitySpy.findViewById<Button>(R.id.revealAnswerButton) } returns mockk(relaxed = true)
    }

    @Test
    fun `starts polling`() {

        val mPoller = mockk<Poller>(relaxed = true)
        val mPollerFactory = mockk<PollerFactory>()
        every {
            mPollerFactory.create(any())
        } returns mPoller
        createWaitingForPlayersActivity(mPollerFactory)
        verify { mPoller.start(any()) }
    }


    @Test
    fun `doesnt stop while when api returns a stage equal to yours`() {

        val mPoller: Poller = mockk(relaxed = true)

        val mPollerFactory = mockk<PollerFactory>()
        every {
            mPollerFactory.create(any())
        } returns mPoller
        val mIntentHelper = createMockIntentHelper(stage= -1)
        val activity = createWaitingForPlayersActivity(mPollerFactory, mIntentHelper)
//
//        val extras = mapOf(Pair("STAGE", -1))

        val notStartedResponse = Klaxon().toJsonString(StatusResponse(-1, false))
        activity.getHasStartedRequestListener().onResponse(notStartedResponse)

        verify(exactly = 0){ mPoller.stop() }

    }

    @Test
    fun `stops and reveals the answer when question is revealed`() {

        val mPoller: Poller = mockk(relaxed = true)

        val mPollerFactory = mockk<PollerFactory>()
        every {
            mPollerFactory.create(any())
        } returns mPoller
        val mIntentFactory = mockk<IntentFactory>(relaxed = true)
        val mIntentHelper = createMockIntentHelper(stage=0)
        val activity = createWaitingForPlayersActivity(mPollerFactory, mIntentHelper, mIntentFactory)

        val justStartedResponse = Klaxon().toJsonString(StatusResponse(0, true))
        activity.getHasStartedRequestListener().onResponse(justStartedResponse)

        verify { mPoller.stop() }
        verify { activity.startActivity(any()) }

        verify { mIntentFactory.create(any(), RevealAnswerActivity::class.java) }
    }


    @Test
    fun `stops and opens next activity when api returns a stage later than yours -- opens question screen`() {

        val mPoller: Poller = mockk(relaxed = true)

        val mIntentFactory = mockk<IntentFactory>(relaxed = true)

        val mPollerFactory = mockk<PollerFactory>()
        every {
            mPollerFactory.create(any())
        } returns mPoller

        val mIntentHelper = createMockIntentHelper(stage = -1)

        val activity = createWaitingForPlayersActivity(mPollerFactory, mIntentHelper, mIntentFactory = mIntentFactory)
        val justStartedResponse = Klaxon().toJsonString(StatusResponse(0, false))

        activity.getHasStartedRequestListener().onResponse(justStartedResponse)


        verify { mPoller.stop() }
        verify { activity.startActivity(any()) }

        verify { mIntentFactory.create(any(), QuestionActivity::class.java) }
    }

    @Test
    fun `reveal answer -- tells api to reveal the current questionanswer`() {
        val mRequestFactory: StringRequestFactory = mockk(relaxed = true)

        val mIntentHelper = createMockIntentHelper("a-quiz-id", "my name", false, 0)
        val activity = createWaitingForPlayersActivity(mIntentHelper= mIntentHelper, mRequestFactory = mRequestFactory)

        activity.revealAnswer(mockk())

        verify {
            mRequestFactory.create(
                Request.Method.PUT,
                match { it.endsWith("revealQuestion?quizId=a-quiz-id&questionNumber=0") },
                any(),
                any()
            )
        }
    }


    @Test
    fun `when I am host -- and stage returned is later than mine -- reopen waiting screen as next stage`() {

        val mPoller: Poller = mockk(relaxed = true)
        val mIntentFactory = mockk<IntentFactory>(relaxed = true)
        val mCreatedIntend: Intent = mockk(relaxed = true)
        every { mIntentFactory.create(any(), WaitingForPlayersActivity::class.java) } returns mCreatedIntend;

        val mPollerFactory = mockk<PollerFactory>()
        every {
            mPollerFactory.create(any())
        } returns mPoller

        val mIntentHelper = createMockIntentHelper(stage = -1, isHost = true)
        val activity = createWaitingForPlayersActivity(mPollerFactory,mIntentHelper, mIntentFactory)

        val nextQuestionResponse = Klaxon().toJsonString(StatusResponse(0, false))
        activity.getHasStartedRequestListener().onResponse(nextQuestionResponse)

        verify { mPoller.stop() }
        verify { activity.startActivity(any()) }
        verify { mIntentFactory.create(any(), WaitingForPlayersActivity::class.java) }
        verify { mCreatedIntend.putExtra("STAGE", 0) }

    }
}
