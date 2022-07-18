package ibd.util

import ibd.struct.IndexPage
import ibd.struct.Record
import ibd.struct.sdi.Index
import ibd.struct.sdi.TableInfo

private val time = ThreadLocal<Long>()
fun begin() = time.set(System.nanoTime())
fun end() = println((System.nanoTime() - time.get()) / 1000)

fun printSlotsKeys(slots: List<Int>, page: IndexPage, indexInfo: Index, tableInfo: TableInfo) {

    slots.forEach {

        val r = Record(it, page, indexInfo, tableInfo)
        println(r.keyList)

    }

}