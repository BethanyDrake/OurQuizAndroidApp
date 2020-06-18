package com.sycorax.ourquiz.During

import android.content.Context
import android.os.Handler
import android.util.Log
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
    private var name:String

    private var active = false;

    companion object {
        var count:Int = 0;
    }
    private val queue: RequestQueue
    init {
        queue =  Volley.newRequestQueue(context)
        name = "Poller #" + count + " "
        count++
    }


    private fun waitAndThenDo () {
        if (!active) return
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
        //Log.wtf("POLLER", name +"resumed")
        active = true
        //queue.addRequestFinishedListener(requestFinishedListener)
        waitAndThenDo()

    }

    fun start(requests: List<StringRequest>) {
        this.requests = requests
        //Log.wtf("POLLER", name +"started polling " + requests.count() + " requests")
        active=true;
        waitAndThenDo()
        queue.addRequestFinishedListener(requestFinishedListener)
    }

    fun stop() {
        //Log.wtf("POLLER", name +"stopped polling")

        queue.cancelAll {true}
        active = false
        //queue.removeRequestFinishedListener(requestFinishedListener)
    }
}
