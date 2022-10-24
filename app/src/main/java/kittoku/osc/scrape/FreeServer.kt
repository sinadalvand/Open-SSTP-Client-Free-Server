package kittoku.osc.scrape

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.handleCoroutineException
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit


data class ServerData(
    var LOCATION: String = "",
    var HOSTNAME: String = "",
    var PORT: Int = 443,
    var UPTIME: String = "",
    var PING: String = "",
    var FLAG: String = "",
    var SESSIONS: Int = 0,
    var LINE_QUALITY: String = "",
    var SCORE: Int = 0
){
    override fun toString(): String {
        return "$LOCATION\n$HOSTNAME\n$PORT\n$UPTIME\n$PING"
    }

    fun getPing():Int{
        return PING.filter { it.isDigit() }.toInt()
    }

    fun getUpTimeInSeconds():Long{
        val multi = when{
            UPTIME.contains("day")-> 60 * 60 * 24L
            UPTIME.contains("hour")-> 60 * 60L
            UPTIME.contains("min")-> 60L
            else -> 1L
        }
        return UPTIME.filter { it.isDigit()}.run{ toInt() * multi}
    }

    fun getLineQuality():Float = LINE_QUALITY.filter { it.isDigit() || it == '.'}.run{ toFloat()}
}


class HtmlExtractionFreeServer {

    fun extract(context: Context, myCallback: (result: ArrayList<ServerData>) -> Unit) {
        val thread = Thread {
            var listOfServerData = ArrayList<ServerData>()
            try {
                val contents = URL("https://raw.githubusercontent.com/FreeSSTP/server-list/main/Records.json").readText()
                //Log.i("tag" , contents)
                val listType = object: TypeToken<ArrayList<ServerData>>() {}.type
                listOfServerData = Gson().fromJson(contents, listType)
                ExportServers(context, listOfServerData)
            } catch (e: Exception) {
                Log.e("tag", e.toString())
                listOfServerData = ImportServers(context)
            } finally {
                myCallback.invoke(listOfServerData)
            }
        }
        thread.start()
    }

    private  fun ExportServers(context: Context, data: ArrayList<ServerData>) {
        val file = File(context.getFilesDir(), "Records.json")
        file.createNewFile()
        val json = Gson().toJson(data)
        //Log.i("TAG", json)
        file.writeText(json)
    }

    private fun ImportServers(context: Context): ArrayList<ServerData> {
        val listOfServerData = ArrayList<ServerData>()
        val file = File(context.getFilesDir(), "Records.json")
        if (file.exists()) {
            val contents = file.readText()
            //Log.i("TAG", contents)
            val listType = object: TypeToken<ArrayList<ServerData>>() {}.type
            return Gson().fromJson(contents, listType)
        }
        return listOfServerData
    }
}