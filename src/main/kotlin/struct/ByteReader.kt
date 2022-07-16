package struct

/**
 * 继承此类使用bytes2Int32(read(size))解析字节数组中的数据到实体对应字段
 */
open class ByteReader(var data: List<Byte>) {
    var p: Int = 0

    /**
     * 重置指针位置
     */
    fun reset(i: Int) {
        if (i == -1) {
            p = data.size
            return
        }
        p = i
    }

    /**
     * 向前读取
     */
    fun read(size: Int): List<Byte> {
        val r = data.slice(p until p + size)
        p += size
        return r
    }

    /**
     * 向后读取
     */
    fun readReverse(size: Int): List<Byte> {
        val r = data.slice(p - size until p)
        p -= size
        return r
    }
}