package org.example

fun main() {
    {
        val service = ChatService()
        val currentUserId = service.createUser("Я")
        val friend1Id = service.createUser("friend1")
        val friend2Id = service.createUser("friend2")
        val messageId = service.createMessage("Привет", currentUserId, friend1Id)
        println(service.getUnreadChatCount(currentUserId))
        println(service.getMessagesListInChat(friend1Id, currentUserId))
        println(service.getChatList(currentUserId))
        println(service.getMessagesListInChat(currentUserId, friend1Id))
        println(service.getUnreadChatCount(currentUserId))
        service.editMessage(messageId, "Привет, как дела?", currentUserId)
        println(service.getMessagesListInChat(friend1Id, currentUserId))
        val message2Id = service.createMessage("Привет", friend1Id, currentUserId)
        println(service.getUnreadChatCount(currentUserId))
        println(service.getMessagesListInChat(friend1Id, currentUserId))
        service.deleteMessage(message2Id, currentUserId)
        println(service.getMessagesListInChat(friend1Id, currentUserId))
        println(service.getLastMessagesInChats(currentUserId))
    }

    val service = ChatService()
    val currentUser = service.createUser("Пользователь 1")
    val user1 = service.createUser("Пользователь 2")
    val user2 = service.createUser("Пользователь .3")
    service.createMessage("Привет", currentUser, user1)
    service.createMessage("Привет2", currentUser, user1)
    service.createMessage("Привет3", currentUser, user2)
    service.createMessage("Привет5", currentUser, user2)
    service.createMessage("Привет6", currentUser, user2)
    Thread.sleep(100)
    service.createMessage("Привет7", currentUser, user2)
    service.deleteMessage(1,currentUser)
    service.deleteMessage(2,currentUser)
    println(service.getMessagesListInChat(user2, currentUser))
    println(service.getLastMessagesInChats(currentUser))

}