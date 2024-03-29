package org.example

open class ObjectWithId(
    open val id: Int = 0,
    val dateCreated: Long = System.currentTimeMillis()
)
