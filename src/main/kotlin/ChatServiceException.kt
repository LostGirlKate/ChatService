package org.example

sealed class ChatServiceException(override val message: String?) : Exception(message)

class ChatNotFoundException(message: String?) : ChatServiceException(message)
class MessageNotFoundException(message: String?) : ChatServiceException(message)
class UserNotFoundException(message: String?) : ChatServiceException(message)
class UserNotInThisChatException(message: String?) : ChatServiceException(message)