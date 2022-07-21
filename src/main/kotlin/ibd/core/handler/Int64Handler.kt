package ibd.core.handler

import ibd.struct.Record
import ibd.struct.sdi.Column
import ibd.util.bytes2Int64
import ibd.util.signed

object Int64Handler : FieldTypesAdapter {
    override fun readValue0(record: Record, column: Column): Any {
        return  if (column.unsigned)
            bytes2Int64(record.read(8)).toULong()
        else
            signed(bytes2Int64(record.read(8)))

    }




}
