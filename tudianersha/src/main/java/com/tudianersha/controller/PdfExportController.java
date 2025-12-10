package com.tudianersha.controller;

import com.tudianersha.service.ItineraryPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin
public class PdfExportController {

    @Autowired
    private ItineraryPdfService pdfService;

    /**
     * 生成行程PDF文件
     * @param projectId 项目ID
     * @return PDF文件的字节流
     */
    @GetMapping("/itinerary/{projectId}")
    public ResponseEntity<byte[]> generateItineraryPdf(@PathVariable Long projectId) {
        try {
            System.out.println("[PDF Export] 开始生成PDF for project: " + projectId);
            
            byte[] pdfBytes = pdfService.generateItineraryPdf(projectId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "travel-itinerary-" + projectId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            System.out.println("[PDF Export] PDF生成成功，大小: " + pdfBytes.length + " bytes");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("[PDF Export] 生成PDF失败: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
