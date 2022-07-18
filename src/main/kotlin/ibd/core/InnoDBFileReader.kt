package ibd.core

import ibd.struct.IndexPage
import ibd.struct.Page
import java.io.RandomAccessFile
import kotlin.reflect.KClass

class InnoDBFileReader(fileName: String) {
    private val pageSize = 0x4000
    var file: RandomAccessFile

    init {
        file = RandomAccessFile(fileName, "r")
    }

    fun read(pageNo: Int): IndexPage {
        return read(pageNo,  IndexPage::class)
    }

    fun <T : Page> read(pageNo: Int, clazz: KClass<T>): T {
        file.seek((pageNo * pageSize).toLong())

        val b = ByteArray(pageSize)
        file.read(b, 0, pageSize)

        @Suppress("UNCHECKED_CAST")
        return clazz.java.constructors.first().newInstance(b.toList()) as T
    }

}