package ibd.core.handler

import ibd.struct.Record
import ibd.struct.sdi.Column
import java.nio.charset.Charset

object VarcharHandler : FieldTypesAdapter {

    override fun readValue0(record: Record, column: Column): String? {

        val len = if (record.clustered) {
            record.lenList[record.tableInfo.lenMap[column.name]!!]


        } else {
            val i = record.indexInfo.elements.first { it.column_opx == column.ordinal_position - 1 }
            record.lenList[i.ordinal_position - 1]
        }

        return String(record.read(len).toByteArray(), Charset.forName("utf-8"))
    }

}