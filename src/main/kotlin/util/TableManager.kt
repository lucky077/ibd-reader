package util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject

object TableManager {

    data class Table (
        var name: String,
        var columns: JSONArray,
        var indexes: JSONArray,
        var reader: InnoDBFileReader
    ){}

    var tableMap = mutableMapOf<String, Table>()

    fun load(name: String): Table {
        var table = tableMap[name]
        if (table == null) {
            synchronized(tableMap) {
                table = tableMap[name]
                if (table == null) {
                    val dataRoot = "/usr/local/mysql/data"
                    val path = "$dataRoot/$name/$name.ibd"
                    val reader = InnoDBFileReader(path)
                    val process = Runtime.getRuntime().exec("ibd2sdi $path")
                    val sdi = JSON.parseArray(String(process.inputStream.readAllBytes()))[1] as JSONObject
                    val obj = sdi.getJSONObject("object").getJSONObject("dd_object")
                    table = Table(name, obj["columns"] as JSONArray, obj["indexes"] as JSONArray, reader)
                    tableMap[name] = table!!
                }
            }
        }
        return table as Table
    }



    fun test() {}

}