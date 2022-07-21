package ibd.core.handler

import ibd.struct.Record
import ibd.struct.sdi.Column
import ibd.util.bytes2Int32
import ibd.util.signed
import java.time.LocalDate


object DateHandler : FieldTypesAdapter {

    override fun readValue0(record: Record, column: Column): Any {
        val r = unpackDate(signed(bytes2Int32(record.read(3)), 3))
        return r
    }

    fun unpackDate(packedDate: Int): LocalDate {
        var packedDate = packedDate
        val date = packedDate and 0x1f
        packedDate = packedDate ushr 5
        val month = (packedDate and 0x0f) // Month value is 0-based. e.g., 0 for January.
        val year = packedDate ushr 4 and 0x7FFF
        return LocalDate.of(year, month, date)
    }
}