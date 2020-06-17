package com.sycorax.ourquiz

import android.content.Intent
import android.os.Bundle
import io.mockk.every
import io.mockk.mockk

fun createMockIntentWithExtras(extras: Map<String, Any>): Intent {
    val mIntent: Intent = mockk(relaxed = true)
    val mExtras: Bundle = mockk(relaxed = true)
    extras.forEach{key, value ->
        every { mExtras.get(key) } returns value
    }

    every { mIntent.extras } returns mExtras
    return mIntent
}



