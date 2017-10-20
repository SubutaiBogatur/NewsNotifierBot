import models.News
import utils.Logger.Companion.log
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.EntityType
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import utils.Logger
import utils.Logger.Companion.logException
import utils.detailed_logging
import java.util.*

class NewsNotifierBot : TelegramLongPollingBot() {
    val subscribersDispatcher = SubscribersDispatcher()

    override fun getBotToken() = "460715821:AAErvovpexiD6Kb2mMluAtCoJKu3ER14ux8"
    override fun getBotUsername() = "NewsNotifierBot"

    override fun onUpdateReceived(update: Update?) {
        try {
            if (update == null) {
                log("~WARNING: received onUpdate call with null")
                return
            }

            if (update.message.from.userName == null) {
                sendMessageWithoutLogging(update.message.chatId.toString(),
                        "Sorry, but to interact with the bot your username should be not null")
                log("~WARNING: guy with chatId: ${update.message.chatId} has no username")
            }

            log(update, update.message.text)

            if (update.message.isCommand) {
                when (update.message.entities.first { it.type == EntityType.BOTCOMMAND }.text) {
                    "/s" -> subscribe(update)
                    "/u" -> unsubscribe(update)
                    "/lsu" -> listSubscribers(update)
                    "/help" -> help(update)
                    "/start" -> sendMessage(update, "Consider entering /help command")
                    "/ls" -> listSubstrings(update)
                    "/as" -> addSubstring(update)
                    "/rs" -> removeSubstring(update)
                    "/rmrf" -> removeAllSubstrings(update)
                }
            }
        } catch (t: Throwable) {
            logException(t)
        }
    }

    private fun subscribe(update: Update) {
        val subscribed = subscribersDispatcher.addSubscriber(update.message.chatId.toString(), update.message.from.userName)
        log(update.message.from.userName, "subscribed ${if (!subscribed) "though was already" else ""}")
        sendMessage(update, "You are subscribed")
    }

    private fun unsubscribe(update: Update) {
        val unsubscribed = subscribersDispatcher.removeSubscriber(update.message.chat.id.toString())
        log(update.message.from.userName, "unsubscribed ${if (!unsubscribed) "though wasn't subscribed" else ""}")
        sendMessage(update, "You are unsubscribed")
    }

    private fun listSubscribers(update: Update) {
        sendMessage(update, subscribersDispatcher.getSubscribersString())
    }

    @Synchronized
    fun announceNewData(newNews: List<List<News>>) {
        log("For clients ${subscribersDispatcher.getSubscribersString()} ${newNews.stream().map { it.size }.reduce({ a, b -> a + b }).get()} new news are dispatched")
        subscribersDispatcher.sendAll(this, newNews)
    }

    private fun help(update: Update) = sendMessage(update, utils.helpMessage)
    private fun listSubstrings(update: Update) = subscribersDispatcher.listSubstrings(this, update)

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
            sendMessage(update, "Error: " + e.message)
        }
    }

    private fun removeSubstring(update: Update) {
        try {
            val ss = extractSubstring(update)
            subscribersDispatcher.removeSubstring(update.message.chatId.toString(), ss)
        } catch (e: IllegalArgumentException) {
            sendMessage(update, "Error: " + e.message)
        }
    }

    private fun removeAllSubstrings(update: Update) = subscribersDispatcher.removeAllSubstrings(update.message.chatId.toString())

    fun sendMessage(update: Update, text: String) {
        sendMessage(update.message.chatId.toString(), update.message.from.userName, text)
    }

    // function sends the message and does logging
    fun sendMessage(chatId: String, username: String, text: String) {
        if (username in detailed_logging) {
            log(username, "Message sent: $text")
        }
        sendMessageWithoutLogging(chatId, text)
    }

    // consider not using this function directly
    private fun sendMessageWithoutLogging(chatId: String, text: String) {
        sendMessage(SendMessage(chatId, text))
    }
}