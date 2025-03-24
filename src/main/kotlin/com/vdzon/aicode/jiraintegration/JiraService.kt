package com.vdzon.aicode.jiraintegration

import com.vdzon.aicode.commonmodel.Story
import net.rcarz.jiraclient.BasicCredentials
import net.rcarz.jiraclient.JiraClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class JiraService(
    @Value("\${atlassion.api_token}") private val apiToken: String,
    @Value("\${atlassion.email}") private val email: String,
) {
    fun getJiraIssue(jiraTicket: String): Story {
        val creds = BasicCredentials(email, apiToken)
        val jira = JiraClient("https://edsn.atlassian.net", creds)
        val issue = jira.getIssue(jiraTicket)
        return Story(issue.summary, issue.description)
    }
}