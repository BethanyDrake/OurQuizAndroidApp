package com.sycorax.ourquiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.beust.klaxon.Klaxon
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
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


    @Test
    fun submit_submitsQuestionDetails_andDetailsFromExtras() {
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
}
