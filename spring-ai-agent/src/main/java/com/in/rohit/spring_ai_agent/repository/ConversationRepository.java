package com.in.rohit.spring_ai_agent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.in.rohit.spring_ai_agent.entity.Conversation;

public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByConversationId(
            String conversationId);

    boolean existsByConversationId(
            String conversationId);

    List<Conversation> findByUserUsername(
            String username);

    List<Conversation> findByUserEmail(
            String email);

    Optional<Conversation> findByConversationIdAndUserEmail(
            String conversationId,
            String email);

    void deleteByConversationId(String conversationId);
}