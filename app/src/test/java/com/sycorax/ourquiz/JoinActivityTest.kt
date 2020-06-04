package com.sycorax.ourquiz

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.view.TextureView
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import io.mockk.*
import org.junit.Assert
import org.junit.Test

class JoinActivityTest {
    private fun createMockEditText(text:String) :EditText{
        val mEditText = mockk<EditText>(relaxed = true)
        val mEditTextEditable = mockk<Editable>(relaxed = true)
        every { mEditTextEditable.toString() } returns text
        every { mEditText.text } returns mEditTextEditable

        return mEditText
    }



    fun setUpJoinActivity(mStringRequestFactory: StringRequestFactory = mockk(relaxed = true), mQueue: RequestQueue = mockk(relaxed = true), mStringRequest: StringRequest = mockk(relaxed = true), intentFactory:IntentFactory = IntentFactory()):JoinActivity {

        class MQueueFactory(private val queue: RequestQueue) : VolleyRequestQueueFactory() {
            override fun create(context: Context): RequestQueue {
                return queue
            }
        }

        val mockQuizIdField = createMockEditText("a-quiz-id")
        val mockNameField = createMockEditText("my-name")

        class SpyJoinActivity(queueFactory: VolleyRequestQueueFactory, stringRequestFactory: StringRequestFactory, intentFactory: IntentFactory) : JoinActivity(queueFactory, stringRequestFactory, intentFactory) {
            override fun <T : View> findViewById(id: Int): T {
                println("finding view")
                return when (id) {
                    R.id.textView -> mockk<TextView>(relaxed = true) as T
                    R.id.editText -> mockQuizIdField as T
                    R.id.nameField -> mockNameField as T
                    else -> mockk<View>(relaxed = true) as T
                }
            }
        }


        every {  mStringRequestFactory.create(any(), any(), any(), any()) } returns mStringRequest
        val joinActivity = SpyJoinActivity(MQueueFactory(mQueue), mStringRequestFactory, intentFactory )
        return joinActivity
    }

    @Test
    fun join_createsARequestDetails_andAddsItToTheQueue() {

        val mQueue = mockk<RequestQueue>(relaxed = true)
        val mStringRequest = mockk<StringRequest>(relaxed = true)
        val mStringRequestFactory = mockk<StringRequestFactory>(relaxed =true);


        val joinActivity = setUpJoinActivity(mStringRequestFactory, mQueue, mStringRequest)


        joinActivity.join(mockk<View>(relaxed = true))


        var expectedMethod = Request.Method.GET
        var expectedURL = "http://10.0.2.2:8090/join?quizId=a-quiz-id&name=my-name"


        verify(atMost = 1)  { mStringRequestFactory.create(expectedMethod, expectedURL, any(), any()) }

        verify(atMost = 1) { mQueue.add(mStringRequest) }


    }

    @Test
    fun join_whenResponseIsOK_opensSubmitQuesionActivity (){

        val mIntentFactory = mockk<IntentFactory>()
        val joinActivity = spyk(setUpJoinActivity(intentFactory=mIntentFactory))

        every {joinActivity.startActivity(any())} returns Unit
        val createdIntentForClass = CapturingSlot<Class<Any>>()
        every {
            mIntentFactory.create(any(), capture(createdIntentForClass))
        } returns mockk(createdIntentForClass.toString(), relaxed = true)


        joinActivity.join(View(joinActivity))
        joinActivity.onResponse("OK")

        verify {
            joinActivity.startActivity(any())
        }

    }

}
