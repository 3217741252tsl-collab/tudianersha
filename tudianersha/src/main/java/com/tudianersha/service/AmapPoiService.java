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
        // 优先搜索景点类型
        // URL encode parameters to handle Chinese characters
        String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
        String encodedCity = java.net.URLEncoder.encode(city, "UTF-8");
        
        String url = String.format(
            "https://restapi.amap.com/v3/place/text?keywords=%s&city=%s&types=110000|120000|130000|140000&offset=5&page=1&key=%s&extensions=all",
            encodedKeyword, encodedCity, apiKey
        );
        
        System.out.println("[Amap POI Search] Searching for: " + keyword + " in " + city);
        
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
                            
                            photos.add(photoInfo);
                        }
                        result.put("photos", photos);
                    } else {
                        System.out.println("[Amap POI] No photos found");
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
    
    /**
     * Search nearby attractions around a specific POI
     * 
     * @param poiName The name of the POI to search around
     * @param city City name
     * @return List of nearby attractions with distance
     */
    public Map<String, Object> searchNearbyAttractions(String poiName, String city) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 首先获取目标POI的位置
            Map<String, Object> poiInfo = searchPoiWithPhotos(poiName, city);
            if (!poiInfo.containsKey("location")) {
                result.put("success", false);
                result.put("message", "未找到景点位置信息");
                return result;
            }
            
            String location = (String) poiInfo.get("location");
            System.out.println("[Amap Nearby] Searching around: " + poiName + " at " + location);
            
            // 使用周边搜索API
            String encodedLocation = java.net.URLEncoder.encode(location, "UTF-8");
            String url = String.format(
                "https://restapi.amap.com/v3/place/around?location=%s&keywords=&types=110000|120000|130000|140000&radius=2000&offset=10&page=1&key=%s&extensions=all",
                encodedLocation, apiKey
            );
            
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
                
                if (jsonResponse.has("pois")) {
                    JsonArray pois = jsonResponse.getAsJsonArray("pois");
                    System.out.println("[Amap Nearby] Found " + pois.size() + " nearby POIs");
                    
                    List<Map<String, String>> attractions = new ArrayList<>();
                    
                    for (int i = 0; i < Math.min(10, pois.size()); i++) {
                        JsonObject poi = pois.get(i).getAsJsonObject();
                        String name = poi.has("name") && poi.get("name").isJsonPrimitive() 
                            ? poi.get("name").getAsString() : "";
                        
                        // 跳过相同名称的景点
                        if (name.equals(poiName)) {
                            continue;
                        }
                        
                        Map<String, String> attraction = new HashMap<>();
                        attraction.put("name", name);
                        
                        if (poi.has("address") && poi.get("address").isJsonPrimitive()) {
                            attraction.put("address", poi.get("address").getAsString());
                        }
                        
                        if (poi.has("type") && poi.get("type").isJsonPrimitive()) {
                            attraction.put("type", poi.get("type").getAsString());
                        }
                        
                        // 计算距离
                        if (poi.has("distance") && poi.get("distance").isJsonPrimitive()) {
                            String distance = poi.get("distance").getAsString();
                            try {
                                int distanceMeters = Integer.parseInt(distance);
                                if (distanceMeters < 1000) {
                                    attraction.put("distance", distanceMeters + "米");
                                } else {
                                    attraction.put("distance", String.format("%.1f公里", distanceMeters / 1000.0));
                                }
                            } catch (NumberFormatException e) {
                                attraction.put("distance", distance);
                            }
                        }
                        
                        if (poi.has("location") && poi.get("location").isJsonPrimitive()) {
                            attraction.put("location", poi.get("location").getAsString());
                        }
                        
                        attractions.add(attraction);
                    }
                    
                    result.put("success", true);
                    result.put("attractions", attractions);
                    result.put("count", attractions.size());
                } else {
                    result.put("success", false);
                    result.put("message", "暂无附近景点");
                }
            }
        } catch (Exception e) {
            System.err.println("[Amap Nearby] Error: " + e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
}
