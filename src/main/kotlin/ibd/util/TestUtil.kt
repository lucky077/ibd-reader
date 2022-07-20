package ibd.util

import ibd.struct.IndexPage
import ibd.struct.Record
import ibd.struct.sdi.Index
import ibd.struct.sdi.TableInfo

var testStart = false
//private val time = ThreadLocal<Long>()
private var time = 0L
fun begin() = if (testStart) time = System.nanoTime() else Unit
fun end() =  if (testStart) println((System.nanoTime() - time) / 1000) else Unit

fun printSlotsKeys(slots: List<Int>, page: IndexPage, indexInfo: Index, tableInfo: TableInfo) {
    if (!testStart) return
    slots.forEach {

        val r = Record(it, page, indexInfo, tableInfo)
        println(r.keyList)

    }

}