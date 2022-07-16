package struct.sdi

class Column {

    var name = ""

    /** @see const.MYSQL_TYPE_DECIMAL */
    var type = 0
    var is_nullable = false
    var is_zerofill = false
    var is_unsigned = false
    var hidden = 0
    var ordinal_position = 0
    var char_length = 0

}