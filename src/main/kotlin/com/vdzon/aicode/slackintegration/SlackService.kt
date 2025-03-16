package com.vdzon.aicode.slackintegration

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SlackService(
    @Value("\${slack.bot.token}") private val slackToken: String,
    @Value("\${slack.channel.id}") private val channelId: String
) {
    private val slack = Slack.getInstance()

    fun getNewCommands(): String? {
        val response = slack.methods(slackToken).conversationsHistory(
            ConversationsHistoryRequest.builder()
                .channel(channelId)
                .limit(1) // check alleen de laatste
                .build()
        )

        val newMessages = response.messages
            ?.map { it.text }
            ?: emptyList()
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
