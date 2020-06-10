package com.sycorax.ourquiz

import android.widget.LinearLayout
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test

class WaitingForPlayersActivityTest {

    @Test
    fun `starts polling`() {

        val activity = spyk(WaitingForPlayersActivity(mockk(relaxed = true), mockk(relaxed = true)))
        every { activity.intent } returns mockk(relaxed = true)
        every { activity.findViewById<LinearLayout>(any()) } returns mockk(relaxed = true)
        activity.innerOnCreate()
    }


}
