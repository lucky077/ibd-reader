package ibd

import ibd.const.ORDER_ASC
import ibd.const.PRIMARY_KEY
import ibd.core.Executer
import ibd.core.TableManager
import ibd.struct.type.Equal
import ibd.struct.type.Range
import ibd.util.testStart
import org.slf4j.impl.SimpleLogger


fun main(args: Array<String>) {

    System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "ibd", "warn")
    TableManager.init("/usr/local/mysql/data/synj_zt")
    TableManager.init("/usr/local/mysql/data/test")
    Executer("test").index(PRIMARY_KEY).type(Equal(1234567)).exec()
    testStart = true



    val result = Executer("test")
        .index(PRIMARY_KEY)
        .type(Range(min = 500000))
        .limit(1000, ORDER_ASC)
        .exec()
    println(result)




}




