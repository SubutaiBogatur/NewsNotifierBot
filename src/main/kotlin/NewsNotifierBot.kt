import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Chat
import org.telegram.telegrambots.api.objects.EntityType
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.User
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NewsNotifierBot : TelegramLongPollingBot() {
    private val logWriter = PrintWriter(OutputStreamWriter(FileOutputStream("botLog-${getStringTime()}.txt")))

    private fun getStringTime() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd--kk-mm"))

    private fun log(update: Update, text: String) = log(update.message.from.userName, text)
    private fun log(text: String) = log("????????????????????", text)

    @Synchronized
    private fun log(username: String, text: String) {
        val msg = "${getStringTime()} -- ${String.format("%-20s", username)} -- $text"
        logWriter.println(msg)
        println(msg)
    }

    val subscribers = Subscribers()

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

    fun announceNewData(news: List<News>) {
        log("Data is announced: " + news)
        if (news.isEmpty()) {
            // todo more seldom pls
            subscribers.sendAll(this, "no new news")
            return
        }
        subscribers.sendAll(this, news)
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