# ibd-reader

一个直接读取数据库物理文件查询器，粗写的，仅用于理解innodb索引结构。

包含 等值、范围、模糊、排序、limit功能，仅实现可以利用索引查找的情况（不包括explain type=index）

使用精简的api运行：
```
TableManager.init("/usr/local/mysql/data/test")

val result = Executer("test")
        .index(PRIMARY_KEY)
        .type(Range(min = 500000))
        .limit(10, ORDER_ASC)
        .exec()

    println(result)
```  
