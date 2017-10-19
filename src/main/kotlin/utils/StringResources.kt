package utils

val newsSources = listOf(
        "http://www.sfgate.com/rss/feed/Business-and-Technology-News-448.php",
        "http://www.sfgate.com/bayarea/feed/Bay-Area-News-429.php",
        "http://www.sfgate.com/rss/feed/Top-News-Stories-595.php",
        "http://www.latimes.com/local/rss2.0.xml",
        "http://lorem-rss.herokuapp.com/feed?unit=second&interval=30"
)

val helpMessage = """
        The bot will notify you about new news, which are posted in a list of sources.
        Current list of sources:
        $newsSources.

        You can choose news you're interested in by providing a bot list of substrings. Only news containing any of the substrings will be sent to you.

        Commands:
        /s = subscribe
        /u = unsubscribe
        /ssls = list current substrings
        /ssa = add substring
        /ssr = remove substring
        /help = show this message

        Bot was created by Aleksandr Tukallo and written in Kotlin
    """.trimIndent()

val onboardingMessage = """
        Welcome on board!

        You are subscribed to list of sources:
        $newsSources

        Starting from this message and till you unsubscribe you will get messages as soon as new news matching your interests appear
    """.trimIndent()