package ibd.struct.type

/**
 * 等值
 */
class Equal(var va: Any) : Type {

    override fun compare(v: Any): Int {
        return (v as Comparable<Any>).compareTo(va)
    }

}