package ibd.core.handler

import ibd.const.*
import ibd.struct.Record
import ibd.struct.sdi.Column

/**
 * 实现FieldTypesAdapter，并且添加case，处理不同的mysql数据类型
 * 可以参考 storage/ndb/clusterj/clusterj-tie/src/main/java/com/mysql/clusterj/tie/Utility.java
 */
fun find(type: Int): FieldTypesAdapter {
    return when (type) {

        MYSQL_TYPE_VARCHAR -> VarcharHandler
        MYSQL_TYPE_TINY -> Int8Handler
        MYSQL_TYPE_SHORT -> Int16Handler
        MYSQL_TYPE_LONG -> Int32Handler
        MYSQL_TYPE_LONGLONG -> Int64Handler
        MYSQL_TYPE_TYPED_ARRAY -> DecimalHandler
        MYSQL_TYPE_DATETIME2 -> DateTime2Handler
        MYSQL_TYPE_NEWDATE -> DateHandler

        else -> TODO()
    }
}

interface FieldTypesAdapter {


    fun readValue0(record: Record, column: Column): Any

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