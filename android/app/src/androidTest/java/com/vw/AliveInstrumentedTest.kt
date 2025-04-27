package com.vw

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AliveInstrumentedTest {
    @Test
    fun serverAlive() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = android.content.Intent(appContext, ServerService::class.java)
        appContext.startService(intent)
        // Wait some time for server startup
        Thread.sleep(4000)
        val client = OkHttpClient.Builder().hostnameVerifier { _, _ -> true }.build()
        val req = Request.Builder().url("https://127.0.0.1:8087/alive").build()
        val resp = client.newCall(req).execute()
        assertEquals(200, resp.code)
        appContext.stopService(intent)
    }
}
