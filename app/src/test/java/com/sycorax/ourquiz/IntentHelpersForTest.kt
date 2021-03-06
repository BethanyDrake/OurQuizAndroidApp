package com.sycorax.ourquiz

import android.content.Intent
import android.os.Bundle
import io.mockk.every
import io.mockk.mockk

fun createMockIntentHelper(
    quizId: String = "",
    playerName: String = "",
    isHost: Boolean = false,
    stage: Int = -1
): IntentHelper {
    val mIntentHelper = mockk<IntentHelper>(relaxed = true)
    every { mIntentHelper.getAmHost(any()) } returns isHost
    every {
        mIntentHelper.getCurrentQuestion(any())
    } returns stage
    every {
        mIntentHelper.getStage(any())
    } returns stage
    every {
        mIntentHelper.getPlayerName(any())
    } returns playerName
    every {
        mIntentHelper.getQuizId(any())
    } returns quizId
    return mIntentHelper
}


fun createMockIntentWithExtras(extras: Map<String, Any>): Intent {
    val mIntent: Intent = mockk(relaxed = true)
    val mExtras: Bundle = mockk(relaxed = true)
    extras.forEach { key, value ->
        every { mExtras.get(key) } returns value
    }

    every { mIntent.extras } returns mExtras
    return mIntent
}



