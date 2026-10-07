package com.in.rohit.spring_ai_agent;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.in.rohit.spring_ai_agent.dto.ChatRequest;
import com.in.rohit.spring_ai_agent.dto.ChatResponse;
import com.in.rohit.spring_ai_agent.dto.ConversationResponse;
import com.in.rohit.spring_ai_agent.entity.Message;
import com.in.rohit.spring_ai_agent.service.PdfTextExtractorService;

/*
 * WHY:
 * ChatController chat, history aur file upload API requests
 * ko handle karta hai.
 */

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AIRouter aiRouter;

    private final ChatService chatService;

    private final PdfTextExtractorService pdfTextExtractorService;

    public ChatController(
            AIRouter aiRouter,
            ChatService chatService,
            PdfTextExtractorService pdfTextExtractorService) {

        this.aiRouter = aiRouter;
        this.chatService = chatService;
        this.pdfTextExtractorService = pdfTextExtractorService;
    }

    @PostMapping
    public ChatResponse chat(
            @RequestBody ChatRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        String response = aiRouter.route(
                email,
                request.getConversationId(),
                request.getMessage()
        );

        return new ChatResponse(
                request.getConversationId(),
                response
        );
    }

    @GetMapping("/history")
    public List<ConversationResponse> getUserHistory(
            Authentication authentication) {

        String email = authentication.getName();

        return chatService.getUserConversationsByEmail(email);
    }

    @GetMapping("/history/{conversationId}")
    public List<Message> getChatHistory(
            @PathVariable String conversationId,
            Authentication authentication) {

        String email = authentication.getName();

        return chatService.getMessages(
                conversationId,
                email
        );
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(
                    value = "message",
                    defaultValue = "Describe this image"
            ) String message,
            @RequestParam("conversationId") String conversationId,
            Authentication authentication) {

        if (file.isEmpty()) {
            return "File is empty";
        }

        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        String email = authentication.getName();

        System.out.println("Uploaded file = " + fileName);
        System.out.println("File size = " + file.getSize() + " bytes");
        System.out.println("Content type = " + contentType);
        System.out.println("Conversation ID = " + conversationId);

        try {

            // PDF processing
            if ("application/pdf".equals(contentType)) {

                String pdfText =
                        pdfTextExtractorService.extractText(file);

                if (pdfText == null || pdfText.isBlank()) {
                    return "PDF uploaded, but no readable text was found.";
                }

                chatService.saveUserMessage(
                        conversationId,
                        email,
                        "[PDF: " + fileName + "] " + message
                );

                return pdfText;
            }

            // Image processing
            if (contentType != null
                    && contentType.startsWith("image/")) {

                chatService.saveUserMessage(
                        conversationId,
                        email,
                        "[Image: " + fileName + "] " + message
                );

                String response =
                        chatService.chatWithGeminiVision(
                                file.getBytes(),
                                contentType,
                                message
                        );

                chatService.saveAiMessage(
                        conversationId,
                        response
                );

                return response;
            }

            return "Unsupported file type: " + contentType;

        } catch (Exception e) {

            e.printStackTrace();

            return "File processing failed: "
                    + e.getMessage();
        }
    }

    @DeleteMapping("/history/{conversationId}")
    public String deleteChat(
            @PathVariable String conversationId,
            Authentication authentication) {

        String email = authentication.getName();

        chatService.deleteChat(
                conversationId,
                email
        );

        return "Chat deleted successfully";
    }
}