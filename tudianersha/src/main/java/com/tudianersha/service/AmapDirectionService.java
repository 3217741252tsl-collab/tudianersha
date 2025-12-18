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
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 高德路径规划服务
 * 用于获取景点间的交通信息：距离、时间、交通方式、费用
 */
@Service
public class AmapDirectionService {
    
    @Value("${amap.api.key:4290f3a6e308a95a70bc29f5577a6a21}")
    private String apiKey;
    
    // 距离阈值：10km，超过此距离认为景点安排不合理
    private static final int MAX_DISTANCE_THRESHOLD = 10000; // 单位：米
    
    // 步行推荐阈值：小于1km推荐步行
    private static final int WALKING_THRESHOLD = 1000; // 单位：米
    
    // 极短距离阈值：小于100米认为是同一位置，无需交通
    private static final int MIN_DISTANCE_THRESHOLD = 100; // 单位：米
    
    private final OkHttpClient client;
    private final Gson gson;
    
    public AmapDirectionService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    /**
     * 获取两点之间的交通信息
     * 自动选择最优交通方式：步行(<1km) > 公交/地铁 > 驾车
     * 
     * @param originLocation 起点经纬度 "lng,lat"
     * @param destLocation 终点经纬度 "lng,lat"
     * @param city 城市名称
     * @return 交通信息Map
     */
    public Map<String, Object> getTransportInfo(String originLocation, String destLocation, String city) {
        return getTransportInfo(originLocation, destLocation, city, null);
    }
    
    /**
     * 获取两点之间的交通信息（带目的地名称）
     * 自动选择最优交通方式：步行(<1km) > 公交/地铁 > 驾车
     * 
     * @param originLocation 起点经纬度 "lng,lat"
     * @param destLocation 终点经纬度 "lng,lat"
     * @param city 城市名称
     * @param destinationName 目的地名称（用于步行描述）
     * @return 交通信息Map
     */
    public Map<String, Object> getTransportInfo(String originLocation, String destLocation, String city, String destinationName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 先获取直线距离，决定使用哪种交通方式
            double distance = calculateDistance(originLocation, destLocation);
            System.out.println("[交通规划] 直线距离: " + distance + "米");
            
            // 2. 极短距离（<100米）直接跳过，认为是同一位置
            if (distance < MIN_DISTANCE_THRESHOLD) {
                System.out.println("[交通规划] 距离过短(" + distance + "米)，跳过交通计算");
                return result; // 返回空结果，表示无需交通
            }
            
            // 3. 根据距离选择交通方式
            if (distance < WALKING_THRESHOLD) {
                // 距离小于1km，推荐步行
                result = getWalkingRoute(originLocation, destLocation);
                result.put("recommendedMethod", "步行");
            } else if (distance <= MAX_DISTANCE_THRESHOLD) {
                // 1-10km，优先公交/地铁
                Map<String, Object> transitResult = getTransitRoute(originLocation, destLocation, city, destinationName);
                if (transitResult != null && !transitResult.isEmpty() && transitResult.containsKey("duration")) {
                    result = transitResult;
                    result.put("recommendedMethod", "公交/地铁");
                } else {
                    // 如果没有公交方案，使用驾车
                    result = getDrivingRoute(originLocation, destLocation);
                    result.put("recommendedMethod", "驾车/打车");
                }
            } else {
                // 超过10km，标记距离过远，但仍优先尝试公交
                result.put("warning", "景点距离过远，建议调整行程安排");
                result.put("distanceTooFar", true);
                
                // 优先尝试公交/地铁方案
                Map<String, Object> transitResult = getTransitRoute(originLocation, destLocation, city, destinationName);
                if (transitResult != null && !transitResult.isEmpty() && transitResult.containsKey("duration")) {
                    result.putAll(transitResult);
                    result.put("recommendedMethod", "公交/地铁（距离较远）");
                } else {
                    // 如果没有公交方案，使用驾车
                    Map<String, Object> drivingResult = getDrivingRoute(originLocation, destLocation);
                    result.putAll(drivingResult);
                    result.put("recommendedMethod", "驾车/打车（距离较远）");
                }
            }
            
            result.put("straightDistance", distance);
            
        } catch (Exception e) {
            System.err.println("[交通规划] 获取交通信息失败: " + e.getMessage());
            e.printStackTrace();
            result.put("error", "获取交通信息失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取步行路线
     */
    public Map<String, Object> getWalkingRoute(String origin, String destination) throws IOException {
        String url = String.format(
            "https://restapi.amap.com/v3/direction/walking?origin=%s&destination=%s&key=%s",
            origin, destination, apiKey
        );
        
        System.out.println("[步行路线] 请求URL: " + url);
        
        Request request = new Request.Builder().url(url).get().build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("步行路线API调用失败: " + response);
            }
            
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            
            Map<String, Object> result = new HashMap<>();
            
            if ("1".equals(json.get("status").getAsString()) && json.has("route")) {
                JsonObject route = json.getAsJsonObject("route");
                if (route.has("paths") && route.getAsJsonArray("paths").size() > 0) {
                    JsonObject path = route.getAsJsonArray("paths").get(0).getAsJsonObject();
                    
                    int distance = path.get("distance").getAsInt();
                    int duration = path.get("duration").getAsInt();
                    
                    result.put("method", "步行");
                    result.put("distance", distance);
                    result.put("distanceText", formatDistance(distance));
                    result.put("duration", duration);
                    result.put("durationText", formatDuration(duration));
                    result.put("cost", 0); // 步行免费
                    result.put("costText", "免费");
                    result.put("details", "步行约" + formatDuration(duration));
                    
                    System.out.println("[步行路线] 距离: " + distance + "米, 时间: " + duration + "秒");
                }
            }
            
            return result;
        }
    }
    
    /**
     * 获取公交/地铁换乘路线
     */
    public Map<String, Object> getTransitRoute(String origin, String destination, String city) throws IOException {
        return getTransitRoute(origin, destination, city, null);
    }
    
    /**
     * 获取公交/地铁换乘路线（带目的地名称）
     */
    public Map<String, Object> getTransitRoute(String origin, String destination, String city, String destinationName) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        String url = String.format(
            "https://restapi.amap.com/v3/direction/transit/integrated?origin=%s&destination=%s&city=%s&strategy=0&key=%s",
            origin, destination, encodedCity, apiKey
        );
        
        System.out.println("[公交路线] 请求URL: " + url);
        
        Request request = new Request.Builder().url(url).get().build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("公交路线API调用失败: " + response);
            }
            
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            
            Map<String, Object> result = new HashMap<>();
            
            if ("1".equals(json.get("status").getAsString()) && json.has("route")) {
                JsonObject route = json.getAsJsonObject("route");
                
                if (route.has("transits") && route.getAsJsonArray("transits").size() > 0) {
                    JsonObject transit = route.getAsJsonArray("transits").get(0).getAsJsonObject();
                    
                    int distance = 0;
                    if (transit.has("distance") && transit.get("distance").isJsonPrimitive()) {
                        distance = transit.get("distance").getAsInt();
                    }
                    
                    int duration = 0;
                    if (transit.has("duration") && transit.get("duration").isJsonPrimitive()) {
                        duration = transit.get("duration").getAsInt();
                    }
                    
                    // 获取费用
                    int cost = 0;
                    if (transit.has("cost") && transit.get("cost").isJsonPrimitive()) {
                        try {
                            cost = (int) Math.ceil(Double.parseDouble(transit.get("cost").getAsString()));
                        } catch (NumberFormatException e) {
                            cost = 3; // 默认公交费用
                        }
                    } else {
                        cost = 3; // 默认公交费用
                    }
                    
                    result.put("method", "公交/地铁");
                    result.put("distance", distance);
                    result.put("distanceText", formatDistance(distance));
                    result.put("duration", duration);
                    result.put("durationText", formatDuration(duration));
                    result.put("cost", cost);
                    result.put("costText", "¥" + cost);
                    
                    // 解析详细换乘信息（结构化分步数据）
                    List<Map<String, Object>> transitSteps = parseTransitDetailsAsSteps(transit, destinationName);
                    result.put("steps", transitSteps);
                    
                    // 同时生成简洁的文本描述（向后兼容）
                    String transitDetails = formatStepsAsText(transitSteps);
                    result.put("details", transitDetails);
                    
                    System.out.println("[公交路线] 距离: " + distance + "米, 时间: " + duration + "秒, 费用: " + cost + "元");
                    System.out.println("[公交路线] 详情: " + transitDetails);
                }
            }
            
            return result;
        }
    }
    
    /**
     * 解析公交换乘详细信息（返回结构化分步数据）
     * @param transit 公交换乘信息
     * @param destinationName 目的地名称（用于最后一步的描述）
     */
    private List<Map<String, Object>> parseTransitDetailsAsSteps(JsonObject transit, String destinationName) {
        List<Map<String, Object>> steps = new ArrayList<>();
        
        if (transit.has("segments")) {
            JsonArray segments = transit.getAsJsonArray("segments");
            int totalSegments = segments.size();
            
            for (int i = 0; i < totalSegments; i++) {
                JsonObject segment = segments.get(i).getAsJsonObject();
                boolean isFirstSegment = (i == 0);
                boolean isLastSegment = (i == totalSegments - 1);
                
                // 处理步行段（在公交段之前）
                if (segment.has("walking")) {
                    JsonElement walkingElement = segment.get("walking");
                    // 高德API返回的walking可能是JsonObject或JsonArray，需要判断
                    JsonObject walking = null;
                    if (walkingElement.isJsonObject()) {
                        walking = walkingElement.getAsJsonObject();
                    } else if (walkingElement.isJsonArray() && walkingElement.getAsJsonArray().size() > 0) {
                        // 如果是数组，取第一个元素
                        JsonElement firstWalk = walkingElement.getAsJsonArray().get(0);
                        if (firstWalk.isJsonObject()) {
                            walking = firstWalk.getAsJsonObject();
                        }
                    }
                    
                    if (walking != null && walking.has("distance")) {
                        int walkDistance = 0;
                        try {
                            walkDistance = walking.get("distance").getAsInt();
                        } catch (Exception e) {
                            // 距离可能是字符串
                            try {
                                walkDistance = Integer.parseInt(walking.get("distance").getAsString());
                            } catch (Exception ex) {
                                walkDistance = 0;
                            }
                        }
                    
                    if (walkDistance > 50) { // 只显示超过50米的步行
                        Map<String, Object> walkStep = new HashMap<>();
                        walkStep.put("type", "walk");
                        walkStep.put("distance", walkDistance);
                        walkStep.put("distanceText", formatDistance(walkDistance));
                        
                        // 确定步行目的
                        if (isLastSegment && !segment.has("bus")) {
                            // 最后一段且没有公交，是走到终点（景点）
                            walkStep.put("purpose", "to_destination");
                            String destDesc = (destinationName != null && !destinationName.isEmpty()) 
                                ? destinationName : "终点";
                            walkStep.put("description", "步行" + formatDistance(walkDistance) + "至 " + destDesc);
                        } else {
                            // 走到公交站
                            walkStep.put("purpose", "to_station");
                            // 获取下一个公交站名称
                            String nextStopName = getNextBusStopName(segment);
                            if (!nextStopName.isEmpty()) {
                                walkStep.put("description", "步行" + formatDistance(walkDistance) + "至 " + nextStopName + "站");
                                walkStep.put("toStop", nextStopName);
                            } else {
                                walkStep.put("description", "步行" + formatDistance(walkDistance) + "至公交站");
                            }
                        }
                        
                        steps.add(walkStep);
                    }
                    }
                }
                
                // 处理公交/地铁段
                if (segment.has("bus")) {
                    JsonElement busElement = segment.get("bus");
                    JsonObject busObj = null;
                    if (busElement.isJsonObject()) {
                        busObj = busElement.getAsJsonObject();
                    } else if (busElement.isJsonArray() && busElement.getAsJsonArray().size() > 0) {
                        JsonElement firstBus = busElement.getAsJsonArray().get(0);
                        if (firstBus.isJsonObject()) {
                            busObj = firstBus.getAsJsonObject();
                        }
                    }
                    
                    if (busObj != null && busObj.has("buslines")) {
                        JsonArray buslines = busObj.getAsJsonArray("buslines");
                    if (buslines.size() > 0) {
                        JsonObject busline = buslines.get(0).getAsJsonObject();
                        
                        String lineName = busline.has("name") ? busline.get("name").getAsString() : "";
                        String departureStop = "";
                        String arrivalStop = "";
                        int viaStops = 0;
                        
                        if (busline.has("departure_stop") && busline.getAsJsonObject("departure_stop").has("name")) {
                            departureStop = busline.getAsJsonObject("departure_stop").get("name").getAsString();
                        }
                        if (busline.has("arrival_stop") && busline.getAsJsonObject("arrival_stop").has("name")) {
                            arrivalStop = busline.getAsJsonObject("arrival_stop").get("name").getAsString();
                        }
                        if (busline.has("via_num")) {
                            viaStops = busline.get("via_num").getAsInt();
                        }
                        
                        // 判断是地铁还是公交
                        boolean isSubway = lineName.contains("地铁") || lineName.contains("号线");
                        String lineType = isSubway ? "subway" : "bus";
                        String lineTypeText = isSubway ? "地铁" : "公交";
                        
                        Map<String, Object> busStep = new HashMap<>();
                        busStep.put("type", lineType);
                        busStep.put("lineName", lineName);
                        busStep.put("lineType", lineTypeText);
                        busStep.put("fromStop", departureStop);
                        busStep.put("toStop", arrivalStop);
                        busStep.put("viaStops", viaStops);
                        
                        // 生成描述
                        StringBuilder desc = new StringBuilder();
                        desc.append(lineTypeText).append(" ").append(cleanLineName(lineName));
                        if (!departureStop.isEmpty() && !arrivalStop.isEmpty()) {
                            desc.append("\n  ").append(departureStop).append(" → ").append(arrivalStop);
                            if (viaStops > 0) {
                                desc.append(" (").append(viaStops).append("站)");
                            }
                        }
                        busStep.put("description", desc.toString());
                        
                        steps.add(busStep);
                    }
                    }
                }
            }
        }
        
        // 后处理：如果最后一个步骤是walk，将其目的地改为终点
        if (!steps.isEmpty()) {
            Map<String, Object> lastStep = steps.get(steps.size() - 1);
            if ("walk".equals(lastStep.get("type"))) {
                lastStep.put("purpose", "to_destination");
                String distanceText = (String) lastStep.get("distanceText");
                String destDesc = (destinationName != null && !destinationName.isEmpty()) 
                    ? destinationName : "终点";
                lastStep.put("description", "步行" + distanceText + "至 " + destDesc);
            }
        }
        
        return steps;
    }
    
    /**
     * 清理线路名称，去除括号内的起终站信息
     */
    private String cleanLineName(String lineName) {
        // 去除括号内的起终站信息，如 "203路(厚街汽车站--可园首末站)" -> "203路"
        int parenIndex = lineName.indexOf('(');
        if (parenIndex > 0) {
            return lineName.substring(0, parenIndex).trim();
        }
        return lineName;
    }
    
    /**
     * 获取下一个公交站名称
     */
    private String getNextBusStopName(JsonObject segment) {
        if (segment.has("bus") && segment.getAsJsonObject("bus").has("buslines")) {
            JsonArray buslines = segment.getAsJsonObject("bus").getAsJsonArray("buslines");
            if (buslines.size() > 0) {
                JsonObject busline = buslines.get(0).getAsJsonObject();
                if (busline.has("departure_stop") && busline.getAsJsonObject("departure_stop").has("name")) {
                    return busline.getAsJsonObject("departure_stop").get("name").getAsString();
                }
            }
        }
        return "";
    }
    
    /**
     * 将分步数据格式化为文本（向后兼容）
     */
    private String formatStepsAsText(List<Map<String, Object>> steps) {
        if (steps.isEmpty()) {
            return "公交/地铁换乘";
        }
        
        List<String> descriptions = new ArrayList<>();
        for (Map<String, Object> step : steps) {
            String desc = (String) step.get("description");
            if (desc != null) {
                // 去除换行符，用于简洁文本
                descriptions.add(desc.replace("\n  ", " "));
            }
        }
        return String.join(" → ", descriptions);
    }
    
    /**
     * 解析公交换乘详细信息（已废弃，保留向后兼容）
     */
    @Deprecated
    private String parseTransitDetails(JsonObject transit) {
        List<Map<String, Object>> steps = parseTransitDetailsAsSteps(transit, null);
        return formatStepsAsText(steps);
    }
    
    /**
     * 获取驾车路线
     */
    public Map<String, Object> getDrivingRoute(String origin, String destination) throws IOException {
        String url = String.format(
            "https://restapi.amap.com/v3/direction/driving?origin=%s&destination=%s&extensions=base&strategy=0&key=%s",
            origin, destination, apiKey
        );
        
        System.out.println("[驾车路线] 请求URL: " + url);
        
        Request request = new Request.Builder().url(url).get().build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("驾车路线API调用失败: " + response);
            }
            
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            
            Map<String, Object> result = new HashMap<>();
            
            if ("1".equals(json.get("status").getAsString()) && json.has("route")) {
                JsonObject route = json.getAsJsonObject("route");
                
                // 获取出租车费用
                int taxiCost = 0;
                if (route.has("taxi_cost") && route.get("taxi_cost").isJsonPrimitive()) {
                    try {
                        taxiCost = (int) Math.ceil(Double.parseDouble(route.get("taxi_cost").getAsString()));
                    } catch (NumberFormatException e) {
                        taxiCost = 0;
                    }
                }
                
                if (route.has("paths") && route.getAsJsonArray("paths").size() > 0) {
                    JsonObject path = route.getAsJsonArray("paths").get(0).getAsJsonObject();
                    
                    int distance = path.get("distance").getAsInt();
                    int duration = path.get("duration").getAsInt();
                    
                    result.put("method", "驾车/打车");
                    result.put("distance", distance);
                    result.put("distanceText", formatDistance(distance));
                    result.put("duration", duration);
                    result.put("durationText", formatDuration(duration));
                    result.put("cost", taxiCost);
                    result.put("costText", taxiCost > 0 ? "约¥" + taxiCost : "费用待定");
                    result.put("details", "驾车约" + formatDuration(duration) + "，打车约¥" + taxiCost);
                    
                    System.out.println("[驾车路线] 距离: " + distance + "米, 时间: " + duration + "秒, 打车费: " + taxiCost + "元");
                }
            }
            
            return result;
        }
    }
    
    /**
     * 计算两点之间的直线距离（米）
     */
    private double calculateDistance(String origin, String destination) {
        try {
            String[] originParts = origin.split(",");
            String[] destParts = destination.split(",");
            
            double lng1 = Double.parseDouble(originParts[0]);
            double lat1 = Double.parseDouble(originParts[1]);
            double lng2 = Double.parseDouble(destParts[0]);
            double lat2 = Double.parseDouble(destParts[1]);
            
            // Haversine公式计算球面距离
            double R = 6371000; // 地球半径（米）
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                       Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                       Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            
            return R * c;
        } catch (Exception e) {
            System.err.println("[距离计算] 计算失败: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 格式化距离显示
     */
    private String formatDistance(int meters) {
        if (meters < 1000) {
            return meters + "米";
        } else {
            return String.format("%.1f公里", meters / 1000.0);
        }
    }
    
    /**
     * 格式化时间显示
     */
    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
        }
    }
    
    /**
     * 批量获取行程中所有景点之间的交通信息
     * 
     * @param locations 景点经纬度列表 ["lng1,lat1", "lng2,lat2", ...]
     * @param city 城市名称
     * @return 相邻景点之间的交通信息列表
     */
    public List<Map<String, Object>> getBatchTransportInfo(List<String> locations, String city) {
        List<Map<String, Object>> transportList = new ArrayList<>();
        
        for (int i = 0; i < locations.size() - 1; i++) {
            String from = locations.get(i);
            String to = locations.get(i + 1);
            
            Map<String, Object> transport = getTransportInfo(from, to, city);
            transport.put("fromIndex", i);
            transport.put("toIndex", i + 1);
            
            transportList.add(transport);
        }
        
        return transportList;
    }
    
    /**
     * 检查行程是否存在距离过远的问题
     * 
     * @param transportList 交通信息列表
     * @return 距离过远的景点索引对列表
     */
    public List<int[]> checkDistanceWarnings(List<Map<String, Object>> transportList) {
        List<int[]> warnings = new ArrayList<>();
        
        for (Map<String, Object> transport : transportList) {
            if (transport.containsKey("distanceTooFar") && (Boolean) transport.get("distanceTooFar")) {
                int fromIndex = (Integer) transport.get("fromIndex");
                int toIndex = (Integer) transport.get("toIndex");
                warnings.add(new int[]{fromIndex, toIndex});
            }
        }
        
        return warnings;
    }
    
    /**
     * 获取多个公交/地铁方案（用于交通卡片多方案选择）
     * 
     * @param origin 起点经纬度
     * @param destination 终点经纬度
     * @return 多个交通方案列表
     */
    public List<Map<String, Object>> getMultipleTransitPlans(String origin, String destination) {
        List<Map<String, Object>> plans = new ArrayList<>();
        
        try {
            // 调用高德公交路线API获取多个方案
            String url = String.format(
                "https://restapi.amap.com/v3/direction/transit/integrated?origin=%s&destination=%s&city=东莞&strategy=0&key=%s",
                origin, destination, apiKey
            );
            
            System.out.println("[多方案查询] 请求URL: " + url);
            
            Request request = new Request.Builder().url(url).get().build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("[多方案查询] API调用失败: " + response);
                    return plans;
                }
                
                String responseBody = response.body().string();
                JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                
                if ("1".equals(json.get("status").getAsString()) && json.has("route")) {
                    JsonObject route = json.getAsJsonObject("route");
                    
                    if (route.has("transits") && route.getAsJsonArray("transits").size() > 0) {
                        JsonArray transits = route.getAsJsonArray("transits");
                        
                        // 最多返回3个方案
                        int maxPlans = Math.min(3, transits.size());
                        
                        for (int i = 0; i < maxPlans; i++) {
                            JsonObject transit = transits.get(i).getAsJsonObject();
                            Map<String, Object> plan = parseTransitPlan(transit);
                            if (plan != null && !plan.isEmpty()) {
                                plans.add(plan);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[多方案查询] 获取失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return plans;
    }
    
    /**
     * 解析单个公交方案
     */
    private Map<String, Object> parseTransitPlan(JsonObject transit) {
        Map<String, Object> plan = new HashMap<>();
        
        try {
            int distance = transit.has("distance") ? transit.get("distance").getAsInt() : 0;
            int duration = transit.has("duration") ? transit.get("duration").getAsInt() : 0;
            
            int cost = 3; // 默认费用
            if (transit.has("cost") && transit.get("cost").isJsonPrimitive()) {
                try {
                    cost = (int) Math.ceil(Double.parseDouble(transit.get("cost").getAsString()));
                } catch (NumberFormatException e) {
                    cost = 3;
                }
            }
            
            // 解析segments获取详细步骤
            List<Map<String, Object>> steps = new ArrayList<>();
            String method = "公交/地铁";
            
            if (transit.has("segments") && transit.get("segments").isJsonArray()) {
                JsonArray segments = transit.getAsJsonArray("segments");
                
                for (JsonElement segElement : segments) {
                    JsonObject segment = segElement.getAsJsonObject();
                    
                    // 步行段
                    if (segment.has("walking") && segment.getAsJsonObject("walking").has("steps")) {
                        JsonObject walking = segment.getAsJsonObject("walking");
                        int walkDistance = walking.has("distance") ? walking.get("distance").getAsInt() : 0;
                        int walkDuration = walking.has("duration") ? walking.get("duration").getAsInt() : 0;
                        
                        if (walkDistance >= 50) { // 过滤极短步行
                            Map<String, Object> step = new HashMap<>();
                            step.put("type", "walk");
                            step.put("description", "步行 " + walkDistance + "米");
                            step.put("distance", walkDistance);
                            step.put("duration", walkDuration);
                            steps.add(step);
                        }
                    }
                    
                    // 公交/地铁段
                    if (segment.has("bus") && segment.getAsJsonObject("bus").has("buslines")) {
                        JsonObject bus = segment.getAsJsonObject("bus");
                        JsonArray buslines = bus.getAsJsonArray("buslines");
                        
                        if (buslines.size() > 0) {
                            JsonObject busline = buslines.get(0).getAsJsonObject();
                            String name = busline.has("name") ? busline.get("name").getAsString() : "公交";
                            int busDuration = busline.has("duration") ? busline.get("duration").getAsInt() : 0;
                            
                            Map<String, Object> step = new HashMap<>();
                            if (name.contains("地铁") || name.contains("号线")) {
                                step.put("type", "subway");
                                method = name; // 更新交通方式描述
                            } else {
                                step.put("type", "bus");
                            }
                            step.put("description", "乘坐 " + name);
                            step.put("duration", busDuration);
                            steps.add(step);
                        }
                    }
                }
            }
            
            plan.put("method", method);
            plan.put("distance", distance);
            plan.put("distanceText", formatDistance(distance));
            plan.put("duration", duration);
            plan.put("durationText", formatDuration(duration));
            plan.put("cost", (double) cost);
            plan.put("costText", "¥" + cost);
            plan.put("steps", steps);
            
        } catch (Exception e) {
            System.err.println("[解析方案] 失败: " + e.getMessage());
        }
        
        return plan;
    }
}
