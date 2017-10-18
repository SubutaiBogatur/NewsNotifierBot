package models

data class News(val title: String,
                val link: String,
                val descripition: String,
                val pubDate: String,
                val providerUrl: String) {

    fun getMessage(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append(title)
        sb.append("\n")
        sb.append("Published on $pubDate. RSS Feed: $providerUrl")
        sb.append("\n")
        sb.append(link)
        sb.append("\n")
        sb.append("Description: $descripition")
        return sb.toString()
    }

    fun getMessage(encounteredSubstring: String): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Word found: $encounteredSubstring\n")
        sb.append(getMessage())
        return sb.toString()
    }
}