import org.telegram.telegrambots.api.objects.User
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class NewsNotifierServer(val bot: NewsNotifierBot) {

    val timer = ScheduledThreadPoolExecutor(2)

    init {
        timer.scheduleAtFixedRate({
            try {
                checkNewNews()
            } catch (t: Throwable) {
                println(t)
            }
        }, 0, 5, TimeUnit.SECONDS)
    }

    val currentNews = mutableListOf<News>()

    fun checkNewNews() {
        fun getNews(): MutableList<News> {
            return mutableListOf<News>()
        }

        val newNews = getNews()
        bot.announceNewData(newNews)
    }
}