package util

object CommonUtil {

    fun bytes2Int16(bytes: List<Byte>): Short {
        return bytes2Int64(bytes).toShort()
    }

    fun bytes2Int32(bytes: List<Byte>): Int {
        return bytes2Int64(bytes).toInt()
    }

    /**
     * 按大端模式把多个字节组成整数
     */
    fun bytes2Int64(bytes: List<Byte>): Long {
        val size = 8.coerceAtMost(bytes.size)
        var r = 0L;
        for (i in 0 until size) {
            r = r or (bytes[i].toLong() and 0xff shl (size - i - 1) * 8)
        }
        return r;
    }

    fun bit2Byte(byte: Byte, offset: Int, size: Int = 1): Byte {
        return ((byte.toInt() shl offset).toByte().toInt() and 0xff shr 8 - size).toByte()
    }

}