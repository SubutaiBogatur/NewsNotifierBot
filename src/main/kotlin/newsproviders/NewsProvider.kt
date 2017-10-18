package newsproviders

import models.News
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import utils.Logger
import java.io.IOException
import java.net.URISyntaxException
import javax.xml.parsers.DocumentBuilderFactory

class NewsProvider {
    private val loggerId = this.javaClass.name

    fun getCurrentNews(addresses: List<String>): List<List<News>> {
        fun Element.getText(childElementName: String)
                = this.getElementsByTagName(childElementName).item(0).textContent

        val ret = mutableListOf<List<News>>()

        for (address in addresses) {
            val curAddr = mutableListOf<News>()
            try {
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(address)
                val items = xmlDoc.documentElement.getElementsByTagName("item")

                (0 until items.length)
                        .map { items.item(it) }
                        .filter { it.nodeType == Node.ELEMENT_NODE }
                        .map { it as Element }
                        .mapTo(curAddr) {
                            News(
                                    it.getText("title"),
                                    it.getText("link"),
                                    it.getText("description"),
                                    it.getText("pubDate"),
                                    address)
                        }
            } catch (e: URISyntaxException) {
                Logger.log(loggerId, "uri syntax exception: " + e)
            } catch (e: IOException) {
                Logger.log(loggerId, "io exception: " + e)
            }
            ret.add(curAddr)
        }

        return ret
    }
}