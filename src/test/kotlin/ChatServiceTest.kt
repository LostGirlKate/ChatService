import org.example.*
import org.junit.Test

import org.junit.Assert.*

class ChatServiceTest {

    @Test
    fun createUserTest() {
        val service = ChatService()
        val result = service.createUser("Пользователь 1")
        assertEquals(result, 1)
    }

    @Test
    fun createMessageTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        val result = service.createMessage("Привет", currentUser, user1)
        assertEquals(result, 1)
    }

    @Test(expected = UserNotFoundException::class)
    fun createMessageUserNotFoundExceptionTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        service.createMessage("Привет", currentUser, 3)
    }

    @Test
    fun deleteMessageTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        val messageID = service.createMessage("Привет", currentUser, user1)
        val result = service.deleteMessage(messageID, currentUser)
        assertTrue(result)
    }

    @Test(expected = MessageNotFoundException::class)
    fun deleteMessageNotFoundExceptionTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        service.createMessage("Привет", currentUser, user1)
        service.deleteMessage(13, currentUser)
    }

    @Test
    fun deleteChatTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        service.createMessage("Привет", currentUser, user1)
        val result = service.deleteChat(service.getChatList(currentUser).first().id, currentUser)
        assertTrue(result)
    }

    @Test(expected = ChatNotFoundException::class)
    fun deleteChatNotFoundExceptionTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        service.createMessage("Привет", currentUser, user1)
        service.deleteChat(10, currentUser)
    }


    @Test
    fun editMessageTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        val messageId = service.createMessage("Привет", currentUser, user1)
        val result = service.editMessage(messageId, "test", currentUser)
        assertTrue(result)
    }

    @Test(expected = UserNotInThisChatException::class)
    fun editMessageUserNotInThisChatExceptionTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        val user2 = service.createUser("Пользователь 3")
        val messageId = service.createMessage("Привет", currentUser, user1)
        service.editMessage(messageId, "test", user2)
    }


    @Test
    fun getChatListTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        val user2 = service.createUser("Пользователь 3")
        service.createMessage("Привет", currentUser, user1)
        service.createMessage("Привет2", currentUser, user1)
        service.createMessage("Привет3", currentUser, user2)
        service.createMessage("Привет5", currentUser, user2)
        val result = service.getChatList(currentUser)
        val listWanted = listOf(
            result[0].copy(
                id = 1,
                title = "Пользователь 1 && Пользователь 2",
                users = mutableListOf(User("Пользователь 1", currentUser), User("Пользователь 2", user1))
            ),
            result[1].copy(
                id = 2,
                title = "Пользователь 1 && Пользователь 3",
                users = mutableListOf(User("Пользователь 1", currentUser), User("Пользователь 3", user2))
            )
        )
        assertEquals(result, listWanted)
    }

    @Test
    fun getLastMessagesInChatsTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        val user2 = service.createUser("Пользователь 3")
        service.createMessage("Привет", currentUser, user1)
        Thread.sleep(100)
        service.createMessage("Привет2", currentUser, user1)
        service.createMessage("Привет3", currentUser, user2)
        Thread.sleep(100)
        service.createMessage("Привет5", currentUser, user2)
        val result = service.getLastMessagesInChats(currentUser)
        val listWanted = mapOf(Pair(1, "Привет2"), Pair(2, "Привет5"))
        assertEquals(result, listWanted)
    }

    @Test
    fun getLastMessagesInChatsNoMessageTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        val user2 = service.createUser("Пользователь 3")
        service.createMessage("Привет", currentUser, user1)
        Thread.sleep(100)
        service.createMessage("Привет2", currentUser, user1)
        service.createMessage("Привет3", currentUser, user2)
        Thread.sleep(100)
        service.createMessage("Привет5", currentUser, user2)
        service.deleteMessage(1, currentUser)
        service.deleteMessage(2, currentUser)
        val result = service.getLastMessagesInChats(currentUser)
        val listWanted = mapOf(Pair(1, "нет сообщений"), Pair(2, "Привет5"))
        assertEquals(result, listWanted)
    }

    @Test
    fun getMessagesListInChatDefaultTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        val user2 = service.createUser("Пользователь .3")
        service.createMessage("Привет", currentUser, user1)
        service.createMessage("Привет2", currentUser, user1)
        service.createMessage("Привет3", currentUser, user2)
        service.createMessage("Привет5", currentUser, user2)
        val result = service.getMessagesListInChat(user2, currentUser)
        val listWanted = listOf(
            result[0].copy(id = 3, text = "Привет3", chatId = 2, author = User("Пользователь 1", 1), isRead = false),
            result[1].copy(id = 4, text = "Привет5", chatId = 2, author = User("Пользователь 1", 1), isRead = false)
        )
        assertEquals(result, listWanted)
    }


    @Test(expected = ChatNotFoundException::class)
    fun getMessagesListInChatNotFoundExceptionTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        val user2 = service.createUser("Пользователь .3")
        service.createMessage("Привет", currentUser, user1)
        service.createMessage("Привет2", currentUser, user1)
        service.createMessage("Привет3", currentUser, user2)
        service.createMessage("Привет5", currentUser, user2)
        service.deleteChat(2, currentUser)
        service.getMessagesListInChat(user2, currentUser)
    }


    @Test
    fun getUnreadChatCountTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        service.createMessage("Привет", currentUser, user1)
        service.createMessage("Привет2", currentUser, user1)
        service.createMessage("Привет3", currentUser, user1)
        val result = service.getUnreadChatCount(user1)
        assertEquals(result, 1)
    }

    @Test
    fun getUnreadChatCountNeedZeroTest() {
        val service = ChatService()
        val currentUser = service.createUser("Пользователь 1")
        val user1 = service.createUser("Пользователь 2")
        service.createMessage("Привет", currentUser, user1)
        service.createMessage("Привет2", currentUser, user1)
        service.createMessage("Привет3", currentUser, user1)
        service.getMessagesListInChat(currentUser, user1)
        val result = service.getUnreadChatCount(user1)
        assertEquals(result, 0)
    }
}