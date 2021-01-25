object ChatService: CrudService<Chat> {
    private var chats = ArrayList<Chat>()

    override fun create(entity: Chat): Long {
        val id = (chats.size + 1).toLong()
        chats.add(entity.copy(id = id))

        return id
    }

    override fun read(): List<Chat> {
        return chats.filter{!it.deleted && MessageService.read(it.id, 0L, 0).isNotEmpty() }
    }

    override fun update(entity: Chat) {
    }

    override fun delete(id: Long) {
        chats.asSequence()
            .filter { it.id == id }
            .map { c -> c.apply { c.deleted = true }}
            .toList()
    }

    override fun restore(id: Long) {
        chats.asSequence()
            .filter { it.id == id }
            .map { c -> c.apply { c.deleted = false } }
            .toList()
    }

    override fun markRead(id: Long) {
    }

    fun createMessage(chatId: Long, ownerId: Long, recipientId: Long, text: String) {
        val message = Message(chatId = chatId, ownerId = ownerId, recipientId = recipientId, text = text)
        MessageService.create(message)
    }

    fun readMessages(chatId: Long, messageId: Long, offset: Int): List<Message> {
        MessageService.markRead(chatId = chatId, messageId = messageId, offset = offset)

        return MessageService.read(chatId = chatId, messageId = messageId, offset = offset)
    }

    fun getUnreadChatsCount(): Int {
        return chats
            .count { chat -> MessageService.read(chat.id, 0L, 0)
                .any { !it.deleted && !it.read}}
    }

    fun deleteAll() {
        chats.asSequence()
            .map { c ->
                c.apply { c.deleted = true };
                c.apply { MessageService.deleteAll(c.id) }}
            .toList()
    }
}