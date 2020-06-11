package com.sycorax.ourquiz

import android.content.Intent
import android.widget.TextView
import com.beust.klaxon.Klaxon
import io.mockk.*
import org.junit.Assert
import org.junit.Test

class QuestionActivityTest {
    @Test
    fun `displays question text`() {

        val activity = spyk(QuestionActivity(mockk(relaxed = true), mockk(relaxed=true)))
        every { activity.intent } returns mockk(relaxed = true)
        val mQuestionTextView: TextView = mockk(relaxed = true)

        val capturingSlot = CapturingSlot<CharSequence>()
        every {mQuestionTextView.setText(capture(capturingSlot)) } returns Unit


        every { activity.findViewById<TextView>(any()) } returns mockk(relaxed = true)
        every { activity.findViewById<TextView>(R.id.questionText) } returns mQuestionTextView
        activity.innerOnCreate()

        val fetchedQuestion = Question("A question?", "someone")
        val response = Klaxon().toJsonString(fetchedQuestion)


        activity.getListener().onResponse(response)

        assert(capturingSlot.isCaptured)
        assert(capturingSlot.captured == "A question?")
    }

    @Test
    fun `displays possible answers`() {

        val activity = spyk(QuestionActivity(mockk(relaxed = true), mockk(relaxed=true)))
        every { activity.intent } returns mockk(relaxed = true)
        val mOptionAView: TextView = mockk(relaxed = true)
        val mOptionBView: TextView = mockk(relaxed = true)
        val mOptionCView: TextView = mockk(relaxed = true)
        val mOptionDView: TextView = mockk(relaxed = true)


        every { activity.findViewById<TextView>(any()) } returns mockk(relaxed = true)


        val capturingSlotA = CapturingSlot<CharSequence>()
        every { activity.findViewById<TextView>(R.id.A) } returns mOptionAView
        every {mOptionAView.setText(capture(capturingSlotA)) } returns Unit

        val capturingSlotB = CapturingSlot<CharSequence>()
        every { activity.findViewById<TextView>(R.id.B) } returns mOptionBView
        every {mOptionBView.setText(capture(capturingSlotB)) } returns Unit

        val capturingSlotC = CapturingSlot<CharSequence>()
        every { activity.findViewById<TextView>(R.id.C) } returns mOptionCView
        every {mOptionCView.setText(capture(capturingSlotC)) } returns Unit

        val capturingSlotD = CapturingSlot<CharSequence>()
        every { activity.findViewById<TextView>(R.id.D) } returns mOptionDView
        every {mOptionDView.setText(capture(capturingSlotD)) } returns Unit


        activity.innerOnCreate()

        val answers = listOf("option 1", "option 2", "option 3", "option 4")
        val fetchedQuestion = Question("A question?", "someone", answers)
        val response = Klaxon().toJsonString(fetchedQuestion)


        activity.getListener().onResponse(response)

        Assert.assertTrue(capturingSlotA.isCaptured)
        Assert.assertEquals("option 1", capturingSlotA.captured)

        Assert.assertTrue(capturingSlotB.isCaptured)
        Assert.assertEquals("option 2", capturingSlotB.captured)

        Assert.assertTrue(capturingSlotC.isCaptured)
        Assert.assertEquals("option 3", capturingSlotC.captured)

        Assert.assertTrue(capturingSlotD.isCaptured)
        Assert.assertEquals("option 4", capturingSlotD.captured)
    }

    @Test
    fun `on select -- opens waiting screen -- with extras`() {
        val mIntentFactory = mockk<IntentFactory>()
        val mIntent = mockk<Intent>(relaxed = true)

        every {  mIntentFactory.create(any(), WaitingForPlayersActivity::class.java) } returns mIntent
        val activity = spyk(QuestionActivity(mockk(relaxed = true), mockk(relaxed=true), mIntentFactory))

        every { activity.startActivity(any()) } returns Unit

        activity.onSelectAnswer(mockk())
        verify { activity.startActivity(mIntent) }
    }
}
