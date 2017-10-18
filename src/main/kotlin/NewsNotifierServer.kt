import models.News
import newsproviders.NewsProvider
import utils.Logger
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class NewsNotifierServer(val bot: NewsNotifierBot) {
    private val loggerId = "~server"

    val tpe = ScheduledThreadPoolExecutor(2)

    val dataProvider = NewsProvider()

    val CACHE_SIZE = 70 // per each address
    val cache = mutableListOf<MutableSet<News>>()

    init {
        // todo serialization
        (1..bot.subscribers.newsSources.size)
                .forEach({ cache.add(mutableSetOf()) })
    }

    fun checkNewNews() {
        Logger.log(loggerId, "checkNew news called")
        val allNewNews = dataProvider.getCurrentNews(bot.subscribers.newsSources)
        val newNews = mutableListOf<MutableList<News>>()
        for (i in 0 until allNewNews.size) {
            newNews.add(mutableListOf())
            allNewNews[i].forEach({
                if (cache[i].add(it)) {
                    newNews[i].add(it)
                }
            })
            //todo: reduce size of cache
        }
        bot.announceNewData(newNews, allNewNews)
    }

    /**
     * Start repeating calls to provide new news to bot
     */
    fun start(periodInSeconds: Long) {
        Logger.log(loggerId, "server started with period: $periodInSeconds")
        //todo: maybe should schedule repeating notification about server still being active (both for me & other users)
        tpe.scheduleAtFixedRate({
            try {
                checkNewNews()
            } catch (t: Throwable) {
                Logger.log(loggerId, t.toString())
            }
        }, 0, periodInSeconds, TimeUnit.SECONDS)
    }

    fun stop() {
        tpe.shutdown()
    }
}