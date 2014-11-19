package com.puhui.crawler;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Rgb {

    public static void main(String[] args) throws Exception {
    }

    public static void removeRed(File image) {
        try {
            BufferedImage bi = ImageIO.read(image);
            int w = bi.getWidth();
            int h = bi.getHeight();
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int rgb = bi.getRGB(x, y);
                    Color color = new Color(rgb);
                    if (color.getRed() > 100) {
                        // System.out.printf("(%d,%d,%d)", color.getRed(),
                        // color.getGreen(), color.getBlue());
                        color = new Color(255, 255, 255);
                        bi.setRGB(x, y, color.getRGB());
                        // System.out.printf("-(%d,%d,%d)\t", color.getRed(),
                        // color.getGreen(), color.getBlue());
                    }
                }
                // System.out.println();
            }
            ImageIO.write(bi, "jpg", image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
