package struct

import const.*
import core.readValue
import struct.sdi.Index
import struct.sdi.TableInfo
import util.bit2Bool
import util.bit2Int
import util.bytes2Int32

class Record(val offset: Int, val page: IndexPage, val indexInfo: Index, val tableInfo: TableInfo) :
    ByteReader(page.data) {

    /** 可变字符串长度表 */
    val lenList = mutableListOf<Int>()

    /** 可以为null字符串表 */
    val nullList = mutableListOf<Boolean>()

    /** 索引key表 */
    val keyList = mutableListOf<Any>()

    /** 索引key顺序表 */
    val  keyOrderList = mutableListOf<Int>()

    /** @see const.RECORD_TYPE_NORMAL */
    val type: Int

    /** 组记录数，非组内最大记录为0 */
    val nOwned: Int

    private val nextRecord: Int

    init {

        reset(offset)
        val header = readReverse(5)
        nOwned = bit2Int(header[0], 4, 4)
        type = bit2Int(header[2], 5, 3)
        nextRecord = bytes2Int32(header.slice(3 until 5))

        //读取叶子节点的null list
        if (type == RECORD_TYPE_NORMAL && indexInfo.name == PRIMARY_KEY) {

            val nullListSize = tableInfo.columns.filter { it.is_nullable }.size
            if (nullListSize > 0) {
                val byteSize = if (nullListSize % 8 == 0) nullListSize / 8 else nullListSize / 8 + 1
                val nullByteList = readReverse(byteSize).asReversed()
                for (byte in nullByteList) {
                    for (i in 7 downTo 0) {
                        if (nullList.size < nullListSize)
                            nullList.add(bit2Bool(byte, i))
                    }
                }
            }
        }


        if (type <= RECORD_TYPE_NON_LEAF) {

            //读取变长字符串的长度表
            //非叶子节点只需要处理index key column
            val varcharList = if (type == RECORD_TYPE_NON_LEAF) {
                val keyOpxSet = indexInfo.elements.filter { !it.hidden }.map { it.column_opx + 1 }.toSet()
                tableInfo.columns.filter { it.type == MYSQL_TYPE_VARCHAR && keyOpxSet.contains(it.ordinal_position) }
            } else {
                tableInfo.columns.filter { it.type == MYSQL_TYPE_VARCHAR }
            }
            repeat(varcharList.size) {
                val b = readReverse(1).first()
                if (bit2Bool(b, 0)) {
                    lenList.add(bytes2Int32(listOf(b, readReverse(1).first())) shl 17 shr 17)
                } else {
                    lenList.add(bytes2Int32(listOf(b)) shl 25 shr 25)
                }

            }

            reset(offset)

            //读取key
            indexInfo.elements.filter { !it.hidden }.forEach {
                keyOrderList.add(it.order)
                keyList.add(readValue(this, tableInfo.columns[it.column_opx])!!)
            }
        }

    }


    /**
     * 读取非叶子节点指向的页
     */
    fun readPageNo(): Int {
        if (type != RECORD_TYPE_NON_LEAF) throw RuntimeException("This is not a non-leaf node")
        //非主键索引会有主键，跳过
        if (indexInfo.type != INDEX_TYPE_PRIMARY) {
            indexInfo.elements.filter { it.hidden }.map { it.column_opx }.forEach {
                readValue(this, tableInfo.columns[it])
            }
        }

        return bytes2Int32(read(4))
    }

    /**
     * 从行中读取所有数据
     */
    fun readALL(): List<Any> {

        return listOf()
    }

    /**
     * 获取吓一条记录
     */
    fun next(): Record {
        if (nextRecord == 0xFFCA) return page.getSupremumRecord(indexInfo, tableInfo)
        return Record(offset + nextRecord, page, indexInfo, tableInfo)
    }

    /**
     * 0：在条件范围内
     * -1：在条件范围之前
     * 1：在条件范围之后
     */
    fun compareTo(types: List<Type>): Int {

        if (type == RECORD_TYPE_INFIMUM) return -1
        if (type == RECORD_TYPE_SUPREMUM) return 1

        for ((index, type) in types.withIndex()) {

            var r = type.compare(keyList[index])
            // 不相等无需比较下一个key，
            if (r != 0) {
                //索引顺序为降序时只需反向结果即可，在其它地方全部按升序处理
                if (keyOrderList[index] == ORDER_DESC) {
                    return if (r > 0) -1 else 1
                }
                return r
            }
        }

        return 0
    }

}