package com.tudianersha.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AmapPoiService {
    
    @Value("${amap.api.key:bf0e97cd7a4c3c6aaa8ecf3dd7485381}")
    private String apiKey;
    
    private final OkHttpClient client;
    private final Gson gson;
    
    public AmapPoiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    /**
     * Search POI by keyword and get photos
     * 
     * @param keyword POI name (e.g., "西湖")
     * @param city City name (e.g., "杭州")
     * @return POI info including photos
     */
    public Map<String, Object> searchPoiWithPhotos(String keyword, String city) throws IOException {
        // 最多重试3次，应对高德API限流
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                Map<String, Object> result = doSearchPoiWithPhotos(keyword, city);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
                // 如果返回空结果，可能是限流，等待后重试
                retryCount++;
                if (retryCount < maxRetries) {
                    System.out.println("[Amap POI] 未获取到数据，第" + retryCount + "次重试，等待500ms...");
                    Thread.sleep(500); // 等待500ms后重试
                }
            } catch (Exception e) {
                retryCount++;
                if (retryCount < maxRetries) {
                    System.out.println("[Amap POI] 请求失败，第" + retryCount + "次重试，等待500ms...");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw new IOException("高德API请求失败: " + e.getMessage());
                }
            }
        }
        return new HashMap<>();
    }
    
    /**
     * 实际执行POI搜索
     */
    private Map<String, Object> doSearchPoiWithPhotos(String keyword, String city) throws IOException {
        // 优先搜索景点类型
        // URL encode parameters to handle Chinese characters
        String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
        String encodedCity = java.net.URLEncoder.encode(city, "UTF-8");
        
        String url = String.format(
            "https://restapi.amap.com/v3/place/text?keywords=%s&city=%s&types=110000|120000|130000|140000&offset=5&page=1&key=%s&extensions=all",
            encodedKeyword, encodedCity, apiKey
        );
        
        System.out.println("[Amap POI Search] Searching for: " + keyword + " in " + city);
        System.out.println("[Amap POI Search] API Key: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null"));
        System.out.println("[Amap POI Search] Request URL: " + url);
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Amap API call failed: " + response);
            }
            
            String responseBody = response.body().string();
            System.out.println("[Amap POI Search] Raw API response: " + responseBody);
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            // 检查是否限流
            String status = jsonResponse.has("status") ? jsonResponse.get("status").getAsString() : "1";
            String infocode = jsonResponse.has("infocode") ? jsonResponse.get("infocode").getAsString() : "";
            
            if ("0".equals(status) && "10021".equals(infocode)) {
                System.out.println("[Amap POI Search] ✗ 限流错误 (CUQPS_HAS_EXCEEDED_THE_LIMIT)，将重试...");
                return new HashMap<>(); // 返回空结果触发重试
            }
            
            System.out.println("[Amap POI Search] API status: " + status);
            System.out.println("[Amap POI Search] Has pois: " + jsonResponse.has("pois"));
            
            Map<String, Object> result = new HashMap<>();
            
            if (jsonResponse.has("pois")) {
                JsonArray pois = jsonResponse.getAsJsonArray("pois");
                System.out.println("[Amap POI Search] Found " + pois.size() + " results");
                
                if (pois.size() > 0) {
                    // 查找最匹配的景点（名称最相似的）
                    JsonObject bestMatch = null;
                    int bestMatchScore = 0;
                    
                    for (int i = 0; i < Math.min(5, pois.size()); i++) {
                        JsonObject poi = pois.get(i).getAsJsonObject();
                        String poiName = poi.has("name") ? poi.get("name").getAsString() : "";
                        String poiType = poi.has("type") ? poi.get("type").getAsString() : "";
                        
                        System.out.println("[Amap POI] #" + i + ": " + poiName + " (" + poiType + ")");
                        
                        // 计算匹配分数（名称匹配优先级最高）
                        int score = 0;
                        
                        // 完全匹配：最高优先级
                        if (poiName.equals(keyword)) {
                            score += 100;
                        }
                        // 名称包含关键词：高优先级
                        else if (poiName.contains(keyword)) {
                            score += 50;
                            // 关键词在开头：额外加分
                            if (poiName.startsWith(keyword)) {
                                score += 20;
                            }
                        }
                        // 关键词包含在名称中（部分匹配）
                        else if (keyword.length() > 1 && poiName.contains(keyword.substring(0, keyword.length() - 1))) {
                            score += 10;
                        }
                        
                        // 景点类型优先（但优先级低于名称匹配）
                        if (poiType.contains("景点") || poiType.contains("旅游") || poiType.contains("公园")) {
                            score += 5;
                        }
                        
                        System.out.println("[Amap POI] Score for " + poiName + ": " + score);
                        
                        if (score > bestMatchScore) {
                            bestMatchScore = score;
                            bestMatch = poi;
                        }
                    }
                    
                    // 如果没有找到好匹配，使用第一个结果
                    if (bestMatch == null) {
                        bestMatch = pois.get(0).getAsJsonObject();
                    }
                    
                    JsonObject poi = bestMatch;
                    
                    // Extract basic info - 安全提取（高德API可能返回空数组而不是字符串）
                    if (poi.has("name") && poi.get("name").isJsonPrimitive()) {
                        String poiName = poi.get("name").getAsString();
                        result.put("name", poiName);
                        System.out.println("[Amap POI] Selected: " + poiName);
                    }
                    if (poi.has("address") && poi.get("address").isJsonPrimitive()) {
                        result.put("address", poi.get("address").getAsString());
                    }
                    if (poi.has("location") && poi.get("location").isJsonPrimitive()) {
                        result.put("location", poi.get("location").getAsString());
                    }
                    if (poi.has("type") && poi.get("type").isJsonPrimitive()) {
                        result.put("type", poi.get("type").getAsString());
                    }
                    
                    // Extract photos
                    if (poi.has("photos")) {
                        List<Map<String, String>> photos = new ArrayList<>();
                        JsonArray photosArray = poi.getAsJsonArray("photos");
                        System.out.println("[Amap POI] Found " + photosArray.size() + " photos");
                        
                        for (int i = 0; i < Math.min(5, photosArray.size()); i++) {
                            JsonObject photo = photosArray.get(i).getAsJsonObject();
                            Map<String, String> photoInfo = new HashMap<>();
                            
                            // 安全提取URL
                            if (photo.has("url")) {
                                if (photo.get("url").isJsonPrimitive()) {
                                    photoInfo.put("url", photo.get("url").getAsString());
                                }
                            }
                            
                            // 安全提取title（可能是Array或String）
                            if (photo.has("title")) {
                                JsonElement titleElement = photo.get("title");
                                if (titleElement.isJsonPrimitive()) {
                                    photoInfo.put("title", titleElement.getAsString());
                                } else if (titleElement.isJsonArray() && titleElement.getAsJsonArray().size() > 0) {
                                    // 如果title是数组，取第一个元素
                                    photoInfo.put("title", titleElement.getAsJsonArray().get(0).getAsString());
                                }
                            }
                            
                            // 只添加有url的图片
                            if (photoInfo.containsKey("url") && !photoInfo.get("url").isEmpty()) {
                                photos.add(photoInfo);
                            }
                        }
                        
                        // 只有当photos不为空时才添加到result中
                        if (!photos.isEmpty()) {
                            result.put("photos", photos);
                            System.out.println("[Amap POI] Added " + photos.size() + " valid photos");
                        } else {
                            System.out.println("[Amap POI] No valid photos (no URL)");
                        }
                    } else {
                        System.out.println("[Amap POI] No photos field in response");
                    }
                    
                    // Extract rating and cost - 安全提取（高德API可能返回空数组而不是字符串）
                    if (poi.has("biz_ext") && poi.get("biz_ext").isJsonObject()) {
                        JsonObject bizExt = poi.getAsJsonObject("biz_ext");
                        if (bizExt.has("rating") && bizExt.get("rating").isJsonPrimitive()) {
                            result.put("rating", bizExt.get("rating").getAsString());
                        }
                        if (bizExt.has("cost") && bizExt.get("cost").isJsonPrimitive()) {
                            result.put("cost", bizExt.get("cost").getAsString());
                        }
                    }
                }
            }
            
            return result;
        }
    }
    
    /**
     * Get first photo URL for a POI
     * 
     * @param keyword POI name
     * @param city City name
     * @return Photo URL or default placeholder
     */
    public String getPoiPhotoUrl(String keyword, String city) {
        try {
            Map<String, Object> poiInfo = searchPoiWithPhotos(keyword, city);
            if (poiInfo.containsKey("photos")) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> photos = (List<Map<String, String>>) poiInfo.get("photos");
                if (!photos.isEmpty() && photos.get(0).containsKey("url")) {
                    return photos.get(0).get("url");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get POI photo: " + e.getMessage());
        }
        
        // Return default placeholder
        return "https://picsum.photos/600/300?random=" + keyword.hashCode();
    }
    
    /**
     * Search POI for autocomplete suggestions
     * 
     * @param keyword Partial keyword
     * @param city City name
     * @param types POI types (e.g., "110000|120000|130000|140000")
     * @return Search result with POI list
     */
    public Map<String, Object> searchPoi(String keyword, String city, String types) throws IOException {
        // URL encode parameters to handle special characters
        String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
        String encodedCity = java.net.URLEncoder.encode(city, "UTF-8");
        String encodedTypes = java.net.URLEncoder.encode(types, "UTF-8");
        
        String url = String.format(
            "https://restapi.amap.com/v3/place/text?keywords=%s&city=%s&types=%s&offset=20&page=1&key=%s",
            encodedKeyword, encodedCity, encodedTypes, apiKey
        );
        
        System.out.println("[Amap POI Search] Autocomplete search: " + keyword + " in " + city);
        System.out.println("[Amap POI Search] Request URL: " + url);
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Amap API call failed: " + response);
            }
            
            String responseBody = response.body().string();
            System.out.println("[Amap POI Search] Raw API response: " + responseBody);
            
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            System.out.println("[Amap POI Search] API status: " + (jsonResponse.has("status") ? jsonResponse.get("status").getAsString() : "no status"));
            System.out.println("[Amap POI Search] Has pois: " + jsonResponse.has("pois"));
            
            Map<String, Object> result = new HashMap<>();
            
            if (jsonResponse.has("pois")) {
                JsonArray pois = jsonResponse.getAsJsonArray("pois");
                System.out.println("[Amap POI Search] Found " + pois.size() + " results");
                
                List<Map<String, String>> poiList = new ArrayList<>();
                for (int i = 0; i < pois.size(); i++) {
                    JsonObject poi = pois.get(i).getAsJsonObject();
                    Map<String, String> poiData = new HashMap<>();
                    
                    poiData.put("name", poi.has("name") ? poi.get("name").getAsString() : "");
                    poiData.put("type", poi.has("type") ? poi.get("type").getAsString() : "");
                    poiData.put("address", poi.has("address") ? poi.get("address").getAsString() : "");
                    poiData.put("location", poi.has("location") ? poi.get("location").getAsString() : "");
                    
                    poiList.add(poiData);
                }
                
                result.put("success", true);
                result.put("count", pois.size());
                result.put("pois", poiList);
            } else {
                result.put("success", false);
                result.put("count", 0);
                result.put("pois", new ArrayList<>());
            }
            
            return result;
        }
    }
    
    /**
     * Search POI list by keyword and types
     * 
     * @param keyword Partial keyword
     * @param city City name
     * @param types POI types (e.g., "110000|120000|130000|140000")
     * @return List of POIs
     */
    public List<Map<String, Object>> searchPoiList(String keyword, String city, String types) throws IOException {
        Map<String, Object> result = searchPoi(keyword, city, types != null ? types : "110000|120000|130000|140000");
        
        if (result.containsKey("pois")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> pois = (List<Map<String, Object>>) result.get("pois");
            return pois;
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Search nearby attractions around a location
     * 
     * @param location Center location in format "longitude,latitude"
     * @param radius Search radius in meters (e.g., 3000)
     * @return List of nearby attractions
     */
    public List<Map<String, Object>> searchNearbyAttractions(String location, int radius) throws IOException {
        String url = String.format(
            "https://restapi.amap.com/v3/place/around?location=%s&radius=%d&types=110000|120000|130000|140000&offset=20&page=1&key=%s&extensions=all",
            location, radius, apiKey
        );
        
        System.out.println("[Amap Nearby Search] Searching near: " + location + " radius: " + radius);
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Amap API call failed: " + response);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            List<Map<String, Object>> attractions = new ArrayList<>();
            
            if (jsonResponse.has("pois")) {
                JsonArray pois = jsonResponse.getAsJsonArray("pois");
                System.out.println("[Amap Nearby Search] Found " + pois.size() + " nearby attractions");
                
                for (int i = 0; i < pois.size(); i++) {
                    JsonObject poi = pois.get(i).getAsJsonObject();
                    Map<String, Object> attraction = new HashMap<>();
                    
                    if (poi.has("name") && poi.get("name").isJsonPrimitive()) {
                        attraction.put("name", poi.get("name").getAsString());
                    }
                    if (poi.has("type") && poi.get("type").isJsonPrimitive()) {
                        attraction.put("type", poi.get("type").getAsString());
                    }
                    if (poi.has("address") && poi.get("address").isJsonPrimitive()) {
                        attraction.put("address", poi.get("address").getAsString());
                    }
                    if (poi.has("location") && poi.get("location").isJsonPrimitive()) {
                        attraction.put("location", poi.get("location").getAsString());
                    }
                    if (poi.has("distance") && poi.get("distance").isJsonPrimitive()) {
                        attraction.put("distance", poi.get("distance").getAsString());
                    }
                    
                    // Extract photos
                    if (poi.has("photos")) {
                        List<Map<String, String>> photos = new ArrayList<>();
                        JsonArray photosArray = poi.getAsJsonArray("photos");
                        
                        for (int j = 0; j < Math.min(3, photosArray.size()); j++) {
                            JsonObject photo = photosArray.get(j).getAsJsonObject();
                            Map<String, String> photoInfo = new HashMap<>();
                            
                            if (photo.has("url") && photo.get("url").isJsonPrimitive()) {
                                photoInfo.put("url", photo.get("url").getAsString());
                            }
                            if (photo.has("title")) {
                                JsonElement titleElement = photo.get("title");
                                if (titleElement.isJsonPrimitive()) {
                                    photoInfo.put("title", titleElement.getAsString());
                                } else if (titleElement.isJsonArray() && titleElement.getAsJsonArray().size() > 0) {
                                    photoInfo.put("title", titleElement.getAsJsonArray().get(0).getAsString());
                                }
                            }
                            
                            photos.add(photoInfo);
                        }
                        attraction.put("photos", photos);
                    }
                    
                    // Extract rating
                    if (poi.has("biz_ext") && poi.get("biz_ext").isJsonObject()) {
                        JsonObject bizExt = poi.getAsJsonObject("biz_ext");
                        if (bizExt.has("rating") && bizExt.get("rating").isJsonPrimitive()) {
                            attraction.put("rating", bizExt.get("rating").getAsString());
                        }
                    }
                    
                    attractions.add(attraction);
                }
            }
            
            return attractions;
        }
    }
    
    /**
     * Geocode address to get city name
     * 
     * @param address Location address
     * @return Geocoding result with city information
     */
    public Map<String, Object> geocode(String address) throws IOException {
        String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");
        String url = String.format(
            "https://restapi.amap.com/v3/geocode/geo?address=%s&key=%s",
            encodedAddress, apiKey
        );
        
        System.out.println("[Amap Geocode] Resolving address: " + address);
        System.out.println("[Amap Geocode] Request URL: " + url);
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Amap Geocode API call failed: " + response);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            System.out.println("[Amap Geocode] API response: " + responseBody);
            
            Map<String, Object> result = new HashMap<>();
            
            if (jsonResponse.has("status") && "1".equals(jsonResponse.get("status").getAsString())) {
                if (jsonResponse.has("geocodes") && jsonResponse.getAsJsonArray("geocodes").size() > 0) {
                    JsonObject geocode = jsonResponse.getAsJsonArray("geocodes").get(0).getAsJsonObject();
                    
                    String city = "";
                    if (geocode.has("city") && !geocode.get("city").isJsonNull()) {
                        JsonElement cityElement = geocode.get("city");
                        if (cityElement.isJsonArray() && cityElement.getAsJsonArray().size() == 0) {
                            // Empty array, try province
                            city = geocode.has("province") ? geocode.get("province").getAsString() : "";
                        } else if (cityElement.isJsonPrimitive() && !cityElement.getAsString().isEmpty()) {
                            city = cityElement.getAsString();
                        } else {
                            city = geocode.has("province") ? geocode.get("province").getAsString() : "";
                        }
                    } else {
                        city = geocode.has("province") ? geocode.get("province").getAsString() : "";
                    }
                    
                    result.put("success", true);
                    result.put("city", city);
                    result.put("province", geocode.has("province") ? geocode.get("province").getAsString() : "");
                    result.put("district", geocode.has("district") ? geocode.get("district").getAsString() : "");
                    
                    System.out.println("[Amap Geocode] Resolved city: " + city);
                } else {
                    result.put("success", false);
                    result.put("message", "No geocode results found");
                }
            } else {
                result.put("success", false);
                result.put("message", jsonResponse.has("info") ? jsonResponse.get("info").getAsString() : "Unknown error");
            }
            
            return result;
        }
    }
}
