package com.in.rohit.spring_ai_agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NvidiaTest {

    public static void main(String[] args) throws Exception {

        String apiKey = System.getenv("NVIDIA_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("NVIDIA_API_KEY not found!");
            return;
        }

        String json = """
                {
                  "model": "openai/gpt-oss-20b",
                  "messages": [
                    {
                      "role": "user",
                      "content": "Hello, introduce yourself in one sentence."
                    }
                  ],
                  "max_tokens": 100
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://integrate.api.nvidia.com/v1/chat/completions"
                ))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("HTTP Status: " + response.statusCode());
        System.out.println("Response:");
        System.out.println(response.body());
    }
}