package com.sycorax.ourquiz

import android.content.Context
import android.content.Intent
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class IntentFactory {
    fun <T>create(context: Context, activityClass: Class<T>): Intent {
        return Intent(context, activityClass)
    }
}
class VolleyRequestQueueFactory {
    fun create(context: Context): RequestQueue {
        return Volley.newRequestQueue(context)
    }
}

open class StringRequestFactory {
    fun create(method: Int, url:String, responseListener: Response.Listener<String>, errorListener: Response.ErrorListener  ): StringRequest {
        return StringRequest(method, url, responseListener, errorListener)
    }
}
