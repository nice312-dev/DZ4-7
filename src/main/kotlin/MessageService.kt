object MessageService: CrudService<Message> {
    private var messages = ArrayList<Message>()

    override fun create(entity: Message): Long {
        val id = (messages.size + 1).toLong()
        var chatId = entity.chatId

        if(chatId == 0L) {
            chatId = ChatService.create(Chat(id = 0L))
        }

        messages.add(entity.copy(id = id, chatId = chatId))
        return id
    }

    override fun read(): List<Message> {
        return messages.filter { !it.deleted }
    }

    override fun update(entity: Message) {
        val message = entity.copy(text = entity.text)

        messages.remove(entity)
        messages.add(message)
    }

    override fun delete(id: Long) {
        val message = messages.filter { id == id }[0]
        message.deleted = true

        when {
            ChatService.readMessages(message.chatId, 0L, 0).isEmpty() -> {
                ChatService.delete(message.chatId)
            }
        }
    }

    override fun restore(id: Long) {
        messages.filter { id == id }[0].deleted = false
    }

    override fun markRead(id: Long) {
        messages.filter { it.id == id }[0].read = true
    }

    fun read(chatId: Long, messageId: Long, offset: Int): List<Message> {
        return when(offset) {
            0 -> messages.asSequence()
                .filter { it.chatId == chatId }
                .filter { it.id >= messageId }
                .toList()
            else -> messages.asSequence()
                .filter { it.chatId == chatId }
                .filter { it.id >= messageId }
                .take(offset)
                .toList()
        }
    }

    fun markRead(chatId: Long, messageId: Long, offset: Int) {
        read(chatId = chatId, messageId = messageId, offset = offset).asSequence()
            .map { m -> m.apply { markRead(m.id) } }
            .toList()
    }

    fun deleteAll(chatId: Long) {
        messages.asSequence()
            .filter { c -> c.chatId == chatId }
            .map { c -> c.apply { deleted = true }}
            .toList()
    }
}