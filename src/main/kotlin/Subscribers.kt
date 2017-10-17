import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.User;


class Subscribers {
    val chatIds = mutableSetOf<String>()
    val usernames = mutableSetOf<String>()

    @Synchronized
    fun addSubscriber(chatId: String, name: String): Boolean {
        chatIds.add(chatId)
        return usernames.add(name)
    }

    @Synchronized
    fun removeSubscriber(chatId: String, name: String): Boolean {
        chatIds.remove(chatId)
        return usernames.remove(name)
    }

    @Synchronized
    fun sendAll(bot: NewsNotifierBot, text: String) {
        chatIds.forEach({ bot.sendMessage(SendMessage(it, text)) })
    }

//    @Synchronized
//    fun sendAll(bot: NewsNotifierBot, news: News) {
//        TODO()
//    }

    @Synchronized
    fun sendAll(bot: NewsNotifierBot, news: List<News>) {
        TODO()
    }

    fun getSubscribersString(): String {
        return usernames.toString()
    }
}

