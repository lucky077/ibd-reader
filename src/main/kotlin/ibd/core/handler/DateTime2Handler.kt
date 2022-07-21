package ibd.core.handler

import ibd.struct.Record
import ibd.struct.sdi.Column
import ibd.util.bytes2Int64
import java.time.LocalDateTime


object DateTime2Handler : FieldTypesAdapter {

    override fun readValue0(record: Record, column: Column): Any {

        //5~8字节，根据datetime_precision变长
        val size = 5 + (column.datetime_precision + 1) / 2
        val time = bytes2Int64(record.read(size))

        return unpackDatetime2(column.datetime_precision, time)

    }

    private fun unpackDatetime2(precision: Int, packedDatetime2: Long): LocalDateTime {
        val yearMonth = (packedDatetime2 and 0x7FFFC00000000000L ushr 46).toInt() // 17 bits year * 13 + month
        val year = yearMonth / 13
        val month = yearMonth % 13 // calendar month is 0-11
        val day = (packedDatetime2 and 0x00003E0000000000L ushr 41).toInt() // 5 bits day
        val hour = (packedDatetime2 and 0x000001F000000000L ushr 36).toInt() // 5 bits hour
        val minute = (packedDatetime2 and 0x0000000FC0000000L ushr 30).toInt() // 6 bits minute
        val second = (packedDatetime2 and 0x000000003F000000L ushr 24).toInt() // 6 bits second
        val milliseconds = unpackFractionalSeconds(precision, (packedDatetime2 and 0x0000000000FFFFFFL).toInt())
        if (year == 0) {
            return LocalDateTime.now()
        }

        return LocalDateTime.of(year, month, day, hour, minute, second, milliseconds * 1000000)
    }

    private fun unpackFractionalSeconds(precision: Int, fraction: Int): Int {
        return when (precision) {
            0 -> 0
            1 -> (fraction and 0x00FF0000 ushr 16) * 10
            2 -> (fraction and 0x00FF0000 ushr 16) * 10
            3, 4 -> (fraction and 0x00FFFF00 ushr 8) / 10
            5, 6 -> (fraction and 0x00FFFFFF) / 1000
            else -> 0
        }
    }

}