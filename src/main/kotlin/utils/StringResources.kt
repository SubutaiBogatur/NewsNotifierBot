package utils

val newsSources = listOf(
        "http://www.sfgate.com/rss/feed/Business-and-Technology-News-448.php",
        "http://www.sfgate.com/bayarea/feed/Bay-Area-News-429.php",
        "http://www.sfgate.com/rss/feed/Top-News-Stories-595.php",
        "http://www.latimes.com/local/rss2.0.xml"
//        "http://lorem-rss.herokuapp.com/feed?unit=second&interval=30"
)

val helpMessage = """
        The bot will notify you about new news, which are posted in a list of sources.
        Current list of sources:
        $newsSources.

        You can choose news you're interested in by providing a bot list of substrings. Only news containing any of the substrings will be sent to you.
        If no substrings are provided, all news are sent to you.

        Commands:
        /s = subscribe
        /u = unsubscribe
        /ls = list current substrings
        /as = add substring
        /rs = remove substring
        /rmrf = remove all substrings
        /help = show this message

        For commands manipulating substrings, substrings should be provided in quotes. Moreover, when looking for occurrences of substrings in news, case is ignored.
        eg:
        /as "America will be great again"
        /as "YNDX"
        then
        /ls will print
        [america will be great again, yndx]

        Bot was created by Aleksandr Tukallo and written in Kotlin
    """.trimIndent()

val onboardingMessage = """
        Welcome on board!

        You are subscribed to list of sources:
        $newsSources

        Starting from this message and till you unsubscribe you will get messages as soon as new news matching your interests appear
    """.trimIndent()

val admins = listOf(
        "alex_tukallo"
)