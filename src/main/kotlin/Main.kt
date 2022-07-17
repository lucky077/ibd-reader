
import const.ORDER_DESC
import const.PRIMARY_KEY
import core.Executer
import org.slf4j.impl.SimpleLogger
import struct.Equal
import util.binarySearch


fun main(args: Array<String>) {

    System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "core", "debug")

    val binarySearch = binarySearch(
        listOf(2,3,4), 3, false
    )
//    println(binarySearch + 1)



    Executer("test2")
//        .primary()
        .index(PRIMARY_KEY)
        .type(Equal("2"))
        .limit(10, ORDER_DESC)
        .run()




}




