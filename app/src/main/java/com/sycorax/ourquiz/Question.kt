package com.sycorax.ourquiz
data class Question(val questionText:String, val submittedBy: String, val answers: List<String> = listOf(), val correctQuestionId: Int = 0)
