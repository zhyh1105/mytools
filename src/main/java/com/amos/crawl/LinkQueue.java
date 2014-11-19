package com.amos.crawl;

import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Created by lixin on 14-11-9.
 *
 *  创建一个带偏好的队列
 *
 */
public class LinkQueue {


    //已经访问的队列
    private static Set visitedUrl = new HashSet();
    //未访问的队列
    private static Queue unVisitedUrl = new PriorityQueue();


    //获得URL队列
    public static Queue getUnVisitedUrl() {
        return unVisitedUrl;
    }

    public static Set getVisitedUrl() {
        return visitedUrl;
    }
    //添加到访问过的URL队列中
    public static void addVisitedUrl(String url) {
        visitedUrl.add(url);
    }

    //删除已经访问过的URL
    public static void removeVisitedUrl(String url){
        visitedUrl.remove(url);
    }
    //未访问的URL出队列
    public static Object unVisitedUrlDeQueue(){
        return unVisitedUrl.poll();
    }
    //保证每个URL只被访问一次,url不能为空,同时已经访问的URL队列中不能包含该url,而且因为已经出队列了所未访问的URL队列中也不能包含该url
    public static void addUnvisitedUrl(String url){
        if(url!=null&&!url.trim().equals("")&&!visitedUrl.contains(url)&&!unVisitedUrl.contains(url))
        unVisitedUrl.add(url);
    }
    //获得已经访问过的URL的数量
    public static int getVisitedUrlNum(){
        return visitedUrl.size();
    }

    //判断未访问的URL队列中是否为空
    public static boolean isUnvisitedUrlsEmpty(){
        return unVisitedUrl.isEmpty();
    }



}
