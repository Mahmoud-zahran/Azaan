package com.example.azaan.core.notification

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class AthanPlayerTest {

    @Test
    fun testPlayAndStopAthan() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val athanPlayer = AthanPlayer(appContext)
        
        try {
            athanPlayer.playAthan()
            
            // Wait a bit to ensure it started
            Thread.sleep(1000)
            
            // Test stopping
            athanPlayer.stopAthan()
        } catch (e: Exception) {
            fail("AthanPlayer crashed: ${e.message}")
        }
    }
}