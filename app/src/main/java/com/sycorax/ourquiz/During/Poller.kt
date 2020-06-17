package com.sycorax.ourquiz.During

import android.content.Context
import android.os.Handler
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class Poller(context: Context) {
    /*
        On start it adds all the requests.
        Once all the request are complete, it waits a little while, then re-adds them.

        On stop it no longer re-adds the requests after they are completed.
     */

    private var pendingRequests = 0
    private var requests: List<StringRequest> = listOf()

    private val queue: RequestQueue
    init {
        queue =  Volley.newRequestQueue(context)
    }

    private fun waitAndThenDo () {
        pendingRequests = requests.count()
        Handler().postDelayed(
            {
                requests.forEach {queue.add(it)}
            }, 1000)

    }

    private fun onRequestComplete(){

        pendingRequests-=1
        //Log.wtf("aaa", "pendingRequests: "+ pendingRequests)
        if (pendingRequests <=0){
            waitAndThenDo()
        }
    }

    private val requestFinishedListener: RequestQueue.RequestFinishedListener<Any> =
        RequestQueue.RequestFinishedListener {
            onRequestComplete()
        }


    fun resume() {
        waitAndThenDo()
        queue.addRequestFinishedListener(requestFinishedListener)

    }

    fun start(requests: List<StringRequest>) {
        this.requests = requests
        waitAndThenDo()
    }

    fun stop() {
        queue.removeRequestFinishedListener(requestFinishedListener)
    }
}
