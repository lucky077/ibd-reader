package ibd

import ibd.const.ORDER_ASC
import ibd.core.Executer
import ibd.core.TableManager
import ibd.struct.type.Prefix
import org.slf4j.impl.SimpleLogger


fun main(args: Array<String>) {

    System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "ibd", "warn")
    TableManager.init("/usr/local/mysql/data/test")


    val result = Executer("test")
        .index("name")
        .type(Prefix("hello12345"))
        .limit(10, ORDER_ASC)
        .exec()

    println(result)


}




