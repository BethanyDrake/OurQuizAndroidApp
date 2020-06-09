package com.sycorax.ourquiz

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import org.w3c.dom.Text

class HostActivityTest {

    @Test
    fun `when you press host -- it sends a host request to the api -- with the quiz id`() {
        val mQueue = mockk<RequestQueue>(relaxed= true)
        val mRequestQueueFactory = mockk<VolleyRequestQueueFactory>(relaxed = true)
        every {mRequestQueueFactory.create(any()) } returns mQueue
        val mRequestFactory = mockk<StringRequestFactory>()

        every { mRequestFactory.create(any(), "http://10.0.2.2:8090/create?quizId=a-quiz-id", any(), any()) } returns mockk()

        val hostActivity = spyk(HostActivity(mRequestQueueFactory, mRequestFactory))

        every { hostActivity.findViewById<TextView>(any()) } returns mockk(relaxed = true)
        every { hostActivity.findViewById<EditText>(R.id.editText) } returns createMockEditText("a-quiz-id")

        hostActivity.host(mockk())
        verify {mQueue.add(any<StringRequest>())}
    }
}
