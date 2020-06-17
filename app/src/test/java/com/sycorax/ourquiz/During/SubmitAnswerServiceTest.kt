package com.sycorax.ourquiz.During

import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.sycorax.ourquiz.Before.RequestWithBody
import com.sycorax.ourquiz.Before.RequestWithBodyFactory
import com.sycorax.ourquiz.VolleyRequestQueueFactory
import io.mockk.*
import org.junit.Test

class SubmitAnswerServiceTest {

    @Test
    fun `invokes callback when api returns OK`(){
        val service =
            SubmitAnswerService(mockk(relaxed = true), mockk(relaxed = true))
        val callback = mockk<()->Unit>(relaxed = true)
        val body = SubmitAnswerBody("", "", 0, 0)
        service.submitAnswer(mockk(), callback, body)
        service.getListener().onResponse("OK")

        verify { callback() }
    }

    @Test
    fun `adds request to the queue`(){
        val queueFactory = mockk<VolleyRequestQueueFactory>(relaxed = true)
        val mQueue = mockk<RequestQueue>(relaxed=true)
        val captureAddedRequest = CapturingSlot<StringRequest>()
        val createdRequest = mockk<RequestWithBody>("created request")

        every {queueFactory.create(any())} returns mQueue

        val requestWithBodyFactory = mockk<RequestWithBodyFactory>(relaxed = true)
        every {requestWithBodyFactory.create(any(), any(), any(), any(), any()) } returns createdRequest

        val service = SubmitAnswerService(queueFactory, requestWithBodyFactory)
        val callback = mockk<()->Unit>(relaxed = true)
        val body = SubmitAnswerBody("", "", 0, 0)
        service.submitAnswer(mockk(), callback, body)

        //added a request the queue
        verify { mQueue.add(createdRequest) }


    }

}
