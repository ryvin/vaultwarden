package com.vw

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@RunWith(AndroidJUnit4::class)
class AliveInstrumentedTest {
    @Test
    fun serverAlive() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = android.content.Intent(appContext, ServerService::class.java)
        appContext.startService(intent)
        // Wait some time for server startup
        Thread.sleep(4000)

        // Create a trust manager that does not validate certificate chains (unsafe, for testing only!)
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // Still bypass hostname verification for 127.0.0.1
            .build()

        val req = Request.Builder().url("https://127.0.0.1:8087/alive").build()
        val resp = client.newCall(req).execute()
        assertEquals(200, resp.code)
        appContext.stopService(intent)
    }
}
