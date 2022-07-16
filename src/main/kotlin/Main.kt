import const.ORDER_ASC
import const.PRIMARY_KEY
import core.Executer
import struct.Equal


fun main(args: Array<String>) {

//    System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "core", "debug")


    Executer("test")
//        .primary()
        .index(PRIMARY_KEY)
        .type(Equal(value0 = 1999999))
        .limit(10, ORDER_ASC)
        .run()

}