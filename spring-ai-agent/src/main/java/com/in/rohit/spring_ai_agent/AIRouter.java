package com.in.rohit.spring_ai_agent;

import org.springframework.stereotype.Component;

/*
 * WHY:
 * User message ko AI providers tak route karta hai.
 * Groq ko primary provider rakhta hai aur failure par
 * NVIDIA, Gemini aur Ollama ko fallback ke roop mein use karta hai.
 */
@Component
public class AIRouter {

    private final ChatService chatService;

    public AIRouter(ChatService chatService) {
        this.chatService = chatService;
    }

    public String route(
            String email,
            String conversationId,
            String message) {

        long start = System.currentTimeMillis();

        // 1. Get previous chat history
        long historyStart = System.currentTimeMillis();

        String history =
                chatService.getChatHistoryForUser(
                        conversationId,
                        email
                );

        System.out.println(
                "History time = "
                        + (System.currentTimeMillis() - historyStart)
                        + " ms"
        );

        // 2. Save USER message
        long saveUserStart = System.currentTimeMillis();

        chatService.saveUserMessage(
                conversationId,
                email,
                message
        );

        System.out.println(
                "Save user message time = "
                        + (System.currentTimeMillis() - saveUserStart)
                        + " ms"
        );

        // 3. Build prompt
        String prompt;

        if (history == null || history.isBlank()) {

            prompt = message;

        } else {

            prompt = history
                    + "\nUSER: "
                    + message;
        }

        // =====================================================
        // 4. Primary - GROQ
        // =====================================================

        System.out.println(
                "Primary AI Route = GROQ"
        );

        try {

            long providerStart =
                    System.currentTimeMillis();

            String response =
                    chatService.chatWithGroq(prompt);

            System.out.println(
                    "GROQ time = "
                            + (System.currentTimeMillis()
                            - providerStart)
                            + " ms"
            );

            chatService.saveAiMessage(
                    conversationId,
                    response
            );

            System.out.println(
                    "Total request time = "
                            + (System.currentTimeMillis()
                            - start)
                            + " ms"
            );

            return response;

        } catch (Exception e) {

            System.out.println(
                    "Groq failed: "
                            + e.getMessage()
            );
        }

        // =====================================================
        // 5. Fallback - NVIDIA
        // =====================================================

        try {

            long providerStart =
                    System.currentTimeMillis();

            String response =
                    chatService.chatWithNvidia(prompt);

            System.out.println(
                    "NVIDIA fallback time = "
                            + (System.currentTimeMillis()
                            - providerStart)
                            + " ms"
            );

            chatService.saveAiMessage(
                    conversationId,
                    response
            );

            System.out.println(
                    "Fallback provider = NVIDIA"
            );

            return response;

        } catch (Exception e) {

            System.out.println(
                    "NVIDIA failed: "
                            + e.getMessage()
            );
        }

        // =====================================================
        // 6. Fallback - Gemini
        // =====================================================

        try {

            long providerStart =
                    System.currentTimeMillis();

            String response =
                    chatService.chatWithGemini(prompt);

            System.out.println(
                    "GEMINI fallback time = "
                            + (System.currentTimeMillis()
                            - providerStart)
                            + " ms"
            );

            chatService.saveAiMessage(
                    conversationId,
                    response
            );

            System.out.println(
                    "Fallback provider = GEMINI"
            );

            return response;

        } catch (Exception e) {

            System.out.println(
                    "Gemini failed: "
                            + e.getMessage()
            );
        }

        // =====================================================
        // 7. Final fallback - Ollama
        // =====================================================

        try {

            long providerStart =
                    System.currentTimeMillis();

            String response =
                    chatService.chatWithOllama(prompt);

            System.out.println(
                    "OLLAMA fallback time = "
                            + (System.currentTimeMillis()
                            - providerStart)
                            + " ms"
            );

            chatService.saveAiMessage(
                    conversationId,
                    response
            );

            System.out.println(
                    "Fallback provider = OLLAMA"
            );

            return response;

        } catch (Exception e) {

            System.out.println(
                    "Ollama failed: "
                            + e.getMessage()
            );
        }

        // =====================================================
        // 8. All providers failed
        // =====================================================

        throw new RuntimeException(
                "All AI providers failed"
        );
    }
}