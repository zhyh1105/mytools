package com.puhui.crawler.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

import com.puhui.crawler.util.HttpUtils;

public class EncodingTest {
    public static void main(String[] args) throws IOException {
        String s = "中国人";
        System.out.printf("%s,%d", s, s.length());
        System.out.printf("%s,%d", s, s.length());
        System.out.println(FileUtils.readFileToString(
                new File("D:/tmp/10086/bills/13552355914/gsm/1416980773005.html"), Charset.forName(HttpUtils.UTF_8)));
    }
}
