package core

import const.INDEX_TYPE_UNIQUE
import const.ORDER_ASC
import const.ORDER_DESC
import org.slf4j.LoggerFactory
import struct.Equal
import struct.Record
import struct.Type
import util.TableManager

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

        //获取索引根页号
        val rootPageNo = indexInfo.se_private_data.split(";")[1].split("=")[1].toInt()
        log.debug("index root pageNo: {}", rootPageNo)

        val rootPage = reader.read(rootPageNo)

        //条件数量不能超过索引key数量
        val keyCount = indexInfo.elements.filter { !it.hidden }.size
        if (types.size > keyCount) types = types.slice(0 until keyCount)

        //找到首个非等值的条件,判断实际应用的排序key的顺序与实际顺序是否一致
        //Record比较函数已经处理了顺序问题。在需求顺序与实际顺序不一致时从后往前查找即可
        var isDesc = true
        for ((index, element) in indexInfo.elements.withIndex()) {
            val type = types.getOrNull(index)
            if (type == null || type !is Equal) {
                isDesc = element.order != order
                break
            }
        }


        //唯一索引与limit 1一同处理
        if (indexInfo.type <= INDEX_TYPE_UNIQUE) {
            offset = 0; size = 1
        }

        //寻找左边界，isDesc的情况下寻找右边界
        for (slot in rootPage.slots.slice(1 until rootPage.slots.size - 1)) {
            val record = Record(rootPage, slot, indexInfo, table.tableInfo)
            record.compareTo(types)
        }


        // TODO: 2022/7/14 联合key处理


    }
}