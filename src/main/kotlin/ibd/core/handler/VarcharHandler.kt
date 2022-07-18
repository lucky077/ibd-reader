package ibd.core.handler

import ibd.struct.Record
import ibd.struct.sdi.Column
import java.nio.charset.Charset

object VarcharHandler : FieldTypesAdapter {

    override fun readValue0(record: Record, column: Column): String? {

        val opx = column.ordinal_position - 1

        val len = record.lenList[record.tableInfo.lenMap[column.name]!!]

        return String(record.read(len).toByteArray(), Charset.forName("utf-8"))
    }

}