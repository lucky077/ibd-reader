package struct

import struct.sdi.Index
import struct.sdi.TableInfo
import util.bytes2Int32
import util.bytes2Int64

/**
 * 组成索引的页，type: 0x45BF
 */
class IndexPage(data: List<Byte>) : Page(data) {

    /** Page Directory Slot数量 */
    val inDirSlots = bytes2Int32(read(2))
    val heapTop = bytes2Int32(read(2))
    val nHeap = bytes2Int32(read(2))
    val free = bytes2Int32(read(2))
    val garbage = bytes2Int32(read(2))
    val lastInsert = bytes2Int32(read(2))
    val direction = bytes2Int32(read(2))
    val nDirection = bytes2Int32(read(2))

    /** 页内有效数据数量 */
    val nRecs = bytes2Int32(read(2))
    val maxTrxId = bytes2Int64(read(8))

    /** 在索引内的高度，0为叶子节点 */
    val level = bytes2Int32(read(2))
    val indexId = bytes2Int64(read(8))
    private val _ignore = read(20)
    private val infimumRecordOffset = p + 5
    private val supremumRecordOffset = p + 18

    private val trailerChkSum: Int
    private val trailerLsn: Int
    var slots = mutableListOf<Int>()


    init {
        reset(-1)
        trailerLsn = bytes2Int32(readReverse(4))
        trailerChkSum = bytes2Int32(readReverse(4))
        if (trailerChkSum != chkSum || trailerLsn != lsn.toInt()) {
            throw RuntimeException("data check error")
        }
        repeat(inDirSlots) {
            slots.add(bytes2Int32(readReverse(2)))
        }
    }

    fun getInfimumRecord(indexInfo: Index, tableInfo: TableInfo): Record {
        return Record(infimumRecordOffset, this, indexInfo, tableInfo)
    }
    fun getSupremumRecord(indexInfo: Index, tableInfo: TableInfo): Record {
        return Record(supremumRecordOffset, this, indexInfo, tableInfo)
    }

}