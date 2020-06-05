package com.sycorax.ourquiz

import android.content.Context
import android.content.Intent
import android.view.View
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class MainActivityTest {

    @Test
    fun join_opensJoinActivity() {
        val mIntent = mockk<Intent>()
        val mIntentFactory = mockk<IntentFactory>()
        every { mIntentFactory.create(any(), JoinActivity::class.java) } returns mIntent
        val mainActivity = spyk(MainActivity(mIntentFactory))
        every {mainActivity.startActivity(any())} returns Unit

        mainActivity.join(View(mainActivity))

        verify {
            mainActivity.startActivity(mIntent)
        }
    }

    @Test
    fun host_opensHostActivity() {
        val mIntent = mockk<Intent>()
        val mIntentFactory = mockk<IntentFactory>()
        every { mIntentFactory.create(any(), HostActivity::class.java) } returns mIntent
        val mainActivity = spyk(MainActivity(mIntentFactory))
        every {mainActivity.startActivity(any())} returns Unit

        mainActivity.host(View(mainActivity))

        verify {
            mainActivity.startActivity(mIntent)
        }

    }
}
