package kittoku.osc.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kittoku.osc.R
import kittoku.osc.scrape.FreeServerListAdapter
import kittoku.osc.scrape.HtmlExtractionFreeServer
import kittoku.osc.scrape.ServerData
import java.io.File

class FreeServerFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prg: ProgressBar
    private lateinit var mListView: ListView
    private lateinit var fabRefresh: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_free_server, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prg = view.findViewById<ProgressBar>(R.id.prgLoading)
        mListView = view.findViewById<ListView>(R.id.serverlist)
        fabRefresh = view.findViewById<FloatingActionButton>(R.id.fabRefresh)



        fabRefresh.setOnClickListener(View.OnClickListener {
            RefreshList(view.context)
        })

        RefreshList(view.context)
    }

    fun RefreshList(context: Context) {
        fabRefresh.visibility = View.INVISIBLE
        mListView.visibility = View.INVISIBLE
        prg.visibility = View.VISIBLE

        var serverDataList = ArrayList<ServerData>()

        HtmlExtractionFreeServer().extract() { result ->
            handler.post {
                if (result.size > 3) {

                    result.forEach {
                        serverDataList.add(it)
                    }

                    val adapter = FreeServerListAdapter(context, serverDataList)
                    mListView.adapter = adapter
                    mListView.visibility = View.VISIBLE

                    ExportServers(context, serverDataList)
                } else {
                    serverDataList = ImportServers(context)

                    if (serverDataList.size > 0) {
                        val adapter = FreeServerListAdapter(context, serverDataList)
                        mListView.adapter = adapter
                        mListView.visibility = View.VISIBLE
                    } else
                        Toast.makeText(
                            context,
                            "Can not fetch the Servers!",
                            Toast.LENGTH_SHORT
                        ).show()
                }

                prg.visibility = View.INVISIBLE
                fabRefresh.visibility = View.VISIBLE
            }
        }
    }

    fun ExportServers(context: Context, data: ArrayList<ServerData>) {
        val file = File(context.getFilesDir(), "Records.txt")
        file.createNewFile()
        data.forEach {
            file.appendText(it.toString() + "\n")
        }
    }

    fun ImportServers(context: Context): ArrayList<ServerData> {
        val listOfServerData = ArrayList<ServerData>()
        val file = File(context.getFilesDir(), "Records.txt")
        if (file.exists()) {
            val contents = file.readText()
            val lines = contents.split("\n")
            var data = ServerData()
            lines.forEachIndexed { index, element ->
                val step = index % 5
                if (step == 0) {
                    data = ServerData()
                }
                when (step) {
                    0 -> data.LOCATION = element
                    1 -> data.HOSTNAME = element
                    2 -> data.PORT = element.toInt()
                    3 -> data.UPTIME = element
                    4 -> {
                        data.PING = element
                        listOfServerData.add(data)
                    }
                }
            }
        }
        return listOfServerData
    }
}

