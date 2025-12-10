package com.tudianersha.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class KimiAIService {
    
    @Value("${kimi.api.key}")
    private String apiKey;
    
    @Value("${kimi.api.url}")
    private String apiUrl;
    
    @Value("${kimi.api.model}")
    private String model;
    
    private final OkHttpClient client;
    private final Gson gson;
    
    public KimiAIService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)  // 增加连接超时到120秒
                .writeTimeout(120, TimeUnit.SECONDS)    // 增加写入超时到120秒
                .readTimeout(180, TimeUnit.SECONDS)     // 增加读取超时到180秒（AI生成需要更长时间）
                .build();
        this.gson = new Gson();
    }
    
    /**
     * Call Kimi AI API to generate travel routes
     * 
     * @param prompt The prompt text containing all requirements
     * @return AI generated response
     * @throws IOException if API call fails
     */
    public String generateRoute(String prompt) throws IOException {
        // Build request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        
        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "你是一个专业的旅行规划师，擅长根据用户需求生成详细的旅行路线方案。");
        messages.add(systemMessage);
        
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);
        
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 4096); // 增加token限制，确保能生成完整的多天行程
        
        // Create request
        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Kimi API call failed: " + response);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            // Extract AI response content
            if (jsonResponse.has("choices")) {
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    JsonObject message = firstChoice.getAsJsonObject("message");
                    return message.get("content").getAsString();
                }
            }
            
            throw new IOException("Unexpected response format from Kimi API");
        }
    }
    
    /**
     * 生成景点介绍
     * 
     * @param attractionName 景点名称
     * @param city 城市名称
     * @return AI生成的景点介绍
     * @throws IOException if API call fails
     */
    public String generateAttractionIntroduction(String attractionName, String city) throws IOException {
        // 构建提示词
        String prompt = String.format(
            "请为「%s」（位于%s）生成一段300-500字的景点介绍。\n\n" +
            "介绍应包含：\n" +
            "1. 景点的历史背景和文化意义\n" +
            "2. 主要看点和特色\n" +
            "3. 游览建议和注意事项\n" +
            "4. 推荐的游玩时长\n\n" +
            "语言风格：亲切、实用、有趣，避免过于正式或干巴。请直接输出介绍文字，不要添加标题或格式化符号。",
            attractionName, city
        );
        
        // 构建请求体
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        
        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "你是一个专业的旅游导游，擅长介绍各种景点的历史文化和游览建议。");
        messages.add(systemMessage);
        
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);
        
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 1000);
        
        // 创建请求
        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        // 执行请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Kimi API call failed: " + response);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            // 提取AI响应内容
            if (jsonResponse.has("choices")) {
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    JsonObject message = firstChoice.getAsJsonObject("message");
                    return message.get("content").getAsString();
                }
            }
            
            throw new IOException("Unexpected response format from Kimi API");
        }
    }
}
