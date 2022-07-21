package ibd.core.handler

import ibd.struct.Record
import ibd.struct.sdi.Column
import ibd.util.bytes2Int32
import ibd.util.signed

object Int16Handler : FieldTypesAdapter {
    override fun readValue0(record: Record, column: Column): Any {
        return  if (column.unsigned)
            bytes2Int32(record.read(2)).toUInt()
        else
            signed(bytes2Int32(record.read(2)), 2)

    }




}
