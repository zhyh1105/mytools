package com.amos.tool;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lixin on 14-7-2.
 */
public class DownUtil {

    private String path;//定义下载资源的路径
    private String targetFile;//定义所下载的文件的保存的位置
    private int threadNum;//线程数
    private DownThread[] threads;//定义下载的线程对象
    private int fileSize;//定义下载的文件的总大小

    public DownUtil(String path, String targetFile, int threadNum) {
        this.path = path;
        this.targetFile = targetFile;
        this.threadNum = threadNum;
        //初始化threads数组
        threads = new DownThread[threadNum];
    }


    public void download() throws Exception {
        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5 * 1000);//设置一个连接超时时间,5秒
        connection.setRequestMethod("GET");
        connection.setRequestProperty(
                "Accept",
                "image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
                        + "application/x-shockwave-flash, application/xaml+xml, "
                        + "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
                        + "application/x-ms-application, application/vnd.ms-excel, "
                        + "application/vnd.ms-powerpoint, application/msword, */*"
        );
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Charset", "UTF-8");

        //get filesize
        fileSize = connection.getContentLength();
        //close connect
        connection.disconnect();

        int currentPartSize = fileSize / threadNum;
        RandomAccessFile file = new RandomAccessFile(targetFile, "rw");
        file.setLength(fileSize);//set fileSize
        file.close();

        for (int i = 0; i < threadNum; i++) {
            int startPos = i * currentPartSize;//计算每个线程的开始位置
            //每个线程使用一个RandomAccessFile进行下载
            RandomAccessFile currentPart = new RandomAccessFile(targetFile, "rw");
            //定位该线程的下载位置
            currentPart.seek(startPos);
            //创建下载线程
            threads[i] = new DownThread(startPos, currentPartSize, currentPart);
            //启动下载线程
            threads[i].start();
        }
    }


    // 获取下载的完成百分比
    public double getCompleteRate() {
        // 统计多条线程已经下载的总大小
        int sumSize = 0;
        for (int i = 0; i < threadNum; i++) {
            sumSize += threads[i].length;
        }
        // 返回已经完成的百分比
        return sumSize * 1.0 / fileSize;
    }


    private class DownThread extends Thread {

        // 当前线程的下载位置
        private int startPos;
        // 定义当前线程负责下载的文件大小
        private int currentPartSize;
        // 当前线程需要下载的文件块
        private RandomAccessFile currentPart;
        // 定义已经该线程已下载的字节数
        public int length;

        public DownThread(int startPos, int currentPartSize,
                          RandomAccessFile currentPart) {
            this.startPos = startPos;
            this.currentPartSize = currentPartSize;
            this.currentPart = currentPart;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5 * 1000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty(
                        "Accept",
                        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
                                + "application/x-shockwave-flash, application/xaml+xml, "
                                + "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
                                + "application/x-ms-application, application/vnd.ms-excel, "
                                + "application/vnd.ms-powerpoint, application/msword, */*"
                );
                conn.setRequestProperty("Accept-Language", "zh-CN");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("Connection", "Keep-Alive");

                InputStream inputStream = conn.getInputStream();

                //跳过startPos那一部分内容,表明该线程仅下载属于它自己的那一部分
                inputStream.skip(this.startPos);

                byte[] bytes = new byte[1024];
                int hasRead = 0;
                while (length < currentPartSize && (hasRead = inputStream.read(bytes)) != -1) {
                    currentPart.write(bytes, 0, hasRead);
                    length += hasRead;
                }
                currentPart.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }


//    private class DownThread extends Thread
//    {
//        // 当前线程的下载位置
//        private int startPos;
//        // 定义当前线程负责下载的文件大小
//        private int currentPartSize;
//        // 当前线程需要下载的文件块
//        private RandomAccessFile currentPart;
//        // 定义已经该线程已下载的字节数
//        public int length;
//
//        public DownThread(int startPos, int currentPartSize,
//                          RandomAccessFile currentPart)
//        {
//            this.startPos = startPos;
//            this.currentPartSize = currentPartSize;
//            this.currentPart = currentPart;
//        }
//
//        @Override
//        public void run()
//        {
//            try
//            {
//                URL url = new URL(path);
//                HttpURLConnection conn = (HttpURLConnection)url
//                        .openConnection();
//                conn.setConnectTimeout(5 * 1000);
//                conn.setRequestMethod("GET");
//                conn.setRequestProperty(
//                        "Accept",
//                        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
//                                + "application/x-shockwave-flash, application/xaml+xml, "
//                                + "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
//                                + "application/x-ms-application, application/vnd.ms-excel, "
//                                + "application/vnd.ms-powerpoint, application/msword, */*");
//                conn.setRequestProperty("Accept-Language", "zh-CN");
//                conn.setRequestProperty("Charset", "UTF-8");
//                InputStream inStream = conn.getInputStream();
//                // 跳过startPos个字节，表明该线程只下载自己负责哪部分文件。
//                inStream.skip(this.startPos);
//                byte[] buffer = new byte[1024];
//                int hasRead = 0;
//                // 读取网络数据，并写入本地文件
//                while (length < currentPartSize
//                        && (hasRead = inStream.read(buffer)) != -1)
//                {
//                    currentPart.write(buffer, 0, hasRead);
//                    // 累计该线程下载的总大小
//                    length += hasRead;
//                }
//                currentPart.close();
//                inStream.close();
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        }
//    }


}
