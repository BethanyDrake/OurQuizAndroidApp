package com.sycorax.ourquiz

import android.content.Context
import android.content.Intent
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

open class IntentFactory {
    open fun <T>create(context: Context, activityClass: Class<T>): Intent {
        return Intent(context, activityClass)
    }
}

open class VolleyRequestQueueFactory {
    open fun create(context: Context): RequestQueue {
        return Volley.newRequestQueue(context)
    }
}

open class StringRequestFactory {
    open fun create(method: Int, url:String, responseListener: Response.Listener<String>, errorListener: Response.ErrorListener  ): StringRequest {
        return StringRequest(method, url, responseListener, errorListener)
    }
}
