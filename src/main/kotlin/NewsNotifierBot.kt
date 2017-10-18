import models.News
import models.SubscribersDispatcher
import utils.Logger.Companion.log
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Chat
import org.telegram.telegrambots.api.objects.EntityType
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.User
import org.telegram.telegrambots.bots.TelegramLongPollingBot

class NewsNotifierBot : TelegramLongPollingBot() {
    val subscribers = SubscribersDispatcher()

    override fun getBotToken(): String {
        return "460715821:AAErvovpexiD6Kb2mMluAtCoJKu3ER14ux8"
    }

    override fun getBotUsername(): String {
        return "NewsNotifierBot"
    }

    private fun subscribe(user: User, chat: Chat) {
        val subscribed = subscribers.addSubscriber(chat.id.toString(), user.userName)
        log(user.userName, "subscribed ${if (!subscribed) "though was already" else ""}")
        sendMessage(SendMessage(chat.id.toString(), "You are subscribed"))
    }

    private fun unsubscribe(user: User, chat: Chat) {
        val unsubscribed = subscribers.removeSubscriber(chat.id.toString(), user.userName)
        log(user.userName, "unsubscribed ${if (!unsubscribed) "though wasn't subscribed" else ""}")
        sendMessage(SendMessage(chat.id.toString(), "You are unsubscribed"))
    }

    private fun listSubscribers(update: Update) {
        sendMessage(SendMessage(update.message.chatId.toString(), subscribers.getSubscribersString()))
    }

    fun announceNewData(newNews: List<List<News>>, allNews: List<List<News>>) {
        log("For clients ${subscribers.getSubscribersString()} ${newNews.stream().map { it.size }.reduce({ a, b -> a + b }).get()} new news are dispatched")
        subscribers.sendAll(this, newNews, allNews)
    }

    override fun onUpdateReceived(update: Update?) {
        if (update == null) {
            log("WARNING: received onUpdate call with null")
            return
        }
        log(update, update.message.text)

        if (update.message.isCommand) {
            when (update.message.entities.first { it.type == EntityType.BOTCOMMAND }.text) {
                "/s" -> subscribe(update.message.from, update.message.chat)
                "/u" -> unsubscribe(update.message.from, update.message.chat)
                "/ls" -> listSubscribers(update)
            }
        }
    }
}