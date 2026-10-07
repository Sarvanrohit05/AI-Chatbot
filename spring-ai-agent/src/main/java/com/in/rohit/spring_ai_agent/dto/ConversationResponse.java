package com.in.rohit.spring_ai_agent.dto;

import java.time.LocalDateTime;

/*
 * WHY:
 * Conversation Entity ko directly API response me bhejne se
 * password aur Hibernate ka internal data aa raha tha.
 * Isliye safe API response ke liye DTO banaya.
 */
public class ConversationResponse {

    private String conversationId;

    private String title;

    private String username;

    private String email;

    private LocalDateTime createdAt;

    public ConversationResponse() {
    }

    public ConversationResponse(
            String conversationId,
            String title,
            String username,
            String email,
            LocalDateTime createdAt) {

        this.conversationId = conversationId;
        this.title = title;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}