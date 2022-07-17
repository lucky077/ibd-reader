package struct

import java.nio.charset.Charset

class Prefix(val prefix: String) : Type {

    /**
     * 字符串前缀匹配使用范围，例：abc% 只需要查找大于等于abc并且小于abd
     */
    override fun compare(v: Any): Int {
        if (v !is String) throw IllegalArgumentException("prefix use only to string")
        val min = prefix
        val bytes = prefix.encodeToByteArray()
        bytes[bytes.size - 1]++
        //不包含max本身，仅小于max
        val max = String(bytes, Charset.forName("utf-8"))

        var r = v.compareTo(min)
        if (r <= 0) {
            return r
        }
        r = v.compareTo(max)
        if (r < 0) {
            return 0
        }

        return 1

    }


}