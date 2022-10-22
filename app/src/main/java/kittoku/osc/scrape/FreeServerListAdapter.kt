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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import kittoku.osc.R
import kittoku.osc.preference.OscPreference
import kittoku.osc.preference.accessor.setIntPrefValue
import kittoku.osc.preference.accessor.setStringPrefValue
import kittoku.osc.preference.custom.HomeHostnamePreference
import kittoku.osc.service.ACTION_VPN_DISCONNECT
import kittoku.osc.service.SstpVpnService


class FreeServerListAdapter(
    private val dataSource: ArrayList<ServerData>,
    private val main_activity: AppCompatActivity
) : RecyclerView.Adapter<FreeServerListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.server_list_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ItemsViewModel = dataSource[position]

        holder.textLocation.setText(" " + ItemsViewModel.LOCATION)
        holder.textHost.setText(ItemsViewModel.HOSTNAME)
        holder.textPort.setText("" + ItemsViewModel.PORT)
        holder.textPing.setText(ItemsViewModel.PING)
        holder.textUptime.setText(ItemsViewModel.UPTIME)

        Picasso.get().load(ItemsViewModel.FLAG).into(holder.imgFlag)

        holder.root.setOnClickListener(View.OnClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(it.context)

            setStringPrefValue(ItemsViewModel.LOCATION, OscPreference.HOME_PROFILE, prefs)
            setStringPrefValue(ItemsViewModel.HOSTNAME, OscPreference.HOME_HOSTNAME, prefs)
            setIntPrefValue(ItemsViewModel.PORT, OscPreference.SSL_PORT, prefs)
            setStringPrefValue("vpn", OscPreference.HOME_USERNAME, prefs)
            setStringPrefValue("vpn", OscPreference.HOME_PASSWORD, prefs)

            it.context?.startService(
                Intent(main_activity, SstpVpnService::class.java).setAction(
                    ACTION_VPN_DISCONNECT
                )
            )
            val tabLayout = main_activity.findViewById<TabLayout>(R.id.tabBar)
            val tab = tabLayout.getTabAt(0)
            tab?.select()
        })
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return dataSource.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textLocation = ItemView.findViewById(R.id.textLocation) as TextView
        val textHost = ItemView.findViewById(R.id.textHost) as TextView
        val textPort = ItemView.findViewById(R.id.textPort) as TextView
        val textPing = ItemView.findViewById(R.id.textPing) as TextView
        val textUptime = ItemView.findViewById(R.id.textUptime) as TextView
        val imgFlag = ItemView.findViewById(R.id.imgFlag) as ImageView

        val root = ItemView.findViewById(R.id.layoutRoot) as LinearLayout
    }

}