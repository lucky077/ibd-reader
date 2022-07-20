# ibd-reader

一个直接读取数据库物理文件查询器，粗写的，仅用于理解innodb索引结构。

```
TableManager.init("/usr/local/mysql/data/test")

val result = Executer("test")
        .index(PRIMARY_KEY)
        .type(Range(min = 500000))
        .limit(10, ORDER_ASC)
        .exec()

    println(result)
```  
