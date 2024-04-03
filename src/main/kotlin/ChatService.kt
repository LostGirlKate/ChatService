package org.example

class ChatService {
    private val userList = mutableListOf<User>()
    private val chatList = mutableListOf<Chat>()
    private val messageList = mutableListOf<Message>()

    private val <E : ObjectWithId> MutableList<E>.maxId: Int
        get() = this.maxOfOrNull { it.id } ?: 0

    private fun <E : ObjectWithId> MutableList<E>.getObjectById(id: Int): E {
        return this.first { it.id == id }
    }

    private fun <E : ObjectWithId> MutableList<E>.checkObjectById(id: Int): Boolean {
        return this.any { it.id == id }
    }

    private fun checkUsers(userIdFirst: Int, userIdSecond: Int = 0) {
        userList.singleOrNull { it.id == userIdFirst }
            .let { it?.id ?: throw UserNotFoundException("Пользователь id $userIdFirst не найден") }
        if (userIdSecond > 0)
            userList.singleOrNull { it.id == userIdSecond }
                .let { it?.id ?: throw UserNotFoundException("Пользователь id $userIdSecond не найден") }
    }

    private fun checkUserInChat(userId: Int, chatId: Int) {
        chatList.singleOrNull { chatId == it.id && it.users.checkObjectById(userId) }
            .let {
                it?.id ?: throw UserNotInThisChatException(
                    "Пользователь id $userId не имеет доступа к чату id $chatId"
                )
            }
    }

    private fun checkChat(chatId: Int) {
        chatList.singleOrNull { it.id == chatId }
            .let { it?.id ?: throw ChatNotFoundException("Чат не найден id: $chatId") }
    }

    private fun checkMessage(messageId: Int) {
        messageList.singleOrNull { it.id == messageId }
            .let { it?.id ?: throw MessageNotFoundException("Сообщение не найдено id: $messageId") }
    }

    private fun getChatIdByUsers(userIdFirst: Int, userIdSecond: Int): Int {
        return chatList.singleOrNull { it.users.checkObjectById(userIdFirst) && it.users.checkObjectById(userIdSecond) }
            .let {
                it?.id ?: throw ChatNotFoundException("Чат не найден userId: $userIdFirst currentUserID: $userIdSecond")
            }
    }


    private fun createChatIfNotExist(author: User, user: User): Int {
        return chatList.firstOrNull { it.users.checkObjectById(author.id) && it.users.checkObjectById(user.id) }?.id
            ?: run {
                chatList.add(Chat("${author.name} && ${user.name}", chatList.maxId + 1, mutableListOf(author, user)))
                chatList.last().id
            }
    }

    private fun messageByID(messageId: Int, currentUserId: Int): Message {
        checkMessage(messageId)
        val message = messageList.getObjectById(messageId)
        checkUsers(currentUserId)
        checkUserInChat(currentUserId, message.chatId)
        checkChat(message.chatId)
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
        val chatId = createChatIfNotExist(author, toUser)
        messageList.add(Message(chatId, text, author, messageList.maxId + 1))
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
        return chatList.asSequence()
            .filter { it.users.checkObjectById(currentUserId) }
            .associate { chat ->
                Pair(chat.id,
                    messageList.filter { it.chatId == chat.id }
                        .maxByOrNull { it.dateCreated }?.text ?: "нет сообщений"
                )
            }
    }

    fun getMessagesListInChat(userId: Int, currentUserId: Int, count: Int = 10): List<Message> {
        checkUsers(currentUserId, userId)
        val chatId = getChatIdByUsers(userId, currentUserId)
        messageList.asSequence()
            .filter { it.chatId == chatId }
            .sortedByDescending { it.dateCreated }
            .take(count)
            .forEach { if (it.author.id != currentUserId) it.isRead = true }
        return messageList.asSequence()
            .filter { it.chatId == chatId }
            .sortedByDescending { it.dateCreated }
            .take(count)
            .ifEmpty { listOf<Message>().asSequence() }
            .toList()
    }


    fun getUnreadChatCount(currentUserId: Int): Int {
        checkUsers(currentUserId)
        return chatList.filter { chat ->
            chat.users.checkObjectById(currentUserId)
                    && messageList.any { it.chatId == chat.id && !it.isRead }
        }.size
    }


}