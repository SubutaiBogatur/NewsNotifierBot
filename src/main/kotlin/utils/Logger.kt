package utils

import org.telegram.telegrambots.api.objects.Update
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Logger {
    companion object {
        private val logWriter = PrintWriter(OutputStreamWriter(FileOutputStream("botLog-${getStringTime()}.txt")))

        fun getStringTime() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd--kk-mm"))

        fun log(update: Update, text: String) = log(update.message.from.userName, text)
        fun log(text: String) = log("????????????????????", text)

        @Synchronized
        fun log(username: String, text: String) {
            val msg = "${getStringTime()} -- ${String.format("%-30s", username)} -- $text"
            logWriter.println(msg)
            logWriter.flush()
            println(msg)
        }
    }
}

