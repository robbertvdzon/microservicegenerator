package com.vdzon.aicode

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SlackService(
    @Value("\${slack.bot.token}") private val slackToken: String,
    @Value("\${slack.channel.id}") private val channelId: String
) {
    private val slack = Slack.getInstance()
//    private var lastProcessedTimestamp: String? = null

    /** Haalt nieuwe opdrachten op sinds het laatste verwerkte bericht **/
    fun getNewCommands(): String? {
        val response = slack.methods(slackToken).conversationsHistory(
            ConversationsHistoryRequest.builder()
                .channel(channelId)
                .limit(1) // check alleen de laatste
                .build()
        )

        val newMessages = response.messages
//            ?.filter { it.ts > (lastProcessedTimestamp ?: "0") } // Negeer oude berichten
            ?.map { it.text }
            ?: emptyList()

//        if (newMessages.isNotEmpty()) {
//            lastProcessedTimestamp = response.messages?.firstOrNull()?.ts // Update laatste timestamp
//        }

        return newMessages.lastOrNull()
    }

    /** Stuurt een bericht terug naar het Slack-kanaal **/
    fun sendMessage(text: String) {
        slack.methods(slackToken).chatPostMessage(
            ChatPostMessageRequest.builder()
                .channel(channelId)
                .text(text)
                .build()
        )
    }
}
