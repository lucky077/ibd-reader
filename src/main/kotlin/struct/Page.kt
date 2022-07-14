package struct

import util.CommonUtil

/**
 * 基本页头结构
 */
open class Page (data: List<Byte>): ByteReader(data) {

    val chkSum = CommonUtil.bytes2Int32(read(4)) // 4
    val offset = CommonUtil.bytes2Int32(read(4)) // 4
    val prev = CommonUtil.bytes2Int32(read(4)) // 4
    val next = CommonUtil.bytes2Int32(read(4)) // 4
    val lsn = CommonUtil.bytes2Int64(read(8)) // 8
    val type = CommonUtil.bytes2Int16(read(2)) // 2
    val fileFlushLsn = CommonUtil.bytes2Int64(read(8)) // 8
    val archLogNo = CommonUtil.bytes2Int32(read(4)) // 4

}