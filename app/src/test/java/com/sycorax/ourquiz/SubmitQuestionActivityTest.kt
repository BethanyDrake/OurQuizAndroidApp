package com.sycorax.ourquiz

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import com.beust.klaxon.Klaxon
import io.mockk.*
import io.mockk.MockKSettings.relaxed
import org.junit.Assert
import org.junit.Test

class SubmitQuestionActivityTest {

    fun createMockIntentWithExtras(extras: Map<String, String>): Intent {
        val mIntent: Intent = mockk(relaxed = true)
        val mExtras: Bundle = mockk()
        extras.forEach{key, value ->
            every { mExtras.get(key) } returns value
        }

        every { mIntent.extras } returns mExtras
        return mIntent
    }

    fun createMockEditText(text: String): EditText {
        val textView: EditText = mockk()
        val textViewText: Editable = mockk()
        every { textViewText.toString() } returns text
        every { textView.text } returns textViewText
        return textView

    }


    @Test
    fun `submit-- submits question text`() {
        val mRequestWithBodyFactory = mockk<RequestWithBodyFactory>()

        val capturedBody:CapturingSlot<String> = CapturingSlot()
        every { mRequestWithBodyFactory.create(capture(capturedBody), any(), any(), any(), any()) } returns mockk(relaxed = true)

        val submitQuestionActivity = spyk(SubmitQuestionActivity(mockk(relaxed = true), mRequestWithBodyFactory))


        val questionTextView = createMockEditText("Question text?")
        every {  submitQuestionActivity.findViewById<EditText>(R.id.questionText) } returns questionTextView

        val mIntent: Intent =  mockk(relaxed = true)
        every {  submitQuestionActivity.intent } returns mIntent

        submitQuestionActivity.submit(View(submitQuestionActivity))

        Assert.assertTrue(capturedBody.isCaptured)
        val body: String = capturedBody.captured
        val parsedBody = Klaxon().parse<QuestionSubmissionBody>(body)

        Assert.assertNotNull(parsedBody)
        Assert.assertEquals("Question text?", parsedBody?.question?.questionText)

    }


    @Test
    fun `submit-- submits details from extras`() {
        val mRequestWithBodyFactory = mockk<RequestWithBodyFactory>()

        val capturedBody:CapturingSlot<String> = CapturingSlot()
        every { mRequestWithBodyFactory.create(capture(capturedBody), any(), any(), any(), any()) } returns mockk(relaxed = true)

        val submitQuestionActivity = spyk(SubmitQuestionActivity(mockk(relaxed = true), mRequestWithBodyFactory))

        every {  submitQuestionActivity.findViewById<EditText>(R.id.questionText) } returns mockk(relaxed = true)

        val extras = mapOf(Pair("QUIZ_ID", "a-quiz-id"), Pair("PLAYER_NAME", "my-name"))
        val mIntent: Intent = createMockIntentWithExtras(extras)
        every {  submitQuestionActivity.intent } returns mIntent


        submitQuestionActivity.submit(View(submitQuestionActivity))

        Assert.assertTrue(capturedBody.isCaptured)
        val body: String = capturedBody.captured
        val parsedBody = Klaxon().parse<QuestionSubmissionBody>(body)


        Assert.assertNotNull(parsedBody)
        Assert.assertEquals("a-quiz-id", parsedBody?.quizId)
        Assert.assertEquals("my-name", parsedBody?.question?.submittedBy)

    }

    @Test
    fun `after submit -- when it returns ok -- opens a WaitingFORPLayersActivity with extras`() {

        val intentFactory = mockk<IntentFactory>()
        every { intentFactory.create(any(), WaitingForPlayersActivity::class.java)} returns mockk(relaxed = true)
        val submitQuestionActivity = spyk(SubmitQuestionActivity(intentFactory = mockk(relaxed = true)))

        val startedActivityWithIntent = CapturingSlot<Intent>()
        every { submitQuestionActivity.startActivity(capture(startedActivityWithIntent)) } returns Unit
        val extras = mapOf(Pair("QUIZ_ID", "a-quiz-id"), Pair("PLAYER_NAME", "my-name"))
        val mIntent = createMockIntentWithExtras(extras)
        every {  submitQuestionActivity.intent } returns mIntent
        submitQuestionActivity.getRequestListener().onResponse("OK")

        Assert.assertTrue(startedActivityWithIntent.isCaptured)
        verify { startedActivityWithIntent.captured.putExtra("QUIZ_ID", "a-quiz-id") }
        verify { startedActivityWithIntent.captured.putExtra("PLAYER_NAME", "my-name") }
    }

}
