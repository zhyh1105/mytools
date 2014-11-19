package com.puhui.crawler;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class PreProcessImage {
	public static void process(File image) throws Exception{
		grayImage(image);
		binanyImage(image);
	}
	public static void grayImage(File src) throws Exception {
		BufferedImage image = ImageIO.read(src);

		BufferedImage dest = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < dest.getWidth(); i++) {
			for (int j = 0; j < dest.getHeight(); j++) {
				int rgb = image.getRGB(i, j);
				dest.setRGB(i, j, rgb);
			}
		}
		ImageIO.write(dest, "jpg", src);
	}

	public static void binanyImage(File src) throws Exception {
		BufferedImage image = ImageIO.read(src);
		BufferedImage dest = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
		for (int i = 0; i < dest.getWidth(); i++) {
			for (int j = 0; j < dest.getHeight(); j++) {
				int rgb = image.getRGB(i, j);
				dest.setRGB(i, j, rgb);
			}
		}
		ImageIO.write(dest, "jpg", src);
	}
}
