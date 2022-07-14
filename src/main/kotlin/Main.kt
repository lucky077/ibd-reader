

import core.Executer
import core.Order
import struct.Range
import util.CommonUtil


fun main(args: Array<String>) {

//    System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "core", "debug")


    Executer("test")
        .primary()
        .type(Range(max = 10000))
        .limit(10, Order.DESC)
        .run()

}