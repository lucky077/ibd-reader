package ibd.struct.sdi

import com.alibaba.fastjson2.annotation.JSONField

class Column {

    var name = ""

    /** @see ibd.const.MYSQL_TYPE_DECIMAL */
    var type = 0
    @JSONField(name = "is_nullable")
    var nullable = false
    @JSONField(name = "is_zerofill")
    var zerofill = false
    @JSONField(name = "is_unsigned")
    var unsigned: Boolean = false
    /** 1:正常字段 2:隐藏字段 */
    var hidden = 0
    var ordinal_position = 0
    var char_length = 0
    var numeric_precision = 0
    var numeric_scale = 0
    var datetime_precision = 0

}