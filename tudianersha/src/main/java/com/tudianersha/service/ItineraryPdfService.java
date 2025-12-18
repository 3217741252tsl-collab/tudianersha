package com.tudianersha.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.tudianersha.entity.AiGeneratedRoute;
import com.tudianersha.entity.TravelProject;
import com.tudianersha.entity.ProjectTask;
import com.tudianersha.entity.User;
import com.tudianersha.repository.AiGeneratedRouteRepository;
import com.tudianersha.repository.TravelProjectRepository;
import com.tudianersha.repository.ProjectTaskRepository;
import com.tudianersha.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ItineraryPdfService {

    @Autowired
    private TravelProjectRepository projectRepository;

    @Autowired
    private AiGeneratedRouteRepository routeRepository;
    
    @Autowired
    private ProjectTaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 生成行程PDF
     */
    public byte[] generateItineraryPdf(Long projectId) throws Exception {
        // 获取项目信息
        TravelProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));

        // 获取当前选择的路线
        if (project.getCurrentRouteId() == null) {
            throw new RuntimeException("项目未选择路线方案");
        }

        AiGeneratedRoute route = routeRepository.findById(project.getCurrentRouteId())
                .orElseThrow(() -> new RuntimeException("路线方案不存在"));

        // 创建PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // 加载中文字体
        PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        document.setFont(font);

        // 添加标题
        Paragraph title = new Paragraph(project.getProjectName())
                .setFont(font)
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(67, 126, 234));
        document.add(title);

        // 添加基本信息
        Paragraph info = new Paragraph()
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        info.add("目的地：" + project.getDestination() + "    ");
        if (project.getStartDate() != null && project.getEndDate() != null) {
            info.add("日期：" + project.getStartDate() + " 至 " + project.getEndDate());
        }
        document.add(info);

        // 解析并添加每日行程
        if (route.getDailyItinerary() != null) {
            Gson gson = new Gson();
            
            // 解析预算数据
            Map<String, Double> budgets = new HashMap<>();
            if (route.getBudgetsJson() != null && !route.getBudgetsJson().isEmpty()) {
                try {
                    budgets = gson.fromJson(
                        route.getBudgetsJson(),
                        new TypeToken<Map<String, Double>>(){}.getType()
                    );
                    System.out.println("[PDF] Loaded budgets: " + budgets.size() + " items");
                } catch (Exception e) {
                    System.err.println("[PDF] Error parsing budgets: " + e.getMessage());
                }
            }
            
            JsonArray dailyItinerary = gson.fromJson(route.getDailyItinerary(), JsonArray.class);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = project.getStartDate() != null ? 
                    LocalDate.parse(project.getStartDate(), formatter) : LocalDate.now();

            for (int i = 0; i < dailyItinerary.size(); i++) {
                JsonObject dayData = dailyItinerary.get(i).getAsJsonObject();
                int dayNumber = dayData.has("day") ? dayData.get("day").getAsInt() : (i + 1);

                // 计算具体日期
                LocalDate currentDate = startDate.plusDays(i);
                String dateStr = currentDate.format(formatter);

                // 添加天数标题
                Paragraph dayTitle = new Paragraph("第 " + dayNumber + " 天 (" + dateStr + ")")
                        .setFont(font)
                        .setFontSize(16)
                        .setBold()
                        .setFontColor(new DeviceRgb(102, 126, 234))
                        .setMarginTop(15)
                        .setMarginBottom(10);
                document.add(dayTitle);

                // 添加活动列表
                if (dayData.has("activities")) {
                    JsonArray activities = dayData.getAsJsonArray("activities");
                    
                    // 创建表格（删除时间列，只保留活动内容和预算）
                    Table table = new Table(UnitValue.createPercentArray(new float[]{75, 25}))
                            .useAllAvailableWidth()
                            .setMarginBottom(10);
                    
                    // 表头
                    Cell headerActivity = new Cell()
                            .add(new Paragraph("活动内容").setFont(font).setFontSize(10).setBold())
                            .setBackgroundColor(new DeviceRgb(67, 126, 234))
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(8);
                    table.addCell(headerActivity);
                    
                    Cell headerBudget = new Cell()
                            .add(new Paragraph("预算（元）").setFont(font).setFontSize(10).setBold())
                            .setBackgroundColor(new DeviceRgb(67, 126, 234))
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(8);
                    table.addCell(headerBudget);
                    
                    // 获取当天的交通信息
                    JsonArray transports = dayData.has("transports") ? dayData.getAsJsonArray("transports") : new JsonArray();
                    
                    // 计算当日总预算
                    double dayTotalBudget = 0.0;
                    
                    // 检查是否有出发地交通（第一天）
                    JsonObject departureTransport = null;
                    for (int t = 0; t < transports.size(); t++) {
                        JsonObject transport = transports.get(t).getAsJsonObject();
                        if ((transport.has("fromIndex") && transport.get("fromIndex").getAsInt() == -1) || 
                            (transport.has("isDeparture") && transport.get("isDeparture").getAsBoolean())) {
                            departureTransport = transport;
                            break;
                        }
                    }
                    
                    // 如果有出发地交通，先添加出发地行
                    if (departureTransport != null) {
                        String fromName = departureTransport.has("fromName") ? departureTransport.get("fromName").getAsString() : "出发地";
                        
                        // 出发地行
                        Cell depCell = new Cell()
                                .add(new Paragraph("[出发] " + fromName).setFont(font).setFontSize(10).setBold())
                                .setBackgroundColor(new DeviceRgb(245, 243, 255))
                                .setPadding(8);
                        table.addCell(depCell);
                        table.addCell(new Cell().add(new Paragraph("-").setFont(font).setFontSize(10)).setTextAlignment(TextAlignment.CENTER).setPadding(8));
                        
                        // 出发地交通信息（详细）
                        addDetailedTransportCell(table, font, departureTransport);
                    }

                    for (int actIdx = 0; actIdx < activities.size(); actIdx++) {
                        JsonElement activityElement = activities.get(actIdx);
                        
                        // 处理新的数据结构：{text: "...", poiInfo: {...}} 或者纯字符串
                        String activity;
                        String poiName = "";
                        if (activityElement.isJsonObject()) {
                            JsonObject activityObj = activityElement.getAsJsonObject();
                            activity = activityObj.has("text") ? activityObj.get("text").getAsString() : activityElement.toString();
                            // 获取景点名称
                            if (activityObj.has("poiInfo")) {
                                JsonObject poiInfo = activityObj.getAsJsonObject("poiInfo");
                                if (poiInfo.has("name")) {
                                    poiName = poiInfo.get("name").getAsString();
                                }
                            }
                        } else {
                            activity = activityElement.getAsString();
                        }
                        
                        // 提取活动内容（去除时间前缀）
                        String content = activity;
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(\\d{2}:\\d{2}-\\d{2}:\\d{2})\\s*(.*)");
                        java.util.regex.Matcher matcher = pattern.matcher(activity);
                        if (matcher.matches()) {
                            content = matcher.group(2);
                        }
                        
                        // 获取预算
                        String budgetKey = (i + 1) + "-" + actIdx;
                        Double budget = budgets.getOrDefault(budgetKey, 0.0);
                        if (budget > 0) {
                            dayTotalBudget += budget;
                        }

                        // 活动单元格
                        Cell contentCell = new Cell()
                                .add(new Paragraph("[" + (actIdx + 1) + "] " + content).setFont(font).setFontSize(10))
                                .setPadding(8);
                        table.addCell(contentCell);
                        
                        // 预算单元格
                        Cell budgetCell = new Cell()
                                .add(new Paragraph(budget > 0 ? String.format("%.2f", budget) : "-")
                                    .setFont(font)
                                    .setFontSize(10))
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setPadding(8);
                        if (budget > 0) {
                            budgetCell.setBackgroundColor(new DeviceRgb(230, 244, 234));
                        }
                        table.addCell(budgetCell);
                        
                        // 查找并添加到下一个景点的交通信息
                        for (int t = 0; t < transports.size(); t++) {
                            JsonObject transport = transports.get(t).getAsJsonObject();
                            // 跳过出发地交通（已经处理过）
                            if ((transport.has("fromIndex") && transport.get("fromIndex").getAsInt() == -1) || 
                                (transport.has("isDeparture") && transport.get("isDeparture").getAsBoolean())) {
                                continue;
                            }
                            
                            // 检查是否是当前景点的出发交通
                            if (transport.has("fromIndex") && transport.get("fromIndex").getAsInt() == actIdx) {
                                addDetailedTransportCell(table, font, transport);
                                break;
                            }
                        }
                    }
                    
                    document.add(table);
                    
                    // 添加当日总预算
                    if (dayTotalBudget > 0) {
                        Paragraph budgetSummary = new Paragraph("当日总预算：¥" + String.format("%.2f", dayTotalBudget))
                                .setFont(font)
                                .setFontSize(12)
                                .setBold()
                                .setFontColor(new DeviceRgb(16, 185, 129))
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setMarginBottom(5);
                        document.add(budgetSummary);
                        
                        // 如果项目有总预算，显示对比
                        if (project.getTotalBudget() != null && project.getTotalBudget() > 0) {
                            double dailyPlan = project.getTotalBudget() / project.getDays();
                            String comparison;
                            DeviceRgb color;
                            if (dayTotalBudget > dailyPlan) {
                                comparison = String.format("超出计划 ¥%.2f", dayTotalBudget - dailyPlan);
                                color = new DeviceRgb(239, 68, 68);
                            } else if (dayTotalBudget < dailyPlan) {
                                comparison = String.format("剩余 ¥%.2f", dailyPlan - dayTotalBudget);
                                color = new DeviceRgb(16, 185, 129);
                            } else {
                                comparison = "符合计划";
                                color = new DeviceRgb(59, 130, 246);
                            }
                            
                            Paragraph budgetComparison = new Paragraph(
                                String.format("计划预算：¥%.2f/天 | ", dailyPlan) + comparison
                            )
                                    .setFont(font)
                                    .setFontSize(10)
                                    .setFontColor(color)
                                    .setTextAlignment(TextAlignment.RIGHT)
                                    .setMarginBottom(10);
                            document.add(budgetComparison);
                        }
                    }
                }
            }
        }

        // 添加页脚
        System.out.println("[PDF] 开始添加footer...");
        Paragraph footer = new Paragraph("由途点儿啥生成 - " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")))
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(30);
        document.add(footer);
        System.out.println("[PDF] Footer添加成功");
        
        System.out.println("[PDF] Footer添加完成，开始查询任务...");
        
        // 添加任务分工信息
        try {
            java.util.List<ProjectTask> tasks = taskRepository.findByProjectIdOrderByCreatedTimeDesc(projectId);
            System.out.println("[PDF] 查询到任务数量: " + tasks.size());
        if (!tasks.isEmpty()) {
            // 添加分页符
            document.add(new com.itextpdf.layout.element.AreaBreak());
            
            Paragraph taskTitle = new Paragraph("任务分工")
                    .setFont(font)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(139, 92, 246))
                    .setMarginBottom(20);
            document.add(taskTitle);
            
            Table taskTable = new Table(UnitValue.createPercentArray(new float[]{50, 30, 20}))
                    .useAllAvailableWidth()
                    .setMarginBottom(10);
            
            // 表头
            taskTable.addCell(new Cell()
                    .add(new Paragraph("任务名称").setFont(font).setFontSize(10).setBold())
                    .setBackgroundColor(new DeviceRgb(139, 92, 246))
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8));
            taskTable.addCell(new Cell()
                    .add(new Paragraph("负责人").setFont(font).setFontSize(10).setBold())
                    .setBackgroundColor(new DeviceRgb(139, 92, 246))
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8));
            taskTable.addCell(new Cell()
                    .add(new Paragraph("状态").setFont(font).setFontSize(10).setBold())
                    .setBackgroundColor(new DeviceRgb(139, 92, 246))
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8));
            
            // 任务列表
            for (ProjectTask task : tasks) {
                System.out.println("[PDF] 添加任务: " + task.getTaskName() + ", 负责人ID: " + task.getAssigneeId());
                taskTable.addCell(new Cell()
                        .add(new Paragraph(task.getTaskName()).setFont(font).setFontSize(10))
                        .setPadding(8));
                
                // 获取负责人姓名
                String assigneeName = "-";
                try {
                    User assignee = userRepository.findById(task.getAssigneeId()).orElse(null);
                    if (assignee != null) {
                        assigneeName = assignee.getUsername();
                    }
                } catch (Exception e) {}
                
                taskTable.addCell(new Cell()
                        .add(new Paragraph(assigneeName).setFont(font).setFontSize(10))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(8));
                
                // 状态
                String statusText;
                DeviceRgb statusColor;
                switch (task.getStatus()) {
                    case "PENDING":
                        statusText = "待处理";
                        statusColor = new DeviceRgb(156, 163, 175);
                        break;
                    case "IN_PROGRESS":
                        statusText = "进行中";
                        statusColor = new DeviceRgb(59, 130, 246);
                        break;
                    case "COMPLETED":
                        statusText = "已完成";
                        statusColor = new DeviceRgb(16, 185, 129);
                        break;
                    default:
                        statusText = task.getStatus();
                        statusColor = new DeviceRgb(0, 0, 0);
                }
                
                taskTable.addCell(new Cell()
                        .add(new Paragraph(statusText).setFont(font).setFontSize(10).setBold())
                        .setFontColor(statusColor)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(8));
            }
            
            document.add(taskTable);
        }
        } catch (Exception e) {
            System.out.println("[PDF] 任务分工添加失败: " + e.getMessage());
            e.printStackTrace();
        }

        document.close();
        return baos.toByteArray();
    }
    
    /**
     * 添加详细交通信息单元格
     */
    private void addDetailedTransportCell(Table table, PdfFont font, JsonObject transport) {
        // 获取摘要信息
        String summary = buildTransportSummary(transport);
        
        // 获取详细步骤
        java.util.List<String> steps = buildTransportSteps(transport);
        
        // 构建内容
        StringBuilder content = new StringBuilder();
        content.append(">>> ").append(summary);
        
        // 如果有详细步骤，添加到内容中
        if (!steps.isEmpty()) {
            content.append("\n");
            for (int i = 0; i < steps.size(); i++) {
                content.append("   ").append(i + 1).append(". ").append(steps.get(i));
                if (i < steps.size() - 1) {
                    content.append("\n");
                }
            }
        }
        
        Cell transportCell = new Cell(1, 2)
                .add(new Paragraph(content.toString()).setFont(font).setFontSize(9))
                .setBackgroundColor(new DeviceRgb(240, 253, 244))
                .setFontColor(new DeviceRgb(22, 163, 74))
                .setPadding(6);
        table.addCell(transportCell);
    }
    
    /**
     * 构建交通信息字符串（摘要）
     */
    private String buildTransportSummary(JsonObject transport) {
        StringBuilder sb = new StringBuilder();
        
        // 交通方式
        if (transport.has("method") && !transport.get("method").isJsonNull()) {
            sb.append(transport.get("method").getAsString());
        } else if (transport.has("recommendedMethod") && !transport.get("recommendedMethod").isJsonNull()) {
            sb.append(transport.get("recommendedMethod").getAsString());
        } else {
            sb.append("交通");
        }
        
        // 时长
        if (transport.has("durationText") && !transport.get("durationText").isJsonNull()) {
            sb.append(" | ").append(transport.get("durationText").getAsString());
        } else if (transport.has("duration") && !transport.get("duration").isJsonNull()) {
            int duration = transport.get("duration").getAsInt();
            int minutes = (int) Math.ceil(duration / 60.0);
            sb.append(" | 约").append(minutes).append("分钟");
        }
        
        // 距离
        if (transport.has("distanceText") && !transport.get("distanceText").isJsonNull()) {
            sb.append(" | ").append(transport.get("distanceText").getAsString());
        } else if (transport.has("distance") && !transport.get("distance").isJsonNull()) {
            double distance = transport.get("distance").getAsDouble();
            if (distance >= 1000) {
                sb.append(" | ").append(String.format("%.1fkm", distance / 1000));
            } else {
                sb.append(" | ").append((int) distance).append("m");
            }
        }
        
        // 费用
        if (transport.has("costText") && !transport.get("costText").isJsonNull()) {
            String costText = transport.get("costText").getAsString();
            if (!costText.equals("免费") && !costText.equals("0")) {
                sb.append(" | ").append(costText);
            }
        }
        
        // 目的地
        if (transport.has("toName") && !transport.get("toName").isJsonNull()) {
            sb.append(" -> ").append(transport.get("toName").getAsString());
        }
        
        return sb.toString();
    }
    
    /**
     * 构建详细交通步骤列表
     */
    private java.util.List<String> buildTransportSteps(JsonObject transport) {
        java.util.List<String> stepsList = new java.util.ArrayList<>();
        
        if (transport.has("steps") && transport.get("steps").isJsonArray()) {
            JsonArray steps = transport.getAsJsonArray("steps");
            for (int i = 0; i < steps.size(); i++) {
                JsonObject step = steps.get(i).getAsJsonObject();
                String type = step.has("type") ? step.get("type").getAsString() : "";
                String description = step.has("description") ? step.get("description").getAsString() : "";
                
                // 过滤极短距离的步行（<50米）
                if (type.equals("walk") && step.has("distance")) {
                    double distance = step.get("distance").getAsDouble();
                    if (distance < 50) continue;
                }
                
                // 根据类型添加前缀
                String prefix;
                switch (type) {
                    case "walk": prefix = "[步行] "; break;
                    case "subway": prefix = "[地铁] "; break;
                    case "bus": prefix = "[公交] "; break;
                    default: prefix = "";
                }
                
                // 清理描述中的换行符
                description = description.replace("\n", " ");
                
                stepsList.add(prefix + description);
            }
        }
        
        return stepsList;
    }
}
