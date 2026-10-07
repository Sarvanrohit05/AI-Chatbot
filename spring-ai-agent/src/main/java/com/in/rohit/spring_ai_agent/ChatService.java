package com.in.rohit.spring_ai_agent;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ResponseStatusException;

import com.in.rohit.spring_ai_agent.dto.ConversationResponse;
import com.in.rohit.spring_ai_agent.entity.Conversation;
import com.in.rohit.spring_ai_agent.entity.Message;
import com.in.rohit.spring_ai_agent.repository.ConversationRepository;
import com.in.rohit.spring_ai_agent.repository.MessageRepository;
import com.in.rohit.spring_ai_agent.repository.UserRepository;

/*
 * WHY:
 * ChatService AI calls, tools, chat history, image analysis
 * aur message/conversation database operations ko handle karta hai.
 */
@Service
public class ChatService {

    private final ChatClient ollamaChatClient;
    private final ChatClient geminiChatClient;
    private final ChatClient groqChatClient;
    private final ChatClient nvidiaChatClient;

    private final MyTools tools;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatService(
            @Qualifier("ollamaChatClient")
            ChatClient ollamaChatClient,

            @Qualifier("geminiChatClient")
            ChatClient geminiChatClient,

            @Qualifier("groqChatClient")
            ChatClient groqChatClient,

            @Qualifier("nvidiaChatClient")
            ChatClient nvidiaChatClient,

            MyTools tools,

            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository) {

        this.ollamaChatClient = ollamaChatClient;
        this.geminiChatClient = geminiChatClient;
        this.groqChatClient = groqChatClient;
        this.nvidiaChatClient = nvidiaChatClient;

        this.tools = tools;

        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    // =====================================================
    // Save USER message
    // =====================================================

    public void saveUserMessage(
            String conversationId,
            String email,
            String message) {

        Optional<Conversation> existingConversation =
                conversationRepository.findByConversationId(
                        conversationId
                );

        if (existingConversation.isEmpty()) {

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found: " + email
                            ));

            // First user message becomes conversation title
            Conversation conversation =
                    new Conversation(
                            conversationId,
                            user,
                            message
                    );

            conversationRepository.save(conversation);

        } else {

            Conversation conversation =
                    existingConversation.get();

            if (!conversation.getUser()
                    .getEmail()
                    .equalsIgnoreCase(email)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Conversation not found or access denied"
                );
            }
        }

        Message userMessage =
                new Message(
                        conversationId,
                        message,
                        "USER"
                );

        messageRepository.save(userMessage);
    }

    // =====================================================
    // Delete Chat
    // =====================================================

    @Transactional
    public void deleteChat(
            String conversationId,
            String email) {

        Conversation conversation =
                conversationRepository
                        .findByConversationIdAndUserEmail(
                                conversationId,
                                email
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Conversation not found or access denied"
                                )
                        );

        messageRepository.deleteByConversationId(
                conversationId
        );

        conversationRepository.delete(conversation);
    }

    // =====================================================
    // Save AI message
    // =====================================================

    public void saveAiMessage(
            String conversationId,
            String response) {

        Message aiMessage =
                new Message(
                        conversationId,
                        response,
                        "AI"
                );

        messageRepository.save(aiMessage);
    }

    // =====================================================
    // Get AI Chat History
    // =====================================================

    public String getChatHistory(
            String conversationId) {

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(
                        conversationId
                )
                .stream()
                .map(message ->
                        message.getRole()
                                + ": "
                                + message.getContent()
                )
                .collect(
                        java.util.stream.Collectors.joining("\n")
                );
    }

    // =====================================================
    // Get Messages for History API
    // =====================================================

    public List<Message> getMessages(
            String conversationId,
            String email) {

        conversationRepository
                .findByConversationIdAndUserEmail(
                        conversationId,
                        email
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Conversation not found or access denied"
                        ));

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(
                        conversationId
                );
    }

    // =====================================================
    // Get User Conversations
    // =====================================================

    public List<ConversationResponse>
            getUserConversationsByEmail(
                    String email) {

        return conversationRepository
                .findByUserEmail(email)
                .stream()
                .map(conversation ->
                        new ConversationResponse(
                                conversation.getConversationId(),
                                conversation.getTitle(),
                                conversation.getUser().getUsername(),
                                conversation.getUser().getEmail(),
                                conversation.getCreatedAt()
                        )
                )
                .toList();
    }

    // =====================================================
    // Get Chat History after ownership validation
    // =====================================================

    public String getChatHistoryForUser(
            String conversationId,
            String email) {

        if (conversationRepository
                .existsByConversationId(conversationId)) {

            conversationRepository
                    .findByConversationIdAndUserEmail(
                            conversationId,
                            email
                    )
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.FORBIDDEN,
                                    "Conversation not found or access denied"
                            ));
        }

        return getChatHistory(conversationId);
    }

    // =====================================================
    // Ollama + Tools
    // =====================================================

    public String chatWithOllama(
            String message) {

        return ollamaChatClient
                .prompt()
                .user(message)
                .tools(tools)
                .call()
                .content();
    }

    // =====================================================
    // Gemini + Tools
    // =====================================================

    public String chatWithGemini(
            String message) {

        return geminiChatClient
                .prompt()
                .user(message)
                .tools(tools)
                .call()
                .content();
    }

    // =====================================================
    // Gemini Vision - Image Analysis
    // =====================================================

    public String chatWithGeminiVision(
            byte[] imageBytes,
            String contentType,
            String message) {

        try {

            MimeType mimeType =
                    MimeTypeUtils.parseMimeType(
                            contentType
                    );

            ByteArrayResource imageResource =
                    new ByteArrayResource(imageBytes);

            return geminiChatClient
                    .prompt()
                    .user(user -> user
                            .text(message)
                            .media(
                                    mimeType,
                                    imageResource
                            )
                    )
                    .call()
                    .content();

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Image analysis failed: "
                            + e.getClass().getName()
                            + " - "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =====================================================
    // Groq + Tools
    // =====================================================

    public String chatWithGroq(
            String message) {

        return groqChatClient
                .prompt()
                .user(message)
                .tools(tools)
                .call()
                .content();
    }

    // =====================================================
    // NVIDIA + Tools
    // =====================================================

    public String chatWithNvidia(
            String message) {

        return nvidiaChatClient
                .prompt()
                .user(message)
                .tools(tools)
                .call()
                .content();
    }
}