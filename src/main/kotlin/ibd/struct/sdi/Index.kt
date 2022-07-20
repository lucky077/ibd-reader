package ibd.struct.sdi

class Index {

    var name = ""
    var ordinal_position = 0
    var se_private_data = ""
    var type = 0
    var elements = mutableListOf<Element>()
    var varcharCount = 0
    var nullableCount = 0

}