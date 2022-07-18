package ibd.struct

import ibd.util.bytes2Int32
import ibd.util.bytes2Int64

/**
 * 基本页头结构
 */
open class Page(data: List<Byte>) : ByteReader(data) {

    val chkSum = bytes2Int32(read(4)) // 4
    val offset = bytes2Int32(read(4)) // 4
    val prev = bytes2Int32(read(4)) // 4
    val next = bytes2Int32(read(4)) // 4
    val lsn = bytes2Int64(read(8)) // 8
    val type = bytes2Int32(read(2)) // 2
    val fileFlushLsn = bytes2Int64(read(8)) // 8
    val archLogNo = bytes2Int32(read(4)) // 4

}