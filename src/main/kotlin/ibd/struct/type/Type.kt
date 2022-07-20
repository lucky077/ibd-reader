package ibd.struct.type

/**
 * 使用索引方式
 */
interface Type {

    fun compare0(v: Any): Int
    fun compare(v: Any?): Int = if (v == null) -1 else compare0(v)

}