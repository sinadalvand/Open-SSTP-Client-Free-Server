package kittoku.opensstpclient.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import kittoku.opensstpclient.*


private val homePreferences = arrayOf<PreferenceWrapper<*>>(
    StrPreference.HOME_HOST,
    StrPreference.HOME_USER,
    StrPreference.HOME_PASS,
)

class HomeFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.home, rootKey)

        homePreferences.forEach {
            it.initPreference(this, preferenceManager.sharedPreferences)
        }

        attachConnectorListener()
        setSwitchOffListener()
    }

    private fun attachConnectorListener() {
        findPreference<SwitchPreferenceCompat>(BoolPreference.HOME_CONNECTOR.name)!!.also {
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newState ->
                if (newState == true) {
                    if (!checkPreferences()) {
                        return@OnPreferenceChangeListener false
                    }

                    val intent = VpnService.prepare(context)

                    if (intent != null) {
                        startActivityForResult(intent, 0)
                    } else {
                        onActivityResult(0, Activity.RESULT_OK, null)
                    }
                } else {
                    startVpnService(VpnAction.ACTION_DISCONNECT)
                }

                true
            }
        }
    }

    private fun detachConnectorListener() {
        findPreference<SwitchPreferenceCompat>(BoolPreference.HOME_CONNECTOR.name)!!.also {
            it.onPreferenceChangeListener = null
        }
    }

    private fun setSwitchOffListener() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == VpnAction.ACTION_SWITCH_OFF.value) {
                        if (activity != null) {
                            detachConnectorListener()
                            BoolPreference.HOME_CONNECTOR.setValue(this@HomeFragment, false)
                            attachConnectorListener()
                        }
                    }
                }
            },
            IntentFilter().also { it.addAction(VpnAction.ACTION_SWITCH_OFF.value) }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            startVpnService(VpnAction.ACTION_CONNECT)
        }
    }

    private fun startVpnService(action: VpnAction) {
        context?.startService(Intent(context, SstpVpnService::class.java).setAction(action.value))
    }

    private fun makeToast(cause: String) {
        Toast.makeText(context, "INVALID SETTING: $cause", Toast.LENGTH_LONG).show()
    }

    private fun checkPreferences(): Boolean {
        val prefs = preferenceManager.sharedPreferences


        StrPreference.HOME_HOST.getValue(prefs).also {
            if (TextUtils.isEmpty(it)) {
                makeToast("Host is missing")
                return false
            }
        }

        IntPreference.SSL_PORT.getValue(prefs).also {
            if (it !in 0..65535) {
                makeToast("The given port is out of 0-65535")
                return false
            }
        }

        val doAddCerts = BoolPreference.SSL_DO_ADD_CERT.getValue(prefs)
        val certDir = DirPreference.SSL_CERT_DIR.getValue(prefs)
        if (doAddCerts && certDir.isEmpty()) {
            makeToast("No certificates directory was selected")
            return false
        }

        IntPreference.PPP_MRU.getValue(prefs).also {
            if (it !in MIN_MRU..MAX_MRU) {
                makeToast("The given MRU is out of $MIN_MRU-$MAX_MRU")
                return false
            }
        }

        IntPreference.PPP_MTU.getValue(prefs).also {
            if (it !in MIN_MTU..MAX_MTU) {
                makeToast("The given MRU is out of $MIN_MTU-$MAX_MTU")
                return false
            }
        }

        val isIpv4Enabled = BoolPreference.PPP_IPv4_ENABLED.getValue(prefs)
        val isIpv6Enabled = BoolPreference.PPP_IPv6_ENABLED.getValue(prefs)
        if (!isIpv4Enabled && !isIpv6Enabled) {
            makeToast("No network protocol was enabled")
            return false
        }

        val isPapEnabled = BoolPreference.PPP_PAP_ENABLED.getValue(prefs)
        val isMschapv2Enabled = BoolPreference.PPP_MSCHAPv2_ENABLED.getValue(prefs)
        if (!isPapEnabled && !isMschapv2Enabled) {
            makeToast("No authentication protocol was enabled")
            return false
        }

        IntPreference.IP_PREFIX.getValue(prefs).also {
            if (it !in 0..32) {
                makeToast("The given address prefix length is out of 0-32")
                return false
            }
        }

        val doSaveLog = BoolPreference.LOG_DO_SAVE_LOG.getValue(prefs)
        val logDir = DirPreference.LOG_DIR.getValue(prefs)
        if (doSaveLog && logDir.isEmpty()) {
            makeToast("No log directory was selected")
            return false
        }


        return true
    }
}

