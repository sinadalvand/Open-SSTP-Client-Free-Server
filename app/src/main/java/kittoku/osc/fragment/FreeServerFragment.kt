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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kittoku.osc.R
import kittoku.osc.scrape.FreeServerListAdapter
import kittoku.osc.scrape.HtmlExtractionFreeServer
import kittoku.osc.scrape.SORT
import kittoku.osc.scrape.ServerData
import kittoku.osc.util.sortByTOPSIS
import kittoku.osc.util.toArrayList
import java.io.File

class FreeServerFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prg: ProgressBar
    private lateinit var mListView: RecyclerView
    private lateinit var sortSpinner: AppCompatSpinner
    private lateinit var swiperefreshlayout: SwipeRefreshLayout
    private val serverList = MutableLiveData<ArrayList<ServerData>?>()
    private var sortingType = SORT.BEST

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_free_server, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prg = view.findViewById<ProgressBar>(R.id.prgLoading)
        sortSpinner = view.findViewById<AppCompatSpinner>(R.id.sort_spinner)
        mListView = view.findViewById<RecyclerView>(R.id.serverlist)
        mListView.layoutManager = LinearLayoutManager(view.context)
        swiperefreshlayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefreshlayout)

        swiperefreshlayout.setOnRefreshListener {
            swiperefreshlayout.isRefreshing = false
            RefreshList(view.context)
            //adapter.notifyDataSetChanged()
        }
        RefreshList(view.context)


        sortSpinner.onItemSelectedListener = this
        ArrayAdapter.createFromResource(requireContext(), R.array.sort_type_array, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                sortSpinner.adapter = adapter
            }


        serverList.observe(viewLifecycleOwner) {
            it?.let {
                val adapter = FreeServerListAdapter(it, getActivity() as AppCompatActivity)
                mListView.adapter = adapter
                mListView.visibility = View.VISIBLE
            }
        }
    }

    fun RefreshList(context: Context) {
        mListView.visibility = View.INVISIBLE
        prg.visibility = View.VISIBLE
        HtmlExtractionFreeServer().extract(context) { result ->
            handler.post {
                if (result.size > 0) {
                    serverList.value = (result)
                    sortServerList()
                } else
                    Toast.makeText(
                        context,
                        "Can not fetch the Servers!",
                        Toast.LENGTH_SHORT
                    ).show()
            }

            prg.visibility = View.INVISIBLE
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        sortingType = when (p2) {
            1 -> SORT.PING
            2 -> SORT.SESSIONS
            3 -> SORT.UPTIME
            4 -> SORT.QUALITY
            else -> SORT.BEST
        }
        sortServerList()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    private fun sortServerList() {
        val oldList = serverList.value
        val sorted = when (sortingType) {
            SORT.BEST -> {
                //1.line_quality    2.ping    3.session   4.score
                val profit = arrayOf(true,false,false,true)
                val weight = arrayOf(4f,3f,2f,1f) // importance of each param according to input
                oldList?.sortByTOPSIS(weight,profit){ arrayOf(it.getLineQuality()*1.0,it.getPing()*1.0,it.SESSIONS*1.0,it.SCORE*1.0)}
            }
            SORT.PING -> {
                oldList?.sortedBy { it.getPing() }?.toArrayList()
            }
            SORT.SESSIONS -> {
                oldList?.sortedBy { it.SESSIONS }?.toArrayList()
            }
            SORT.UPTIME -> {
                oldList?.sortedByDescending { it.getUpTimeInSeconds() }?.toArrayList()
            }
            SORT.QUALITY -> {
                oldList?.sortedByDescending { it.getLineQuality() }?.toArrayList()
            }
            SORT.SCORE -> {
                oldList?.sortedByDescending { it.SCORE }?.toArrayList()
            }
        }
        serverList.postValue(sorted)
    }
}

