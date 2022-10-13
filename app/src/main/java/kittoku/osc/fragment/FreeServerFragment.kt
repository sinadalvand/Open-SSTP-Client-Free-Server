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

    fun RefreshList(context: Context){
        fabRefresh.visibility = View.INVISIBLE
        mListView.visibility = View.INVISIBLE
        prg.visibility = View.VISIBLE

        val serverDataList = ArrayList<ServerData>()

        HtmlExtractionFreeServer().extract() { result ->
            handler.post {
                if (result.size > 3) {

                    result.forEach {
                        serverDataList.add(it)
                    }

                    val adapter = FreeServerListAdapter(context, serverDataList)
                    mListView.adapter = adapter

                    mListView.visibility = View.VISIBLE
                }
                else
                {
                    Toast.makeText(
                        context,
                        "No internet connection!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                prg.visibility = View.INVISIBLE
                fabRefresh.visibility = View.VISIBLE
            }
        }
    }
}

