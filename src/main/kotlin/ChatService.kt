package org.example

class ChatService {
    private val userList = mutableListOf<User>()
    private val chatList = mutableListOf<Chat>()
    private val messageList = mutableListOf<Message>()

    private val <E : ObjectWithId> MutableList<E>.maxId: Int
        get() = if (this.size > 0) this.maxOf { it.id } else 0

    private fun <E : ObjectWithId> MutableList<E>.getObjectById(id: Int): E {
        return this.first { it.id == id }
    }

    private fun <E : ObjectWithId> MutableList<E>.checkObjectById(id: Int): Boolean {
        return this.any { it.id == id }
    }

    private fun checkUsers(userIdFirst: Int, userIdSecond: Int = 0) {
        if (!userList.checkObjectById(userIdFirst)) throw UserNotFoundException("Пользователь id $userIdFirst не найден")
        if (userIdSecond > 0 && !userList.checkObjectById(userIdSecond)) throw UserNotFoundException("Пользователь id $userIdSecond не найден")
    }

    private fun checkUserInChat(userId: Int, chatId: Int) {
        if (!chatList.getObjectById(chatId).users.checkObjectById(userId)) throw UserNotInThisChatException(
            "Пользователь id $userId не имеет доступа к чату id $chatId"
        )
    }

    private fun checkChat(chatId: Int) {
        if (!chatList.checkObjectById(chatId)) throw ChatNotFoundException("Чат не найден id: $chatId")
    }

    private fun checkMessage(messageId: Int) {
        if (!messageList.checkObjectById(messageId)) throw MessageNotFoundException("Сообщение не найдено id: $messageId")

    }

    private fun getChatIdByUsers(userIdFirst: Int, userIdSecond: Int): Int {
        if (!chatList.any { it.users.checkObjectById(userIdFirst) && it.users.checkObjectById(userIdSecond) })
            throw ChatNotFoundException("Чат не найден userId: $userIdFirst currentUserID: $userIdSecond")

        return chatList.first { it.users.checkObjectById(userIdFirst) && it.users.checkObjectById(userIdSecond) }.id
    }


    private fun createChatIfNotExist(author: User, user: User): Int {
        if (!chatList.any { it.users.checkObjectById(author.id) && it.users.checkObjectById(user.id) }) {
            chatList.add(Chat("${author.name} && ${user.name}", chatList.maxId + 1, mutableListOf(author, user)))
            return chatList.last().id
        } else {
            return chatList.first { it.users.checkObjectById(author.id) && it.users.checkObjectById(user.id) }.id
        }

    }

    private fun messageByID(messageId: Int, currentUserId: Int): Message {
        checkMessage(messageId)
        val message = messageList.getObjectById(messageId)
        checkUsers(currentUserId)
        checkChat(message.chatId)
        checkUserInChat(currentUserId, message.chatId)
        return message
    }

    fun createUser(name: String): Int {
        userList.add(User(name, userList.maxId + 1))
        return userList.last().id
    }

    fun deleteChat(chatId: Int, currentUserId: Int): Boolean {
        checkUsers(currentUserId)
        checkChat(chatId)
        checkUserInChat(currentUserId, chatId)
        messageList.removeAll { it.chatId == chatId }
        return chatList.removeAll { it.id == chatId }
    }


    fun createMessage(text: String, currentUserId: Int, toUserId: Int): Int {
        checkUsers(currentUserId, toUserId)
        val author = userList.getObjectById(currentUserId)
        val toUser = userList.getObjectById(toUserId)
        val chat = chatList.getObjectById(createChatIfNotExist(author, toUser))
        messageList.add(Message(chat.id, text, author, messageList.maxId + 1))
        return messageList.last().id
    }

    fun deleteMessage(messageID: Int, currentUserID: Int): Boolean {
        val message = messageByID(messageID, currentUserID)
        return messageList.removeAll { it.id == message.id }
    }

    fun editMessage(messageId: Int, text: String, currentUserId: Int): Boolean {
        val message = messageByID(messageId, currentUserId)
        val messageIndex = messageList.indexOfFirst { it.id == messageId }
        val editedMessage = message.copy(text = text)
        messageList[messageIndex] = editedMessage
        return messageList[messageIndex] == editedMessage
    }

    fun getChatList(currentUserId: Int): List<Chat> {
        checkUsers(currentUserId)
        return chatList.filter { it.users.checkObjectById(currentUserId) }
    }

    fun getLastMessagesInChats(currentUserId: Int): Map<Int, String> {
        checkUsers(currentUserId)
        val resultMap = mutableMapOf<Int, String>()
        chatList.filter { it.users.checkObjectById(currentUserId) }.forEach { chat ->
            if (messageList.none { it.chatId == chat.id })
                resultMap[chat.id] = "нет сообщений"
            else
                resultMap[chat.id] = messageList.filter { it.chatId == chat.id }.maxByOrNull { it.dateCreated }!!.text
        }
        return resultMap
    }

    fun getMessagesListInChat(userId: Int, currentUserId: Int, count: Int = 10): List<Message> {
        checkUsers(currentUserId, userId)
        val chatId = getChatIdByUsers(userId, currentUserId)
        if (!messageList.any { it.chatId == chatId })
            return listOf()
        else {
            val lastShowIndex =
                if (count > 0 && count <= messageList.filter { it.chatId == chatId }.size) count - 1 else messageList.filter { it.chatId == chatId }.lastIndex
            val resultList = messageList.filter { it.chatId == chatId }.sortedByDescending { it.dateCreated }
                .slice(0..lastShowIndex)
            messageList.filter { it.chatId == chatId }.sortedByDescending { it.dateCreated }
                .slice(0..lastShowIndex).forEach { if (it.author.id != currentUserId) it.isRead = true }
            return resultList
        }
    }


    fun getUnreadChatCount(currentUserId: Int): Int {
        checkUsers(currentUserId)
        return chatList.filter { chat ->
            chat.users.checkObjectById(currentUserId)
                    && messageList.any {
                it.chatId == chat.id
                        && !it.isRead
            }
        }.size
    }


}