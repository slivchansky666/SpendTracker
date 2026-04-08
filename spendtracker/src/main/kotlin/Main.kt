package org.example

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import java.sql.DriverManager.println
import java.net.HttpURLConnection
import java.net.URL
fun main() {
    val bot = bot {
        token = "YOUR_BOT_TOKEN"
        dispatch {

            command("start") {
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Привет! Я твой личный помощник.")
            }

            command("help") {
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Я умею записывать расходы и показывать статистику.")
            }

            command("add") {
                val args = args
                if (args.size >= 2) {
                    val amount = args[0]
                    val category = args[1]
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Записал: $amount лей за $category")
                    sendToTable(amount, category)
                } else {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Использование: /add [сумма] [категория]")
                }
            }
            command("list") {
                Thread {
                    try {
                        val content = url.readText() 
                        val prettyReport = formatReport(content)

                        bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = prettyReport,
                            parseMode = ParseMode.HTML
                        )
                    } catch (e: Exception) {
                        bot.sendMessage(ChatId.fromId(message.chat.id), "Ошибка: ${e.message}")
                    }
                }.start()
            }
        }
    }
    bot.startPolling()
}
val url = URL("YOUR_URL_FOR_DB")
fun sendToTable(amount: String, category: String) {

    val json = """{"amount": "$amount", "category": "$category"}"""

    with(url.openConnection() as HttpURLConnection) {
        requestMethod = "POST"
        doOutput = true
        setRequestProperty("Content-Type", "application/json")

        outputStream.use { os ->
            os.write(json.toByteArray())
        }

        val responseCode = responseCode
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 302) {
            println("Успешно! Код: $responseCode")
        } else {
            println("Ошибка сервера: $responseCode")
        }
    }
}

fun formatReport(rawJson: String): String {
    // 1. Убираем лишние символы
    val clean = rawJson
        .replace("[[", "")
        .replace("]]", "")
        .replace("\"", "")
        .split("],[") 

    val builder = StringBuilder()
    builder.append("<b>📊 Твои расходы:</b>\n\n")
    builder.append("<pre>")
    builder.append(String.format("%-11s | %-6s | %s\n", "Дата", "Сумма", "Что"))
    builder.append("------------|--------|---------\n")

    for (row in clean) {
        val columns = row.split(",")
        if (columns.size >= 3) {
            val time = columns[0].substringAfter("T").substringBefore(".")
            val amount = columns[1]
            val item = columns[2]

            builder.append(String.format("%-11s | %-6s | %s\n", time, amount, item))
        }
    }
    builder.append("</pre>")
    return builder.toString()
}
