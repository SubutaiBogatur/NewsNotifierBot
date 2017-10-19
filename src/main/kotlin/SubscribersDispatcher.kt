import org.telegram.telegrambots.api.methods.send.SendMessage
import models.News
import models.Subscriber
import org.telegram.telegrambots.api.objects.Update
import utils.Logger
import java.io.*


class SubscribersDispatcher {

    val subscribers: MutableMap<String, Subscriber> // chatId -> subscriber
    val onboardingSubscribers = mutableMapOf<String, Subscriber>() // chatId -> subscriber


    /**
     * Add new subscriber and return true if it was added
     */
    @Synchronized
    fun addSubscriber(chatId: String, name: String): Boolean {
        if (subscribers.containsKey(chatId)) {
            return false
        }

        val sub = Subscriber(chatId, name)
        subscribers.put(chatId, sub)
        onboardingSubscribers.put(chatId, sub)
        storeSubscribers()
        return true
    }

    /**
     * Return true if subscriber was removed
     */
    @Synchronized
    fun removeSubscriber(chatId: String): Boolean {
        if (!subscribers.containsKey(chatId)) {
            return false
        }
        onboardingSubscribers.remove(chatId)
        subscribers.remove(chatId)
        storeSubscribers()
        return true
    }

    @Synchronized
    fun sendAll(bot: NewsNotifierBot, text: String) {
        subscribers.forEach({ (_, sub) -> bot.sendMessage(SendMessage(sub.chatId, text)) })
    }

    @Synchronized
    fun sendAll(bot: NewsNotifierBot, newNews: List<List<News>>) {
        for (entries in subscribers) {
            val subscriber = entries.value

            if (subscriber.chatId in onboardingSubscribers) {
                bot.sendMessage(SendMessage(subscriber.chatId, utils.onboardingMessage))
            }

            for (newsFromOneAddress in newNews) {
                if (subscriber.substrings.isEmpty()) {
                    newsFromOneAddress.forEach {
                        bot.sendMessage(SendMessage(subscriber.chatId, it.getMessage("you are subscribed to all the substrings")))
                    }
                } else {
                    val metSubstrings = mutableMapOf<News, String>() // title -> first encountered substring
                    newsFromOneAddress.forEach { news ->
                        subscriber.substrings.forEach({
                            if (news.title.contains(it, ignoreCase = true) || news.description.contains(it, ignoreCase = true)) {
                                metSubstrings.put(news, it)
                            }
                        })
                    }
                    // now the map contains all the filtered news and corresponding substring for them
                    for ((news, substring) in metSubstrings) {
                        bot.sendMessage(SendMessage(subscriber.chatId, news.getMessage(substring)))
                    }
                }
            }
        }
        onboardingSubscribers.clear()
    }

    @Synchronized
    fun getSubscribersString(): String {
        val sb = StringBuilder()
        sb.append("[")
        for ((_, sub) in subscribers) {
            sb.append("${sub.username}, ")
        }
        sb.append(("]"))
        return sb.toString()
    }

    @Synchronized
    fun listSubstrings(bot: NewsNotifierBot, chatId: String) {
        val substrings = subscribers.get(chatId)?.substrings?.toString() ?: return
        bot.sendMessage(SendMessage(chatId, "Your substrings:\n$substrings"))
    }

    @Synchronized
    fun addSubstring(chatId: String, str: String) {
        subscribers.get(chatId)?.substrings?.add(str)
        storeSubscribers()
    }

    @Synchronized
    fun removeSubstring(chatId: String, str: String) {
        subscribers.get(chatId)?.substrings?.remove(str)
        storeSubscribers()
    }

    @Synchronized
    fun removeAllSubstrings(chatId: String) {
        subscribers.get(chatId)?.substrings?.clear()
        storeSubscribers()
    }

    init {
        //let's load subscribers from memory
        var subs: MutableMap<String, Subscriber> = mutableMapOf()
        try {
            ObjectInputStream(FileInputStream("stored-subscribers"))
                    .use { subs = it.readObject() as MutableMap<String, Subscriber> }
        } catch (e: Exception) {
            Logger.log(e.toString())
        }
        subscribers = subs
    }

    fun storeSubscribers() {
        try {
            ObjectOutputStream(FileOutputStream("stored-subscribers")).use { it.writeObject(subscribers) }
        } catch (e: IOException) {
            Logger.log(e.toString())
        }
    }
}

