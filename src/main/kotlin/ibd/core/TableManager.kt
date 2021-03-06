package ibd.core

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import ibd.const.MYSQL_TYPE_VARCHAR
import ibd.struct.sdi.TableInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

object TableManager {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    data class Table(
        val name: String,
        val tableInfo: TableInfo,
        val reader: InnoDBFileReader
    )

    private var tableMap = mutableMapOf<String, Table>()
    private lateinit var dbRoot: String

    /**
     * 加载文件夹内所有ibd文件
     */
    fun init(dbRoot: String) {
        TableManager.dbRoot = dbRoot
        val root = File(dbRoot)
        if (!root.isDirectory) throw FileNotFoundException()
        root.listFiles { _, name -> name.endsWith(".ibd") }!!
            .map { it.name.removeSuffix(".ibd") }
            .forEach { load(it) }
    }

    fun load(name: String): Table {
        var table = tableMap[name]
        if (table == null) {
            synchronized(tableMap) {
                table = tableMap[name]
                if (table == null) {
                    val path = "$dbRoot/$name.ibd"
                    val reader = InnoDBFileReader(path)
                    val process = Runtime.getRuntime().exec("ibd2sdi $path")
                    val sdi = JSON.parseArray(String(process.inputStream.readAllBytes()))[1] as JSONObject

                    val tableInfo =
                        JSON.parseObject(sdi.getJSONObject("object").getString("dd_object"), TableInfo::class.java)

                    for ((index, column) in tableInfo.columns.filter { it.nullable }.withIndex()) {
                        tableInfo.nullableMap[column.name] = index
                    }

                    for ((index, column) in tableInfo.columns.filter { it.type == MYSQL_TYPE_VARCHAR }.withIndex()) {
                        tableInfo.lenMap[column.name] = index
                    }

                    //读取变长字符串、可空的数量
                    tableInfo.varcharCount = tableInfo.columns.count { it.type == MYSQL_TYPE_VARCHAR }
                    tableInfo.indexes.forEach { indexInfo ->
                        val keyOpxSet = indexInfo.elements.filter { !it.hidden }.map { it.column_opx + 1 }
                        indexInfo.varcharCount = tableInfo.columns.count { it.type == MYSQL_TYPE_VARCHAR && keyOpxSet.contains(it.ordinal_position) }
                        indexInfo.nullableCount = tableInfo.columns.count { it.nullable && keyOpxSet.contains(it.ordinal_position) }
                    }

                    table = Table(name, tableInfo, reader)
                    tableMap[name] = table!!
                    reader.read(0)
                    log.info("table {} loaded", name)
                }
            }
        }
        return table as Table
    }


}