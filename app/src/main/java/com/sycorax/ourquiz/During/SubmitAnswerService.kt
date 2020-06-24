package com.sycorax.ourquiz.During

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.beust.klaxon.Klaxon
import com.sycorax.ourquiz.Before.RequestWithBodyFactory
import com.sycorax.ourquiz.VolleyRequestQueueFactory


data class SubmitAnswerBody(val quizId: String, val playerName: String, val questionNumber: Int, val answerId:Int)

class SubmitAnswerService(
    val queueFactory: VolleyRequestQueueFactory = VolleyRequestQueueFactory(),
    val requestWithBodyFactory: RequestWithBodyFactory = RequestWithBodyFactory()
) {

    fun getListener(): Response.Listener<String> {
        return Response.Listener<String> { response ->
            lastCallback()
        }
    }

    private var lastCallback: () -> Unit = {}

    fun submitAnswer(context: Context, callback: () -> Unit, body: SubmitAnswerBody) {

        val bodyJson = Klaxon().toJsonString(body)
        val url = "http://10.0.2.2:8090/submitAnswer?"
        val request = requestWithBodyFactory.create(bodyJson, Request.Method.PUT, url, getListener(), Response.ErrorListener{})
        val queue = queueFactory.create(context)
        queue.add(request)

        lastCallback = callback;



    }


}
