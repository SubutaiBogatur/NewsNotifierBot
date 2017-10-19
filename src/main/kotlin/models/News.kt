package models

data class News(val title: String,
                val description: String,
                val link: String,
                val pubDate: String,
                val providerUrl: String) {

    fun getMessage(): String {
        val sb = StringBuilder()
        sb.append(title)
        sb.append("\n\n")
        sb.append("Published on $pubDate. RSS Feed: $providerUrl")
        sb.append("\n\n")
        sb.append(link)
        sb.append("\n\n")
        sb.append("Description: $description")
        return sb.toString()
    }

    fun getMessage(encounteredSubstring: String): String {
        val sb = StringBuilder()
        sb.append("Word found: $encounteredSubstring\n")
        sb.append(getMessage())
        return sb.toString()
    }
}