package ibd.core.handler

import ibd.struct.Record
import ibd.struct.sdi.Column
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal

object DecimalHandler : FieldTypesAdapter {

    var log:Logger? = LoggerFactory.getLogger(javaClass)

    override fun readValue0(record: Record, column: Column): Any {
        record.skip(size(column.numeric_scale) + size(column.numeric_precision - column.numeric_scale))
        log?.let {
            log!!.error("Decimal not implemented. See strings/decimal.cc")
            log = null
        }

        return BigDecimal("0.00")
    }

}

private fun size(size: Int): Int {
    var r = 0
    val n = size / 9
    r += (n * 4)
    return r + (size % 9 + 1) / 2
}