package org.example

data class Chat(
    val title: String,
    override val id: Int,
    val users: MutableList<User> = mutableListOf()
) : ObjectWithId(id)
