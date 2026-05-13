package com.examme.examme.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class FileProcessingUtil {

    public String extractTextFromFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is required");
        }

        if (filename.endsWith(".pdf")) {
            return extractFromPdf(file.getInputStream());
        } else if (filename.endsWith(".docx")) {
            return extractFromDocx(file.getInputStream());
        } else if (filename.endsWith(".xlsx")) {
            return extractFromExcel(file.getInputStream());
        } else if (filename.endsWith(".txt")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Supported formats: .pdf, .docx, .xlsx");
        }
    }

    private String extractFromPdf(InputStream inputStream) throws IOException {
        StringBuilder text = new StringBuilder();
        PDDocument document = PDDocument.load(inputStream);
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            text.append(stripper.getText(document));
        } finally {
            document.close();
        }
        return text.toString();
    }

    private String extractFromDocx(InputStream inputStream) throws IOException {
        StringBuilder text = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
        }
        return text.toString();
    }

    private String extractFromExcel(InputStream inputStream) throws IOException {
        StringBuilder text = new StringBuilder();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        text.append(cell.toString()).append("\t");
                    }
                    text.append("\n");
                }
            }
        }
        return text.toString();
    }

}
