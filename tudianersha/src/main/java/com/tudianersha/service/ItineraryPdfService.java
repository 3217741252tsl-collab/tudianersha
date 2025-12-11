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
import com.tudianersha.repository.AiGeneratedRouteRepository;
import com.tudianersha.repository.TravelProjectRepository;
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
                    
                    // 创建表格（添加预算列）
                    Table table = new Table(UnitValue.createPercentArray(new float[]{15, 60, 25}))
                            .useAllAvailableWidth()
                            .setMarginBottom(10);
                    
                    // 表头
                    Cell headerTime = new Cell()
                            .add(new Paragraph("时间").setFont(font).setFontSize(10).setBold())
                            .setBackgroundColor(new DeviceRgb(67, 126, 234))
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(8);
                    table.addCell(headerTime);
                    
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
                    
                    // 计算当日总预算
                    double dayTotalBudget = 0.0;

                    for (int actIdx = 0; actIdx < activities.size(); actIdx++) {
                        JsonElement activityElement = activities.get(actIdx);
                        String activity = activityElement.getAsString();
                        
                        // 提取时间和活动内容
                        String time = "";
                        String content = activity;
                        int colonIndex = activity.indexOf(":");
                        if (colonIndex > 0 && colonIndex < 10) {
                            time = activity.substring(0, colonIndex).trim();
                            content = activity.substring(colonIndex + 1).trim();
                        }
                        
                        // 获取预算
                        String budgetKey = (i + 1) + "-" + actIdx;
                        Double budget = budgets.getOrDefault(budgetKey, 0.0);
                        if (budget > 0) {
                            dayTotalBudget += budget;
                        }

                        // 时间单元格
                        Cell timeCell = new Cell()
                                .add(new Paragraph(time).setFont(font).setFontSize(10))
                                .setBackgroundColor(new DeviceRgb(240, 242, 245))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setPadding(8);
                        table.addCell(timeCell);

                        // 活动单元格
                        Cell contentCell = new Cell()
                                .add(new Paragraph(content).setFont(font).setFontSize(10))
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
        Paragraph footer = new Paragraph("由途点儿啥生成 - " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")))
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(30);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }
}
