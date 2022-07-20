package ibd.struct.sdi

class TableInfo {
    var row_format = 0
    val indexes = mutableListOf<Index>()
    val columns = mutableListOf<Column>()
    val nullableMap = mutableMapOf<String, Int>()
    val lenMap = mutableMapOf<String, Int>()
    var varcharCount = 0
}