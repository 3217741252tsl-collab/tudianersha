package com.tudianersha.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Component
public class DocxReader {
    
    public String readDocxFile(String filePath) {
        StringBuilder content = new StringBuilder();
        
        try {
            // Using absolute path
            FileInputStream fis = new FileInputStream(filePath);
            XWPFDocument document = new XWPFDocument(fis);
            
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            for (XWPFParagraph paragraph : paragraphs) {
                content.append(paragraph.getText()).append("\n");
            }
            
            document.close();
            fis.close();
            
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading DOCX file: " + e.getMessage();
        }
        
        return content.toString();
    }
    
    public void printDocxContent(String filePath) {
        String content = readDocxFile(filePath);
        System.out.println(content);
    }
}