package util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import const.MYSQL_TYPE_VARCHAR
import struct.sdi.TableInfo

object TableManager {

    data class Table(
        val name: String,
        val tableInfo: TableInfo,
        val reader: InnoDBFileReader
    ) {}

    var tableMap = mutableMapOf<String, Table>()

    fun load(name: String): Table {
        var table = tableMap[name]
        if (table == null) {
            synchronized(tableMap) {
                table = tableMap[name]
                if (table == null) {
                    val dataRoot = "/usr/local/mysql/data"
                    val dbName = "test"
                    val path = "$dataRoot/$dbName/$name.ibd"
                    val reader = InnoDBFileReader(path)
                    val process = Runtime.getRuntime().exec("ibd2sdi $path")
                    val sdi = JSON.parseArray(String(process.inputStream.readAllBytes()))[1] as JSONObject

                    val tableInfo =
                        JSON.parseObject(sdi.getJSONObject("object").getString("dd_object"), TableInfo::class.java)

                    for ((index, column) in tableInfo.columns.filter { it.is_nullable }.withIndex()) {
                        tableInfo.nullableMap[column.name] = index
                    }

                    for ((index, column) in tableInfo.columns.filter { it.type == MYSQL_TYPE_VARCHAR }.withIndex()) {
                        tableInfo.lenMap[column.name] = index
                    }

                    table = Table(name, tableInfo, reader)
                    tableMap[name] = table!!
                }
            }
        }
        return table as Table
    }


}