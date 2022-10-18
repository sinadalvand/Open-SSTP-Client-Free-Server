package kittoku.osc.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kittoku.osc.scrape.ServerData
import java.io.File

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("tag", "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d("tag", "Message data payload: ${remoteMessage.data}")

            Log.d("tag", "Short lived task is done.")
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("tag", "Message Notification Body: ${it.body}")
        }

    }
    // [END receive_message]

//    fun sss(){
//        //Log.i("TAG", contents)
//        val listType = object: TypeToken<ArrayList<ServerData>>() {}.type
//        return Gson().fromJson(contents, listType)
//    }
//
//    fun ExportServers(context: Context, data: ArrayList<ServerData>) {
//        val file = File(context.getFilesDir(), "Records.json")
//        file.createNewFile()
//        val json = Gson().toJson(data)
//        //Log.i("TAG", json)
//        file.writeText(json)
//    }

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d("tag", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]


    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d("tag", "sendRegistrationTokenToServer($token)")

        Firebase.messaging.subscribeToTopic("all-user")
            .addOnCompleteListener { task ->
                var msg = "Subscribed 2"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed 2"
                }
                Log.d("TAG", msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
    }


}