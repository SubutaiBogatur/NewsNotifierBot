package models

import org.telegram.telegrambots.api.methods.send.SendMessage
import NewsNotifierBot


class SubscribersDispatcher {
    val chatIds = mutableSetOf<String>()
    val usernames = mutableSetOf<String>()

    val targetSubstrings = mutableMapOf<String, MutableList<String>>()

    val ONBOARDING_SIZE = 3
    val onboardingUsers = mutableSetOf<String>()

    val newsSources = listOf(
            "http://www.sfgate.com/rss/feed/Business-and-Technology-News-448.php",
            "http://www.sfgate.com/bayarea/feed/Bay-Area-News-429.php",
            "http://www.sfgate.com/rss/feed/Top-News-Stories-595.php",
            "http://www.latimes.com/local/rss2.0.xml"
    )

    @Synchronized
    fun addSubscriber(chatId: String, name: String): Boolean {
        chatIds.add(chatId)
        onboardingUsers.add(chatId)
        return usernames.add(name)
    }

    @Synchronized
    fun removeSubscriber(chatId: String, name: String): Boolean {
        chatIds.remove(chatId)
        onboardingUsers.remove(chatId)
        return usernames.remove(name)
    }

    @Synchronized
    fun sendAll(bot: NewsNotifierBot, text: String) {
        chatIds.forEach({ bot.sendMessage(SendMessage(it, text)) })
    }

    @Synchronized
    fun sendAll(bot: NewsNotifierBot, newNews: List<List<News>>, newsAll: List<List<News>>) {
        for (chatId in chatIds) {
            if (chatId in onboardingUsers) {
                //todo: onboarding
                bot.sendMessage(SendMessage(chatId, "Onboarding will be implemented later, here is one msg for you:"))
                bot.sendMessage(SendMessage(chatId, newsAll[0][0].getMessage()))
            } else {
                //todo: check for substrings
                for (newsFromOneAddress in newNews) {
                    for (news in newsFromOneAddress) {
                        bot.sendMessage(SendMessage(chatId, news.getMessage()))
                    }
                }
            }
        }
        onboardingUsers.clear()
    }

    fun getSubscribersString(): String {
        return usernames.toString()
    }

    @Synchronized
    fun addSubstring(chatId: String, str: String) {
        targetSubstrings.compute(chatId, { _, ls -> ls?.apply { add(str) } ?: mutableListOf(str) })
    }

    @Synchronized
    fun removeSubstring(chatId: String, str: String) {
        targetSubstrings.compute(chatId, { _, ls -> ls?.apply { remove(str) } ?: mutableListOf() })
    }
}

