package com.in.rohit.spring_ai_agent.repository;

import com.in.rohit.spring_ai_agent.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(
            String conversationId
    );

    @Modifying
    void deleteByConversationId(String conversationId);
}