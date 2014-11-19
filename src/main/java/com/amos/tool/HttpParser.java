package com.amos.tool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by lixin on 14-7-6.
 */
public class HttpParser {



    public static String getValueFromInputByName(String html,String name){

        Document doc = Jsoup.parse(html);

        String inputValue = doc.select("input[name=" + name + "]").first().attr("value").trim();

        return inputValue;
    }

    /**
     *
     * @param html 源文件
     * @param start 从哪个id,或标签开始
     * @param name input的名称
     * @return
     */
    public static String getValueFromStartInputByName(String html,String start,String name){
        try{
            Document doc = Jsoup.parse(html);

            String inputValue = doc.select(start).select("input[name=" + name + "]").first().attr("value").trim();

            return inputValue;

        }catch (Exception e){
            System.err.println("name:"+name);
            e.printStackTrace();
            return "";
        }

    }

    public  static void main(String args[]){

        String gvfdcname = new HttpParser().getValueFromInputByName("<input type=\"hidden\" id=\"gvfdc\" name=\"gvfdcname\" value=\"10\">", "gvfdcname");

        System.out.println(gvfdcname);

    }
}
