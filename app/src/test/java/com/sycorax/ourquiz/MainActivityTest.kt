package com.sycorax.ourquiz

import android.content.Context
import android.content.Intent
import android.view.View
import org.junit.Assert
import org.junit.Test

class MainActivityTest {

    class MIntentFactory : IntentFactory() {
        override fun <T> create(context: Context, activityClass: Class<T>): Intent {
            return MockIntent(activityClass.simpleName)
        }
    }
    class SpyMainActivity : MainActivity(MIntentFactory()) {
        var startedActivity = false
        var startedWith:Intent? = null
        override fun startActivity(intent: Intent){
            startedActivity = true
            startedWith = intent
        }
    }

    class MockIntent(private val tag:String) : Intent() {
        override fun toString(): String {
            return "Intent (mock) $tag"
        }

        override fun equals(other: Any?): Boolean {
            return other.toString() == toString()
        }

        override fun hashCode(): Int {
            return tag.hashCode()
        }
    }

    @Test
    fun join_opensJoinActivity() {
        val mainActivity = SpyMainActivity()

        mainActivity.join(View(mainActivity))

        val expectedIntent = MIntentFactory().create(mainActivity, JoinActivity::class.java)
        val startedWithIntent = mainActivity.startedWith
        Assert.assertTrue(mainActivity.startedActivity)
        Assert.assertEquals(expectedIntent, startedWithIntent)
    }

    @Test
    fun host_opensHostActivity() {
        val mainActivity = SpyMainActivity()

        mainActivity.host(View(mainActivity))

        val expectedIntent = MIntentFactory().create(mainActivity, HostActivity::class.java)
        val startedWithIntent = mainActivity.startedWith
        Assert.assertTrue(mainActivity.startedActivity)
        Assert.assertEquals(expectedIntent, startedWithIntent)
    }
}
