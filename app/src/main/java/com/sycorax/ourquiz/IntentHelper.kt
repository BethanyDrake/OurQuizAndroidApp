package com.sycorax.ourquiz

import android.content.Intent

class IntentHelper {
    fun getQuizId(intent: Intent) : String {
        return intent.extras?.get("QUIZ_ID").toString()
    }


    fun copyExtrasFromIntent(oldIntent: Intent, newIntent: Intent){
        newIntent.putExtra("QUIZ_ID", getQuizId(oldIntent))
        newIntent.putExtra("STAGE", getStage(oldIntent))
        newIntent.putExtra("HOST", getAmHost(oldIntent))
        newIntent.putExtra("PLAYER_NAME", getPlayerName(oldIntent))
    }

    fun getAmHost(intent: Intent): Boolean{
        val host = intent.extras?.get("HOST")
        if (host is Boolean) return host;
        return false
    }

    fun getPlayerName(intent: Intent): String {
        return intent.extras?.get("PLAYER_NAME").toString()
    }


    fun getStage(intent: Intent) : Int {
        val stage = intent.extras?.get("STAGE")
        if (stage is Int) return stage
        return -1
    }

    fun getCurrentQuestion(intent: Intent) : Int {
        return getStage(intent)
    }
}

