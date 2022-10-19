package kittoku.osc.scrape

import android.util.Log
import it.skrape.core.document
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import kotlinx.coroutines.handleCoroutineException
import java.util.concurrent.TimeUnit


data class ServerData(
    var LOCATION: String = "",
    var HOSTNAME: String = "",
    var PORT: Int = 443,
    var UPTIME: String = "",
    var PING: String = ""
){
    override fun toString(): String {
        return "$LOCATION\n$HOSTNAME\n$PORT\n$UPTIME\n$PING"
    }
}


class HtmlExtractionFreeServer {

    fun extract(myCallback: (result: MutableList<ServerData>) -> Unit) {
        val thread = Thread {
            val listOfServerData: MutableList<ServerData> = mutableListOf()
            try {
                val extracted = skrape(HttpFetcher) {
                    request {
                        url = "https://ipspeed.info/freevpn_sstp.php?language=en"
                    }

                    response {
                        status { code }
                        status { message }
                        htmlDocument {
                            "div.area div.list" {
                                findAll {
                                    eachText
                                }
                            }
                        }
                    }
                }


                var data = ServerData()
                extracted.forEachIndexed { index, element ->
                    //Log.i("TAG", element)
                    if (index > 3) {
                        val step = index % 4
                        if (step == 0) {
                            data = ServerData()
                        }
                        when (step) {
                            0 -> data.LOCATION = element
                            1 -> {
                                if (element.contains(':')) {
                                    val temp = element.split(":")
                                    data.HOSTNAME = temp[0]
                                    data.PORT = temp[1].toInt()
                                } else {
                                    data.HOSTNAME = element
                                    data.PORT = 443
                                }
                            }
                            2 -> data.UPTIME = element
                            3 -> {
                                data.PING = element
                                listOfServerData.add(data)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("tag", e.toString())
            } finally {
                myCallback.invoke(listOfServerData)
            }
        }
        thread.start()
    }
}