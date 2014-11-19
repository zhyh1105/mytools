package com.puhui.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Ocr {
    public static String getCodeFromImage(File image) throws Exception {
        return getCodeFromImage(image, null, false);
    }

    public static String getCodeFromImage(File image, String lang, boolean binanyImage) throws Exception {
        if (binanyImage) {
            PreProcessImage.binanyImage(image);
        }
        ProcessBuilder processBuilder = new ProcessBuilder("D:\\apps\\Tesseract-OCR\\tesseract", image.getName(),
                "out", "-psm", "7", "-l", lang == null ? "eng" : lang);
        processBuilder.directory(new File(image.getParent()));
        Process process = processBuilder.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (br != null) {
                br.close();
            }
        }
        BufferedReader fileReader = new BufferedReader(new FileReader(new File(image.getParent(), "out.txt")));
        String code = null;
        try {
            code = fileReader.readLine();
        } finally {
            fileReader.close();
        }
        return code;
    }

    public static void main(String[] args) throws IOException {
    }
}
