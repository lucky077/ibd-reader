package ibd.core.handler

import ibd.const.MYSQL_TYPE_LONG
import ibd.const.MYSQL_TYPE_VARCHAR
import ibd.struct.Record
import ibd.struct.sdi.Column

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

    return column.is_nullable && record.nullList.isNotEmpty() && record.nullList[record.tableInfo.nullableMap[column.name]!!]
}