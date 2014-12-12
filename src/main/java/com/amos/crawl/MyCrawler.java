package com.amos.crawl;
import java.util.Set;

/**
 * Created by lixin on 14-11-10.
 */
public class MyCrawler {
    /**
     * 使用种子初始化URL队列
     *
     * @param seeds
     */
    private void initCrawlerWithSeeds(String[] seeds) {
        for (int i = 0; i < seeds.length; i++) {
            LinkQueue.addUnvisitedUrl(seeds[i]);
        }
    }

    public void crawling(String[] seeds) {
        //定义过滤器,提取以http://news.fudan.edu.cn/的链接
        LinkFilter filter = new LinkFilter() {
            @Override
            public boolean accept(String url) {
                if (url.startsWith("http://news.fudan.edu.cn")) {
                    return true;
                }
                return false;
            }
        };
        //初始化URL队列
        initCrawlerWithSeeds(seeds);

        int count=0;
        //循环条件:待抓取的链接不为空抓取的网页最多100条
        while (!LinkQueue.isUnvisitedUrlsEmpty() && LinkQueue.getVisitedUrlNum() <= 100) {

            System.out.println("count:"+(++count));

            //附头URL出队列
            String visitURL = (String) LinkQueue.unVisitedUrlDeQueue();
            DownLoadFile downloader = new DownLoadFile();
            //下载网页
            downloader.downloadFile(visitURL);
            //该URL放入怩访问的URL中
            LinkQueue.addVisitedUrl(visitURL);
            //提取出下载网页中的URL
            Set<String> links = HtmlParserTool.extractLinks(visitURL, filter);

            //新的未访问的URL入列
            for (String link : links) {
                System.out.println("link:"+link);
                LinkQueue.addUnvisitedUrl(link);
            }
        }

    }

    public static void main(String args[]) {
        //程序入口
        MyCrawler myCrawler = new MyCrawler();
        myCrawler.crawling(new String[]{"http://news.fudan.edu.cn/news/"});
    }

}
