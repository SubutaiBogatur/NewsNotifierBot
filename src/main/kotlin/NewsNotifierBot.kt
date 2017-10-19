import models.News
import utils.Logger.Companion.log
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Chat
import org.telegram.telegrambots.api.objects.EntityType
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.User
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import utils.Logger

class NewsNotifierBot : TelegramLongPollingBot() {
    val subscribersDispatcher = SubscribersDispatcher()

    override fun getBotToken() = "460715821:AAErvovpexiD6Kb2mMluAtCoJKu3ER14ux8"
    override fun getBotUsername() = "NewsNotifierBot"

    override fun onUpdateReceived(update: Update?) {
        try {
            if (update == null) {
                log("WARNING: received onUpdate call with null")
                return
            }

            if (update.message.from.userName == null) {
                sendMessage(SendMessage(update.message.chatId, "Sorry, but to interact with the bot your username should be not null"))
            }

            log(update, update.message.text)

            if (update.message.isCommand) {
                when (update.message.entities.first { it.type == EntityType.BOTCOMMAND }.text) {
                    "/s" -> subscribe(update.message.from, update.message.chat)
                    "/u" -> unsubscribe(update.message.from, update.message.chat)
                    "/lsu" -> listSubscribers(update)
                    "/help" -> help(update)
                    "/start" -> sendMessage(SendMessage(update.message.chatId.toString(), "Consider entering /help command"))
                    "/ls" -> listSubstrings(update)
                    "/as" -> addSubstring(update)
                    "/rs" -> removeSubstring(update)
                    "/rmrf" -> removeAllSubstrings(update)
                }
            }
        } catch (t: Throwable) {
            Logger.Companion.log("CRITICAL~ERROR", t.toString())
        }
    }

    private fun subscribe(user: User, chat: Chat) {
        val subscribed = subscribersDispatcher.addSubscriber(chat.id.toString(), user.userName)
        log(user.userName, "subscribed ${if (!subscribed) "though was already" else ""}")
        sendMessage(SendMessage(chat.id.toString(), "You are subscribed"))
    }

    private fun unsubscribe(user: User, chat: Chat) {
        val unsubscribed = subscribersDispatcher.removeSubscriber(chat.id.toString())
        log(user.userName, "unsubscribed ${if (!unsubscribed) "though wasn't subscribed" else ""}")
        sendMessage(SendMessage(chat.id.toString(), "You are unsubscribed"))
    }

    private fun listSubscribers(update: Update) {
        sendMessage(SendMessage(update.message.chatId.toString(), subscribersDispatcher.getSubscribersString()))
    }

    @Synchronized
    fun announceNewData(newNews: List<List<News>>) {
        log("For clients ${subscribersDispatcher.getSubscribersString()} ${newNews.stream().map { it.size }.reduce({ a, b -> a + b }).get()} new news are dispatched")
        subscribersDispatcher.sendAll(this, newNews)
    }

    private fun help(update: Update) = sendMessage(SendMessage(update.message.chatId.toString(), utils.helpMessage))
    private fun listSubstrings(update: Update) = subscribersDispatcher.listSubstrings(this, update.message.chatId.toString())

    /**
     * Throws exception if unable to extract substring
     */
    private fun extractSubstring(update: Update): String {
        val msg = update.message.text
        if (msg.count { it == '\"' } != 2) {
            throw IllegalArgumentException("There should be only two quotes in substring -- in it's start and it's end")
        }

        val suffix = msg.subSequence(msg.indexOfFirst { it == '\"' } + 1, msg.length)
        return suffix.subSequence(0, suffix.indexOfFirst { it == '\"' }).toString().toLowerCase()
    }

    private fun addSubstring(update: Update) {
        try {
            val ss = extractSubstring(update)
            subscribersDispatcher.addSubstring(update.message.chatId.toString(), ss)
        } catch (e: IllegalArgumentException) {
            sendMessage(SendMessage(update.message.chatId.toString(), "Error: " + e.message))
        }
    }

    private fun removeSubstring(update: Update) {
        try {
            val ss = extractSubstring(update)
            subscribersDispatcher.removeSubstring(update.message.chatId.toString(), ss)
        } catch (e: IllegalArgumentException) {
            sendMessage(SendMessage(update.message.chatId.toString(), "Error: " + e.message))
        }
    }

    private fun removeAllSubstrings(update: Update) = subscribersDispatcher.removeAllSubstrings(update.message.chatId.toString())
}