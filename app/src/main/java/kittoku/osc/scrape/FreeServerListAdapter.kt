package kittoku.osc.scrape

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayout
import kittoku.osc.R
import kittoku.osc.preference.OscPreference
import kittoku.osc.preference.accessor.setIntPrefValue
import kittoku.osc.preference.accessor.setStringPrefValue
import kittoku.osc.preference.custom.HomeHostnamePreference
import kittoku.osc.service.ACTION_VPN_DISCONNECT
import kittoku.osc.service.SstpVpnService


class FreeServerListAdapter(private val context: Context, private val dataSource: ArrayList<ServerData>, private val main_activity: AppCompatActivity) : BaseAdapter(){

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.server_list_item, parent, false)
        val serverData = getItem(position) as ServerData

        val textLocation = rowView.findViewById(R.id.textLocation) as TextView
        val textHost = rowView.findViewById(R.id.textHost) as TextView
        val textPort = rowView.findViewById(R.id.textPort) as TextView
        val textPing = rowView.findViewById(R.id.textPing) as TextView
        val textUptime = rowView.findViewById(R.id.textUptime) as TextView

        textLocation.setText(serverData.LOCATION)
        textHost.setText(serverData.HOSTNAME)
        textPort.setText("" + serverData.PORT)
        textPing.setText(serverData.PING)
        textUptime.setText(serverData.UPTIME)

        val root = rowView.findViewById(R.id.layoutRoot) as LinearLayout

        root.setOnClickListener(View.OnClickListener {

            val prefs = PreferenceManager.getDefaultSharedPreferences(it.context)
            setStringPrefValue(serverData.HOSTNAME, OscPreference.HOME_HOSTNAME, prefs)
            setIntPrefValue(serverData.PORT, OscPreference.SSL_PORT, prefs)
            setStringPrefValue("vpn", OscPreference.HOME_USERNAME, prefs)
            setStringPrefValue("vpn", OscPreference.HOME_PASSWORD, prefs)

            it.context?.startService(Intent(context, SstpVpnService::class.java).setAction(
                ACTION_VPN_DISCONNECT
            ))

            val tabLayout = main_activity.findViewById<TabLayout>(R.id.tabBar);
            val tab = tabLayout.getTabAt(0);
            tab?.select();

//            Toast.makeText(
//                it.context,
//                "Host changed to ["+ serverData.HOSTNAME + "]\n\n Go to HOME and connect to the new server.",
//                Toast.LENGTH_LONG
//            ).show()
        })

        return rowView
    }
}