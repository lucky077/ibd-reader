package core

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import org.slf4j.LoggerFactory
import struct.Type
import util.TableManager

/**
 * 执行逻辑链
 */
class Executer (var tableName: String) {

    val log = LoggerFactory.getLogger(this.javaClass)

    lateinit var index: String
    var types: Map<Int, Type> = mapOf(1 to Type())

    var offset :Int = 0
    var size: Int = 0
    var order: Order = Order.ASC

    fun primary(): Executer {
        return index("PRIMARY")
    }

    fun index(name: String): Executer {
        this.index = name
        return this
    }

    fun type(type: Type) :Executer {
        return type(1 to type)
    }

    fun type(vararg pairs: Pair<Int, Type>): Executer {
       return type(pairs.toMap())
    }

    fun type(types: Map<Int, Type>): Executer {
        this.types = types
        return this;
    }

    /**
     * 排序必须与limit一同使用。
     * 并且只能针对索引中的第一个key，
     * 因为如果第二个key的顺序与期望顺序一致则不用处理，否则难以利用
     */
    fun limit(offset :Int, size: Int = 0, order: Order = Order.ASC): Executer {
        this.offset = offset
        this.size = size
        this.order = order
        return this
    }

    fun limit(size: Int = 0, order: Order = Order.ASC): Executer {
        return limit(0, size, order)
    }

    fun run() {
        val table = TableManager.load(tableName)
        val reader = table.reader
        val indexObj = table.indexes.first { (it as JSONObject).getString("name") == index } as JSONObject

        //获取索引根页号
        val rootPageNo = indexObj.getString("se_private_data").split(";")[1].split("=")[1].toInt()
        log.debug("index root pageNo: {}", rootPageNo)

        val rootPage = reader.read(rootPageNo)

        log.info(JSON.toJSONString(rootPage))

        // TODO: 2022/7/14 slots的顺序 null list的顺序

    }
}

enum class Order(var VAL: Int) {
    ASC(2),DESC(3)
}