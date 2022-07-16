package struct

/**
 * 等值
 */
class Equal(var value0: Any) : Type {

    override fun compare(v: Any): Int {
        return (v as Comparable<Any>).compareTo(value0)
    }

}