package com.puhui.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Jpg2Tiff {
    public static String converImage2Tif(File image) throws IOException {
        String name = image.getName();
        ProcessBuilder processBuilder = new ProcessBuilder("D:\\apps\\ImageMagick\\convert", name, name.substring(0,
                name.lastIndexOf(".")) + ".tiff");
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
        return null;
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i <= 9; i++) {
            File file = new File("D:/tmp/images2/" + i + ".jpg");
            // file.renameTo(new File(file.getParent() ,(i + 200) + ".jpg"));
            // converImage2Tif(file);
            // System.out.printf("%d\t%s\n", i, Ocr.getCodeFromImage(file,
            // "rhzx"));
            // PreProcessImage.binanyImage(file);
            Rgb.removeRed(file);
            // PreProcessImage.grayImage(file);
            System.out.printf("%d\t%s\n", i, Ocr.getCodeFromImage(file, "eng", false));
        }

        // for(int i = 0; i <= 299; i++){
        // File file = new File("D:/tmp/codes_binany/" + i + ".jpg");
        // converImage2Tif(file);
        // }
    }
}
