package ibd.core

import ibd.const.*
import ibd.struct.Record
import ibd.struct.type.Equal
import ibd.struct.type.Type
import ibd.util.binarySearch
import org.slf4j.LoggerFactory
import java.util.*


private val EMPTY_LIST = listOf<Map<String, Any>>()
/**
 * 执行逻辑链
 */
class Executer(var tableName: String) {

    private val log = LoggerFactory.getLogger(this.javaClass)

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

    fun exec(): List<Map<String, Any>> {

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

        //找到首个非等值的条件,判断实际应用的排序key的顺序与实际顺序是否一致:isRight
        //isRight决定了有多个值命中时候边界在左还是右
        var isRight = false
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
        var pageNo = rootPageNo
        var record: Record

        var stack: Deque<Record> = LinkedList()

        l1@ while (true) {

            val page = reader.read(pageNo)
            val slots = page.slots

//            printSlotsKeys(slots, page, indexInfo, tableInfo)

            //数量太少时不使用二分查找
            if (page.slots.size < 17) {
                record = page.getInfimumRecord(indexInfo, tableInfo)
            } else {

                val slotI = binarySearch(slots,
                    { Record(it, page, indexInfo, tableInfo) },
                    types,
                    isRight
                )

                //目标大于当前level所有节点，准备进入下一个level查找
                if (slotI == slots.lastIndex) {
                    val record1 = Record(slots[slots.lastIndex - 1], page, indexInfo, tableInfo)
                    //通常这个最后slot指向的记录就是本页最后的记录，但索引desc的情况下有记录不进组，必须遍历散落的记录
                    if (record1.next().type == RECORD_TYPE_SUPREMUM) {
                        if (page.level == 0) {
                            return EMPTY_LIST
                        }
                        pageNo = record1.readPageNo()
                        continue
                    }
                }

                record = Record(slots[slotI - 1], page, indexInfo, tableInfo)
            }

            var lastRecord:Record
            while (true) {
                lastRecord = record
                record = record.next()
                val cmp = record.compareTo(types)
                //可能的值一定在上一个; isRight只能通过cmp > 0结束
                if (cmp > 0) {
                    record = lastRecord
                    break
                }
                if (!isRight && cmp == 0) {
                    //当前和上一个都可能包括边界，先处理上一个,这里使用stack保证优先左边界
                    if (lastRecord.type != RECORD_TYPE_INFIMUM) {
                        stack.push(record)
                        record = lastRecord
                    }
                    break
                }
            }

            if (record.type == RECORD_TYPE_INFIMUM) {
                return EMPTY_LIST
            }

            if (record.type == RECORD_TYPE_NORMAL) {
                // 已经找到叶子节点边界
                if (record.compareTo(types) == 0) {
                    break
                }
                if (stack.isEmpty()) {
                    return EMPTY_LIST
                }
                //pop之前的多种可能的节点
                while (true) {
                    record = stack.pop()
                    if (record.type == RECORD_TYPE_NON_LEAF) {
                        break
                    }
                    // 已经找到叶子节点边界（只有cmp == 0才能push到stack）
                    break@l1
                }
            }
            // 查找下一个level
            pageNo = record.readPageNo()
        }


        log.debug("find key: {}", record.keyList.joinToString(",") { it.toString() })

        return listOf(mapOf("key" to record.keyList.first()))

        //页内所有记录都符合，无需比较
        //页内有记录不符合，一边比较一边读取

        while (true) {

        }

    }
}