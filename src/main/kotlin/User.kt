package org.example

data class User(
    val name: String,
    override val id: Int,
) : ObjectWithId(id)
