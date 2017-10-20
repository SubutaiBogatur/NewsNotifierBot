import models.News
import newsproviders.NewsProvider
import utils.Logger
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class NewsNotifierServer(private val bot: NewsNotifierBot) {

    // constants:
    companion object {
        private val loggerId = "server"
        private val MAX_NEW_NEWS = 5
        private val ADMIN_PINGING_PERIOD: Long = 60 // minutes
        private val THREADS = 2
    }

    private val tpe = ScheduledThreadPoolExecutor(THREADS)

    private val dataProvider = NewsProvider()
    private var cache = mutableListOf<MutableSet<News>>()

    init {
        (1..utils.newsSources.size).forEach({ cache.add(mutableSetOf()) })
    }

    /**
     * New news are loaded from rss feeds. Then they're compared to cached ones and only truly
     * new ones are extracted and then announced to the subscribers
     *
     * @return list of list of new news is returned (for each source).
     * Total size is guaranteed to be not more, than MAX_NEW_NEWS
     */
    fun checkNewNews() {
        Logger.log(loggerId, "checkNewNews called")

        val allNewNews = dataProvider.getCurrentNews(utils.newsSources)

        // if cache is empty, let's initialize it
        if (cache.stream().allMatch({ it.isEmpty() })) {
            cache.forEachIndexed({ i, set -> set.addAll(allNewNews[i]) })
            Logger.log(loggerId, "Cache initialized and not announced")
            return
        }

        val newCache = mutableListOf<MutableSet<News>>()
        var newNews = mutableListOf<MutableList<News>>()

        for (i in 0 until allNewNews.size) {
            newNews.add(mutableListOf())
            newCache.add(mutableSetOf())
            allNewNews[i].forEach({
                newCache[i].add(it)
                if (!cache[i].contains(it)) {
                    newNews[i].add(it)
                }
            })
        }
        cache = newCache // update cache, so it's not too big

        //don't send too much data
        newNews = reduceNewNewsSize(newNews)
        bot.announceNewData(newNews)
    }

    /**
     * Reduces size if needed
     */
    private fun reduceNewNewsSize(newNewsToCheck: MutableList<MutableList<News>>): MutableList<MutableList<News>> {
        var newNews = newNewsToCheck
        if (newNews.stream().map { it.size }.reduce({ a, b -> a + b }).get() > MAX_NEW_NEWS) {
            val lessNews = mutableListOf<MutableList<News>>()
            var curSize = 0
            outer@ for (l in newNews) {
                val newList = mutableListOf<News>()
                lessNews.add(newList)
                for (news in l) {
                    newList.add(news)
                    curSize++
                    if (curSize >= MAX_NEW_NEWS) {
                        break@outer
                    }
                }
            }
            newNews = lessNews
        }
        return newNews
    }

    fun informAboutBeingActive() {
        utils.admins.forEach {
            bot.subscribersDispatcher.directMessage(bot, it,
                    "Bot is still being active, don't worry. Users are: ${bot.subscribersDispatcher.getSubscribersString()}")
        }
    }

    /**
     * Start repeating calls to provide new news to bot
     */
    fun start(periodInSeconds: Long) {
        Logger.log(loggerId, "server started with period: $periodInSeconds")
        tpe.scheduleAtFixedRate({
            try {
                checkNewNews()
            } catch (t: Throwable) {
                Logger.log(Logger.SEVERE_TAG, t.toString() + " stacktrace: " + Arrays.toString(t.stackTrace))
            }
        }, 0, periodInSeconds, TimeUnit.SECONDS)

        tpe.scheduleAtFixedRate({
            try {
                informAboutBeingActive()
            } catch (t: Throwable) {
                Logger.log(Logger.SEVERE_TAG, t.toString() + " stacktrace: " + Arrays.toString(t.stackTrace))
            }
        }, 0, ADMIN_PINGING_PERIOD, TimeUnit.MINUTES)
    }

    fun stop() {
        tpe.shutdown()
    }
}