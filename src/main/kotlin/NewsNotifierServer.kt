import models.News
import newsproviders.NewsProvider
import utils.Logger
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class NewsNotifierServer(val bot: NewsNotifierBot) {
    private val loggerId = "~server"

    val tpe = ScheduledThreadPoolExecutor(2)

    val dataProvider = NewsProvider()

    val CACHE_SIZE = 50 // per each address
    val cache = mutableListOf<MutableSet<News>>()

    val MAX_NEW_NEWS = 5

    init {
        (1..utils.newsSources.size)
                .forEach({ cache.add(mutableSetOf()) })
    }

    /**
     * New news are loaded from rss feeds. Then they're compared to cached ones and only truly
     * new ones are extracted and then announced to the subscribers
     *
     * @return list of list of new news is returned (for each source).
     * Total size is guaranteed to be not more, than MAX_NEW_NEWS
     */
    fun checkNewNews() {
        Logger.log(loggerId, "checkNew news called")

        val allNewNews = dataProvider.getCurrentNews(utils.newsSources)

        if (cache.stream().allMatch({ it.isEmpty() })) {
            // if cache is empty, let's initialize it
            cache.forEachIndexed({ i, set -> set.addAll(allNewNews[i]) })
            Logger.log(loggerId, "Cache initialized and not announced")
            return
        }

        var newNews = mutableListOf<MutableList<News>>()
        for (i in 0 until allNewNews.size) {
            newNews.add(mutableListOf())
            allNewNews[i].forEach({
                if (cache[i].add(it)) {
                    newNews[i].add(it)
                }
            })
            //todo: reduce size of cache
        }

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
            outerloop@ for (l in newNews) {
                val newList = mutableListOf<News>()
                lessNews.add(newList)
                for (news in l) {
                    newList.add(news)
                    curSize++
                    if (curSize >= MAX_NEW_NEWS) {
                        break@outerloop
                    }
                }
            }
            newNews = lessNews
        }
        return newNews
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