package com.sycorax.ourquiz

import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test

class SubmitQuestionActivityTest {

    @Test
    fun submit_submitsQuestionDetails_andDetailsFromExtras() {
        val submitQuestionActivity = spyk(SubmitQuestionActivity(mockk(relaxed = true), mockk(relaxed =true)))

        every {  submitQuestionActivity.findViewById<EditText>(R.id.questionText) } returns mockk(relaxed = true)
        every {submitQuestionActivity.intent} returns mockk(relaxed =true)
        submitQuestionActivity.submit(View(submitQuestionActivity))

    }
}
