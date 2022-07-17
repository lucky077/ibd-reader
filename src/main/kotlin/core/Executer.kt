package core

import const.INDEX_TYPE_UNIQUE
import const.ORDER_ASC
import const.ORDER_DESC
import const.RECORD_TYPE_NORMAL
import org.slf4j.LoggerFactory
import struct.Equal
import struct.Record
import struct.Type
import util.TableManager
import util.binarySearch

/**
 * 执行逻辑链
 */
class Executer(var tableName: String) {

    val log = LoggerFactory.getLogger(this.javaClass)

    lateinit var index: String
    var types: List<Type> = listOf()

    var offset: Int = 0
    var size: Int = 0
    var order: Int = ORDER_ASC

    fun primary(): Executer {
        return index("PRIMARY")
    }

    fun index(name: String): Executer {
        this.index = name
        return this
    }

    /**
     * 对应索引key顺序
     */
    fun type(vararg types: Type): Executer {
        return type(types.toList())
    }

    /**
     * 非等值条件的下一个失效
     */
    fun type(types: List<Type>): Executer {
        var validSize = 0
        for (type in types) {
            validSize++
            if (type !is Equal) {
                break
            }
        }
        this.types = types.slice(0 until validSize)
        return this
    }

    /**
     * 排序必须与limit一同使用。
     * 并且只能针对索引中的一个key，因为如果第下一个key的顺序与期望顺序一致则不用处理，否则难以利用
     * 应用于首个非等值的key条件，
     */
    fun limit(offset: Int, size: Int = 0, order: Int = ORDER_ASC): Executer {
        this.offset = offset.coerceAtLeast(0)
        this.size = size.coerceAtLeast(0)
        this.order = order.coerceAtLeast(ORDER_ASC).coerceAtMost(ORDER_DESC)
        return this
    }

    fun limit(size: Int, order: Int = ORDER_ASC): Executer {
        return limit(0, size, order)
    }

    fun run() {

        val table = TableManager.load(tableName)
        val reader = table.reader
        val indexInfo = table.tableInfo.indexes.first { it.name == index }
        val tableInfo = table.tableInfo

        //获取索引根页号
        val rootPageNo = indexInfo.se_private_data.split(";")[1].split("=")[1].toInt()
        log.debug("index root pageNo: {}", rootPageNo)

        //条件数量不能超过索引key数量
        val keyCount = indexInfo.elements.filter { !it.hidden }.size
        if (types.size > keyCount) types = types.slice(0 until keyCount)

        //找到首个非等值的条件,判断实际应用的排序key的顺序与实际顺序是否一致
        //Record比较函数已经处理了顺序问题。在需求顺序与实际顺序不一致时从后往前查找即可
        var isRight = true
        for ((index, element) in indexInfo.elements.withIndex()) {
            val type = types.getOrNull(index)
            if (type == null || type !is Equal) {
                isRight = element.order != order
                break
            }
        }


        //唯一索引按limit 1的方式处理，单独处理性能提升很有限
        if (indexInfo.type <= INDEX_TYPE_UNIQUE) {
            offset = 0; size = 1
        }

        //寻找左边界，isDesc的情况下寻找右边界
        var pageNo = rootPageNo
        var record:Record

        while (true) {

            val page = reader.read(pageNo)
            val slots = page.slots

            if (page.slots.size > 2) {
                //不需要搜索Infimum和Supremum，结果 + 1保持下标一致
                val slotI = binarySearch(slots.slice(1 until slots.size - 1)
                    , { Record(it, page, indexInfo, tableInfo) }
                    , types
                    , isRight
                ) + 1

                //目标大于本层级所有节点，进入下一级查找
                if (slotI == slots.lastIndex) {
                    if (page.level == 0) {
                        return
                    }
                    pageNo = Record(slots[slots.lastIndex - 1], page, indexInfo, tableInfo).readPageNo()
                    continue
                }

                record = Record(slots[slotI - 1], page, indexInfo, tableInfo)
            } else {
                record = page.getInfimumRecord(indexInfo, tableInfo)
            }

            var lastRecord = record.next()
            record = lastRecord
            var cmp = lastRecord.compareTo(types)
            if (cmp < 0 || isRight && cmp == 0) {
                while (true) {
                    lastRecord = record
                    record = record.next()
                    cmp = record.compareTo(types)
                    if (!isRight && cmp == 0) {
                        break
                    } else if (cmp > 0) {
                        record = lastRecord
                        break
                    }
                }
            }


            // 已经找到叶子节点边界
            if (record.type == RECORD_TYPE_NORMAL) {
                break
            }

            // 查找下一层级
            pageNo = record.readPageNo()
        }

        if (record.compareTo(types) != 0) {
            return
        }
        log.debug("find key: {}", record.keyList.joinToString(",") { it.toString() })

        //页内所有记录都符合，无需比较
        //页内有记录不符合，一边比较一边读取

        while (true) {

        }

        TODO("数据太少的时候会不会出问题")

    }
}