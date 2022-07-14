package struct

import util.CommonUtil

/**
 * 组成索引的页，type: 0x45BF
 */
class IndexPage(data: List<Byte>): Page(data) {

    /** Page Directory Slot数量 */
    val inDirSlots = CommonUtil.bytes2Int16(read(2));
    val heapTop = CommonUtil.bytes2Int16(read(2));
    val nHeap = CommonUtil.bytes2Int16(read(2));
    val free = CommonUtil.bytes2Int16(read(2));
    val garbage = CommonUtil.bytes2Int16(read(2));
    val lastInsert = CommonUtil.bytes2Int16(read(2));
    val direction = CommonUtil.bytes2Int16(read(2));
    val nDirection = CommonUtil.bytes2Int16(read(2));
    /** 页内有效数据数量 */
    val nRecs = CommonUtil.bytes2Int16(read(2));
    val maxTrxId = CommonUtil.bytes2Int64(read(8));
    /** 在索引内的高度，0为叶子节点 */
    val level = CommonUtil.bytes2Int16(read(2));
    val indexId = CommonUtil.bytes2Int64(read(8));
    private val _ignore = read(20);

    val trailerChkSum: Int
    val trailerLsn: Int
    var slots = mutableListOf<Short>()


    init {
        reset(-1)
        trailerLsn = CommonUtil.bytes2Int32(readReverse(4))
        trailerChkSum = CommonUtil.bytes2Int32(readReverse(4))
        if (trailerChkSum != chkSum || trailerLsn != lsn.toInt()) {
            throw RuntimeException("data check error")
        }
        for (i in 1 .. inDirSlots) {
            slots.add(CommonUtil.bytes2Int16(readReverse(2)))
        }
    }

}