package com.durakcheat.engine

fun <T> ArrayList(size: Int, init: (Int) -> T) = ArrayList<T>(size).apply {
    for(i in 0..<size)
        add(init(i))
}

fun <T> List<T>.with1Affected(at: Int, affect: T.() -> T) = ArrayList(size){
    val p = this[it]
    if (it == at) affect(p) else p
}

fun <T> List<T>.with1Affected(where: (T) -> Boolean, affect: T.() -> T) = ArrayList(size){
    val p = this[it]
    if (where(p)) affect(p) else p
}

fun <T> ArrayList<T>.map(transform: (T) -> T) = ArrayList(size){
    transform(this[it])
}

fun <T> ArrayList<T>.mapIndexed(transform: (Int, T) -> T) = ArrayList(size){
    transform(it, this[it])
}

fun <T> Iterable<T>.indexOfFirstOrNull(predicate: (T) -> Boolean) =
    this.indexOfFirst(predicate).let { if(it >= 0) it else null }