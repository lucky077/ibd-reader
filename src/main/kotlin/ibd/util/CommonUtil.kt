package ibd.util

import ibd.struct.Record
import ibd.struct.type.Type


fun bytes2Int32(bytes: List<Byte>): Int {
    return bytes2Int64(bytes).toInt()
}

/**
 * 按大端模式把多个字节组成整数
 */
fun bytes2Int64(bytes: List<Byte>): Long {
    val size = 8.coerceAtMost(bytes.size)
    var r = 0L
    for (i in 0 until size) {
        r = r or (bytes[i].toLong() and 0xff shl (size - i - 1) * 8)
    }
    return r
}

fun bit2Bool(byte: Byte, offset: Int): Boolean {
    return bit2Int(byte, offset, 1) == 1
}

fun bit2Int(byte: Byte, offset: Int, size: Int = 1): Int {
    return (byte.toInt() shl offset).toByte().toInt() and 0xff shr 8 - size
}

/**
 * 返回与目标等值最左/最右的下标
 * 如果目标不存在,返回按顺序应该存在的位置
 */
//fun binarySearch(list: List<Int>, target: Int, isRight: Boolean): Int {
//    var low = 0
//    var high = list.size - 1
//    var mid = 0
//    var find = false
//    var cmp = 0
//    while (low <= high) {
//        mid = (low + high).ushr(1)
//        val midVal = list[mid]
//        cmp = compareValues(midVal, target)
//        if (!find && cmp == 0) find = true
//        if (isRight) {
//            if (cmp <= 0)
//                low = mid + 1
//            else
//                high = mid - 1
//        } else {
//            if (cmp < 0)
//                low = mid + 1
//            else
//                high = mid - 1
//        }
//    }
//    if (find && cmp > 0) {
//        return mid - 1
//    }
//    if (cmp < 0){
//        return mid + 1
//    }
//    return mid
//}

/**
 * 查找page directory,返回与条件匹配的slot
 * 都不匹配返回slot应该所在的位置
 */
fun binarySearch(list: List<Int>, getRecord: (slot: Int) -> Record, target: List<Type>, isRight: Boolean): Int {

    var low = 0
    var high = list.size - 1
    var mid = 0
    var find = false
    var cmp = 0
    while (low <= high) {
        mid = (low + high).ushr(1)
        val midVal = getRecord(list[mid])
        cmp = midVal.compareTo(target)
        if (!find && cmp == 0) find = true
        if (isRight) {
            if (cmp <= 0)
                low = mid + 1
            else
                high = mid - 1
        } else {
            if (cmp < 0)
                low = mid + 1
            else
                high = mid - 1
        }
    }
    if (find && cmp > 0) {
        return mid - 1
    }
    if (cmp < 0){
        return mid + 1
    }
    return mid
}