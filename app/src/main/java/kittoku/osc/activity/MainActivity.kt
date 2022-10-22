package kittoku.osc.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kittoku.osc.BuildConfig
import kittoku.osc.R
import kittoku.osc.databinding.ActivityMainBinding
import kittoku.osc.fragment.FreeServerFragment
import kittoku.osc.fragment.HomeFragment
import kittoku.osc.fragment.SettingFragment
import org.json.JSONObject
import java.net.URL


class MainActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
            registerToTopic()
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun registerToTopic() {
        Firebase.messaging.subscribeToTopic("all-user")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d("TAG", msg)
            }
    }

    fun checkForUpdate(view: View) {
        val thread = Thread {
            try {
                val contents =
                    URL("https://raw.githubusercontent.com/FreeSSTP/server-list/main/version.json").readText()
                val jsonObject = JSONObject(contents)
                val version_code = jsonObject.getInt("version_code")
                val version_name = jsonObject.getString("version_name")
                val download_url = jsonObject.getString("url")
                if (version_code > BuildConfig.VERSION_CODE) {
                    handler.post {
                        val snack = Snackbar.make(
                            view,
                            "New version v${version_name} is available.",
                            Snackbar.LENGTH_INDEFINITE
                        )
                        snack.setAction("Download") {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(download_url))
                            startActivity(browserIntent)
                        }
                        snack.show()
                    }
                }
            } catch (e: Exception) {
                Log.e("tag", e.toString())
            }
        }
        thread.start()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "${getString(R.string.app_name)}: ${BuildConfig.VERSION_NAME} (FREE SERVER)"
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkForUpdate(binding.root)

        registerToTopic()
        
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            Log.d("TAG", "Token: " + token)
        })

        object : FragmentStateAdapter(this) {
            private val homeFragment = HomeFragment()

            private val settingFragment = SettingFragment()

            private val freeServerFragment = FreeServerFragment()

            override fun getItemCount() = 3

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> homeFragment
                    1 -> freeServerFragment
                    2 -> settingFragment
                    else -> throw NotImplementedError()
                }
            }

        }.also {
            binding.pager.adapter = it
        }

        TabLayoutMediator(binding.tabBar, binding.pager) { tab, position ->
            tab.text = when (position) {
                0 -> "HOME"
                1 -> "FREE SERVER"
                2 -> "SETTING"
                else -> throw NotImplementedError()
            }
        }.attach()
    }
}
