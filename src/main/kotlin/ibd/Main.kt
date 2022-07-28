package ibd

import ibd.const.ORDER_ASC
import ibd.const.PRIMARY_KEY
import ibd.core.Executer
import ibd.core.TableManager
import ibd.struct.type.Range
import org.slf4j.impl.SimpleLogger


fun main() {

    System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "ibd", "warn")
    TableManager.init("/usr/local/mysql/data/test")

    val result = Executer("test")
        .index(PRIMARY_KEY)
        .type(Range(min = 1))
        .limit(1, ORDER_ASC)
        .exec()

    println(result)


}




