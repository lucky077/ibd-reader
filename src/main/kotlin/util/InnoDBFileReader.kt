package util

import struct.IndexPage
import struct.Page
import java.io.RandomAccessFile
import kotlin.reflect.KClass

class InnoDBFileReader(fileName: String) {
    val pageSize = 0x4000
    var file: RandomAccessFile

    init {
        file = RandomAccessFile(fileName, "r")
    }

    fun read(pageNo: Int): IndexPage {
        return read0(pageNo, 1, IndexPage::class).first()
    }

    fun <T: Page>read0(pageNo: Int, clazz: KClass<T>): T {
        return read0(pageNo, 1, clazz).first()
    }

    fun <T: Page>read0(pageNo: Int, num: Int, clazz: KClass<T>): ArrayList<T> {
        var list = arrayListOf<T>()
        file.seek((pageNo * pageSize).toLong())

        for (i in 1 .. num) {

            val b = ByteArray(pageSize)
            file.read(b, 0, pageSize)
            list.add(clazz.java.constructors.first().newInstance(b.toList()) as T)

        }

        return list
    }

}