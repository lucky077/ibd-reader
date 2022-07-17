package core.handler

import core.FieldTypesAdapter
import struct.Record
import struct.sdi.Column
import util.bytes2Int32

object Int32Handler : FieldTypesAdapter {
    override fun readValue0(record: Record, column: Column): Any? =

        if (column.is_unsigned)
            bytes2Int32(record.read(4)).toUInt()
        else
            bytes2Int32(record.read(4)) shl 1 shr 1


}