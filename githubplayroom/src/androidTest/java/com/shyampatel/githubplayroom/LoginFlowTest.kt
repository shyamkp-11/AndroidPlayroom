package com.shyampatel.githubplayroom

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.google.firebase.messaging.FirebaseMessaging
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


private const val APP_PACKAGE_NAME = "com.shyampatel.githubplayroom"
private const val LAUNCH_TIMEOUT = 5000L

@RunWith(AndroidJUnit4::class)
class LoginFlowTest {

    private lateinit var device: UiDevice
    private lateinit var context: Context

    @Before
    fun setUp() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = InstrumentationRegistry.getInstrumentation().targetContext

        startApp()
    }

    private fun generateFirebaseAuthToken() {
        val token = FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
        Thread.sleep(2000)
    }

    private fun startApp() {
        // Start from the home screen
        device.pressHome()

        // Wait for launcher
        val launcherPackage: String = device.launcherPackageName
        MatcherAssert.assertThat(launcherPackage, notNullValue())
        device.wait(
            Until.hasObject(By.pkg(launcherPackage).depth(0)),
            LAUNCH_TIMEOUT
        )

        // Launch the app
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(
            APP_PACKAGE_NAME
        )?.apply {
            // Clear out any previous instances
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        // Wait for the app to appear
        device.wait(
            Until.hasObject(By.pkg(APP_PACKAGE_NAME).depth(0)),
            LAUNCH_TIMEOUT
        )
    }

    @Test
    fun login() {
        device.findObject(By.text(context.getString(R.string.login))).click()
        Assert.assertTrue(device.wait({
            device.hasObject(By.textContains("My Github Playroom"))
        }, 5000))
        fun inputLoginCredentials() {
            device.findObject(
                UiSelector().instance(0).className(EditText::class.java)
            ).setText(BuildConfig.GITHUB_TEST_USERNAME)
            device.findObject(
                UiSelector().instance(1).className(EditText::class.java)
            ).setText(BuildConfig.GITHUB_TEST_PASSWORD)
            device.findObject(By.text("Sign in")).click()
            Thread.sleep(2000)
        }
        if (device.hasObject(By.text("Use a different account"))) {
            device.findObject(By.text("Use a different account"))?.click()
            inputLoginCredentials()
        } else {
            inputLoginCredentials()
            if (device.hasObject(By.text("Use a different account"))) {
                device.findObject(By.text("Use a different account"))?.click()
                inputLoginCredentials()
            }
        }
        device.performActionAndWait(
            { device.findObject(By.text("Authorize shyamkp-11"))?.click() },
            Until.newWindow(),
            2000
        )

        Thread.sleep(1000)
        if (device.hasObject(By.text("Authorize shyamkp-11"))) {
            device.findObject(By.text("Authorize shyamkp-11")).click()
        }
        device.waitForIdle(1000)
        if (device.hasObject(By.text("Use a different account"))) {
            inputLoginCredentials()
        }
//        if (!device.hasObject(By.text("Sign out"))) {
//            return
//        }
        device.waitForIdle(1000)
        if (device.hasObject(By.textContains("Star Notifications App"))) {
            device.findObject(By.scrollable(true))
                ?.scrollUntil(Direction.DOWN, Until.scrollFinished(Direction.DOWN))
            device.findObject(By.text("Install & Authorize").clazz(Button::class.java))?.click()
            device.waitForIdle(1000)
        }
        Assert.assertTrue(
            device.wait(
                { device.hasObject(By.text(context.getString(R.string.sign_out))) },
                10000
            )
        )
    }

    @Test
    fun starApp() {
        generateFirebaseAuthToken()
        val loggedIn = device.findObject(By.text(context.getString(R.string.login)))
        if (loggedIn != null) {
            login()
        }
        Thread.sleep(1000)
        if (device.hasObject(By.text(context.getString(R.string.notifications_off)))) {
            device.performActionAndWait({
                device.findObject(By.text(context.getString(R.string.notifications_off)))
                    .click()
            }, Until.newWindow(), 2000)
            device.performActionAndWait({
                device.findObject(UiSelector().instance(1).className(Button::class.java))
                    .click()
            }, Until.newWindow(), 2000)
            device.performActionAndWait({
                device.findObject(By.text("Allow")).click()
            }, Until.newWindow(), 2000)
            Thread.sleep(4000)
        }
        assert(device.findObject(By.text(context.getString(R.string.notifications_on))) != null)
        device.findObject(By.text(context.getString(R.string.search_repositories))).click()
        device.findObject(
            UiSelector().instance(0).className(EditText::class.java)
        ).setText("Public-Test-Repo user:shyamkp-14")
        device.pressEnter()
        device.wait(
            Until.hasObject(By.text("Public-Test-Repo").clazz(TextView::class.java)),
            LAUNCH_TIMEOUT
        )
        device.findObject(By.desc("Star it")).click()
        device.waitForIdle(1000)
        device.wait({ device.hasObject(By.text("UNDO")) }, 5000)
        device.findObject(By.text("UNDO")).click()
        Thread.sleep(2000)
        device.findObject(By.desc("Star it")).click()
        device.waitForIdle(1000)
        Thread.sleep(3000)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        println("Notification "+manager.activeNotifications.first().notification.extras.toString())
        with(manager.activeNotifications.first()) {
            assertTrue( this.packageName == APP_PACKAGE_NAME )
            assertTrue( this.notification.extras.getString("android.title").toString().contains("⭐+ Public-Test-Repo"))
            assertTrue( this.notification.extras.getString("android.text").toString().contains("by shyamkp-14"))
        }
        Thread.sleep(3000)
    }
}