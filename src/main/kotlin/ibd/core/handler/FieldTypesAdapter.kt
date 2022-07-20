package ibd.core.handler

import ibd.const.MYSQL_TYPE_LONG
import ibd.const.MYSQL_TYPE_VARCHAR
import ibd.struct.Record
import ibd.struct.sdi.Column

/**
 * 实现FieldTypesAdapter，并且添加case，处理不同的mysql数据类型
 */
fun find(type: Int): FieldTypesAdapter {
    return when (type) {

        MYSQL_TYPE_VARCHAR -> VarcharHandler
        MYSQL_TYPE_LONG -> Int32Handler

        else -> TODO()
    }
}

interface FieldTypesAdapter {


    fun readValue0(record: Record, column: Column): Any?

    fun readValueWrapper(record: Record, column: Column): Any? {
        if (isNull(record, column)) {
            return null
        }
        return readValue0(record, column)
    }

}

fun readValue(record: Record, column: Column): Any? {
    return find(column.type).readValueWrapper(record, column)
}

fun isNull(record: Record, column: Column): Boolean {

    if (!column.nullable || record.nullList.isEmpty()) {
        return false
    }

    return if (record.clustered)
        record.nullList[record.tableInfo.nullableMap[column.name]!!]
    else {
        val i = record.indexInfo.elements.first { it.column_opx == column.ordinal_position - 1 }
        record.nullList[i.ordinal_position - 1]
    }
}