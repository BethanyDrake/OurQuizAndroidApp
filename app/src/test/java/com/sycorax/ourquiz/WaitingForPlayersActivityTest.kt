package com.sycorax.ourquiz

import android.content.Intent
import android.widget.LinearLayout
import io.mockk.*
import org.junit.Test

class WaitingForPlayersActivityTest {

    @Test
    fun `starts polling`() {

        val activity = spyk(WaitingForPlayersActivity(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)))
        every { activity.intent } returns mockk(relaxed = true)
        every { activity.findViewById<LinearLayout>(any()) } returns mockk(relaxed = true)
        activity.innerOnCreate()
    }


    @Test
    fun `doesnt stop while when api returns a stage equal to yours`() {

        val mPoller:Poller = mockk(relaxed = true)
        val activity = spyk(WaitingForPlayersActivity(mPoller, mockk(relaxed = true), mockk(relaxed = true)))

        val extras = mapOf(Pair("STAGE", -1))

        val mIntent = createMockIntentWithExtras(extras)
        every { activity.intent } returns mIntent
        every { activity.findViewById<LinearLayout>(any()) } returns mockk(relaxed = true)
        activity.getHasStartedRequestListener().onResponse("-1")

        verify { mPoller wasNot Called }

    }


    @Test
    fun `stops and opens next activity when api returns a stage later than yours -- opens question screen`() {

        val mPoller:Poller = mockk(relaxed = true)

        val mIntentFactory = mockk<IntentFactory>(relaxed = true)
        val activity = spyk(WaitingForPlayersActivity(mPoller, mockk(relaxed = true), mIntentFactory))
        val extras = mapOf(Pair("STAGE", -1), Pair("QUIZ_ID", "whatever"),  Pair("HOST", false), Pair("PLAYER_NAME", "my name"))
        val mIntent = createMockIntentWithExtras(extras)
        every { activity.intent } returns mIntent
        every { activity.startActivity(any()) } returns Unit
        every { activity.findViewById<LinearLayout>(any()) } returns mockk(relaxed = true)

        activity.getHasStartedRequestListener().onResponse("0")

        verify { mPoller.stop() }
        verify { activity.startActivity(any()) }

        verify { mIntentFactory.create(any(), QuestionActivity::class.java) }
    }

    @Test
    fun `when I am host -- and stage returned is later than mine -- reopen waiting screen as next stage`() {

        val mPoller:Poller = mockk(relaxed = true)
        val mIntentFactory = mockk<IntentFactory>(relaxed = true)
        val mCreatedIntend : Intent = mockk(relaxed = true)
        every {  mIntentFactory.create(any(), WaitingForPlayersActivity::class.java) } returns mCreatedIntend;
        val activity = spyk(WaitingForPlayersActivity(mPoller, mockk(relaxed = true), mIntentFactory))
        val extras = mapOf(Pair("STAGE", -1), Pair("QUIZ_ID", "whatever"), Pair("HOST", true))
        val mIntent = createMockIntentWithExtras(extras)
        every { activity.intent } returns mIntent
        every { activity.startActivity(any()) } returns Unit
        every { activity.findViewById<LinearLayout>(any()) } returns mockk(relaxed = true)

        activity.getHasStartedRequestListener().onResponse("0")

        verify { mPoller.stop() }
        verify { activity.startActivity(any()) }
        verify { mIntentFactory.create(any(), WaitingForPlayersActivity::class.java) }
        verify { mCreatedIntend.putExtra("STAGE", 0) }


    }


}
