package com.puhui.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessUtils {
    public static void main(String[] args) {
        killDllhost();
    }

    public static void killDllhost() {
        killProcess("dllhost.exe");
    }

    public static void killProcess(String name) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("taskkill ", "/IM", name);
            // processBuilder.directory(new File(image.getParent()));
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
        } catch (Exception e) {
        }
    }
}
