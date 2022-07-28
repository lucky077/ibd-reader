package ibd.core

import ibd.const.*
import ibd.struct.IndexPage
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

    fun type(types: List<Type>): Executer {
        var validSize = 0
        for (type in types) {
            validSize++
            if (type !is Equal) {
                break
            }
        }
        //非等值条件的下一个失效
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

    /**
     * 核心查找逻辑
     */
    fun exec(): List<Map<String, Any?>> {

        val table = TableManager.load(tableName)
        val reader = table.reader
        val indexInfo = table.tableInfo.indexes.firstOrNull { it.name == index } ?: throw RuntimeException("Please check index name")
        val tableInfo = table.tableInfo

        //获取索引根页号
        var pageNo = indexInfo.se_private_data.split(";")[1].split("=")[1].toInt()
        log.debug("index root pageNo: {}", pageNo)

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
        if (indexInfo.type <= INDEX_TYPE_UNIQUE && types.isNotEmpty() && types.all { it is Equal }) {
            offset = 0; size = 1
        }
        var page:IndexPage
        var record: Record

        val stack: Deque<Record> = LinkedList()

        //在索引中寻找叶子节点边界
        l1@ while (true) {

            page = reader.read(pageNo)
            val slots = page.slots

            //数量太少时不使用二分查找，直接从最小记录开始遍历
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
                    //通常这个最后的slot指向的记录就是本页最后的记录，但索引desc的情况下有记录不进组，必须遍历散落的记录
                    if (record1.next().type == RECORD_TYPE_SUPREMUM) {
                        if (page.level == 0) {
                            return EMPTY_LIST
                        }
                        pageNo = record1.readPageNo()
                        continue
                    }
                }
                //因为slot指向的是组内最后的记录，所以从上一个slot指向的记录开始
                record = Record(slots[slotI - 1], page, indexInfo, tableInfo)
            }


            var lastRecord:Record
            while (true) {
                lastRecord = record
                record = record.next()
                val cmp = record.compareTo(types)
                //可能的值一定在上一个; isRight的情况只能通过cmp > 0结束
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

            //要查找的目标在所有数据之前，目标不存在
            if (record.type == RECORD_TYPE_INFIMUM) {
                return EMPTY_LIST
            }

            // 已经到达叶子节点边界
            if (record.type == RECORD_TYPE_NORMAL) {
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

        if (size == 1 && offset == 0) {
            return listOf(record.toObject())
        }

        //下面取数据粗写了，比如offset应该遍历页

        //未处理的计数
        var size0 = -1
        var offset0 = -1
        if (size > 0 || offset > 0) {
            offset0 = offset
        }
        if (size > 0) size0 = size

        val result = mutableListOf<Map<String, Any?>>()

        if (!isRight) {
            if (offset0 > 0) offset0--
            else {
                result.add(record.toObject())
                if (size0 > 0) size0--
            }
            while (size0 != 0 || offset0 != 0) {

                record = record.next()
                if (record.type == RECORD_TYPE_SUPREMUM) {
                    if (page.next <= 0) {
                        break
                    }
                    record = reader.read(page.next).getInfimumRecord(indexInfo, tableInfo)
                    continue
                }
                val cmp = record.compareTo(types)
                if (cmp != 0) {
                    break
                }

                if (offset0 > 0) offset0--
                else {
                    result.add(record.toObject())
                    if (size0 > 0) size0--
                }
            }

            return result
        }

        val temp = mutableListOf<Record>()
        var hasPrevPage = true
        record = page.getInfimumRecord(indexInfo, tableInfo)

        l2@ while (hasPrevPage) {
            while (true) {
                record = record.next()
                val cmp = record.compareTo(types)
                if (cmp > 0) {
                    break
                }
                if (cmp == 0) {
                    temp.add(record)
                } else {
                    hasPrevPage = false
                }
            }
            temp.reverse()
            for (record in temp) {
                if (size0 == 0 && offset0 == 0) break@l2

                if (offset0 > 0) offset0--
                else {
                    result.add(record.toObject())
                    if (size0 > 0) size0--
                }
            }
            temp.clear()
            if (page.prev <= 0) break
            record = reader.read(page.prev).getInfimumRecord(indexInfo, tableInfo)
        }



        return result
    }
}