package org.example

data class Message(
    val chatId: Int,
    val text: String,
    val author: User,
    override val id: Int,
    var isRead: Boolean = false
) : ObjectWithId(id)
