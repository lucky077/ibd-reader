package struct

/**
 * 范围
 */
class Range(val min: Any? = null, val max: Any? = null) : Type {


    override fun compare(v: Any): Int {
        if (min != null) {
            val r = (v as Comparable<Any>).compareTo(min)
            if (r <= 0) {
                return r
            }
        }
        if (max != null) {
            val r = (v as Comparable<Any>).compareTo(max)
            if (r <= 0) {
                return 0
            }
        }

        return 1
    }

}