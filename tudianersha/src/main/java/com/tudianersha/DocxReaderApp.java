package com.tudianersha;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.*;
import java.util.List;

public class DocxReaderApp {
    
    public static void main(String[] args) {
        // Read the DOCX file and print its content
        String docxFilePath = "E:\\tudianershatest\\数据库设计+系统功能设计.docx";
        System.out.println("Reading DOCX file content:");
        System.out.println("==========================");
        
        try {
            // Using absolute path
            FileInputStream fis = new FileInputStream(docxFilePath);
            XWPFDocument document = new XWPFDocument(fis);
            
            // Extract text content
            System.out.println("TEXT CONTENT:");
            System.out.println("-------------");
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    System.out.println(text);
                }
            }
            
            // Extract tables
            System.out.println("\n\nTABLE CONTENT:");
            System.out.println("--------------");
            List<XWPFTable> tables = document.getTables();
            for (int i = 0; i < tables.size(); i++) {
                System.out.println("Table " + (i + 1) + ":");
                XWPFTable table = tables.get(i);
                for (XWPFTableRow row : table.getRows()) {
                    StringBuilder rowText = new StringBuilder();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String cellText = cell.getText();
                        rowText.append(cellText).append(" | ");
                    }
                    System.out.println(rowText.toString());
                }
                System.out.println();
            }
            
            // Extract images from paragraphs
            System.out.println("\n\nIMAGE CONTENT:");
            System.out.println("--------------");
            int imageCount = 0;
            
            // Check paragraphs for embedded images
            for (XWPFParagraph paragraph : paragraphs) {
                for (XWPFRun run : paragraph.getRuns()) {
                    List<XWPFPicture> pictures = run.getEmbeddedPictures();
                    for (XWPFPicture picture : pictures) {
                        imageCount++;
                        XWPFPictureData pictureData = picture.getPictureData();
                        String imageName = "extracted_image_" + imageCount + "." + pictureData.suggestFileExtension();
                        System.out.println("Found image in paragraph: " + imageName);
                        
                        // Save image to file
                        FileOutputStream fos = new FileOutputStream("E:\\tudianershatest\\" + imageName);
                        fos.write(pictureData.getData());
                        fos.close();
                        
                        System.out.println("Saved image to: E:\\tudianershatest\\" + imageName);
                    }
                }
            }
            
            // Extract all pictures from document
            List<XWPFPictureData> allPictures = document.getAllPictures();
            for (int i = 0; i < allPictures.size(); i++) {
                imageCount++;
                XWPFPictureData picture = allPictures.get(i);
                String imageName = "document_image_" + imageCount + "." + picture.suggestFileExtension();
                System.out.println("Found document image: " + imageName);
                
                // Save image to file
                FileOutputStream fos = new FileOutputStream("E:\\tudianershatest\\" + imageName);
                fos.write(picture.getData());
                fos.close();
                
                System.out.println("Saved image to: E:\\tudianershatest\\" + imageName);
            }
            
            if (imageCount == 0) {
                System.out.println("No images found in the document.");
            }
            
            document.close();
            fis.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error reading DOCX file: " + e.getMessage());
        }
        
        System.out.println("==========================");
        System.out.println("DOCX file reading completed.");
    }
}