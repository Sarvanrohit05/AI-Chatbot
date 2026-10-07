package com.in.rohit.spring_ai_agent.service;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/*
 * WHY:
 * Uploaded PDF se readable text extract karne ke liye.
 */
@Service
public class PdfTextExtractorService {

    public String extractText(MultipartFile file)
            throws IOException {

        byte[] fileBytes = file.getBytes();

        try (PDDocument document =
                     Loader.loadPDF(fileBytes)) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            return stripper.getText(document);
        }
    }
}