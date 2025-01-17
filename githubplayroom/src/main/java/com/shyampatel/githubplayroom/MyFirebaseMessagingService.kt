package com.shyampatel.githubplayroom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shyampatel.core.data.github.GithubRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val repository: GithubRepository by inject()
    private val coroutineScope: CoroutineScope by inject()
    private val ioDispatcher: CoroutineDispatcher by inject(named("IO"))

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")


        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title, it.body, it.imageUrl)
        }

        remoteMessage.data.let {
            Log.d(TAG, "Message Data Body: $it")
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }


    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        sendRegistrationToServer(token)
    }

    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
        /*val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance(this).beginWith(work).enqueue()*/
        // [END dispatch_job]
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
        coroutineScope.launch {
            withContext(ioDispatcher) {
                repository.saveFcmToken(token)
            }
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageTitle: String?, messageBody: String?, imageUrl: Uri?) {

        coroutineScope.launch {
            withContext(ioDispatcher) {
                var bitmap: Bitmap? = null
                if (imageUrl != null) {
                    val loader = Coil.imageLoader(this@MyFirebaseMessagingService)
                    val request = ImageRequest.Builder(this@MyFirebaseMessagingService)
                        .data(imageUrl)
                        .build()
                    val result = (loader.execute(request)).drawable
                    bitmap = result?.toBitmap()
                }
                val requestCode = 0
                val intent = Intent(this@MyFirebaseMessagingService, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(
                    this@MyFirebaseMessagingService,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE,
                )

                val channelId = getString(R.string.default_notification_channel_id)
                val defaultSoundUri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder = NotificationCompat.Builder(this@MyFirebaseMessagingService, channelId)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)

                notificationBuilder.setSmallIcon(R.drawable.baseline_notifications_active_24)

                if (bitmap!= null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notificationBuilder.setLargeIcon(bitmap)
                }

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        getString(R.string.default_notification_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT,
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                val notificationId = 0
                notificationManager.notify(notificationId, notificationBuilder.build())
            }
        }
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}