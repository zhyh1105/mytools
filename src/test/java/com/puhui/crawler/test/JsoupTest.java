package com.puhui.crawler.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.puhui.crawler.util.HttpUtils;

public class JsoupTest {

    public static void main(String[] args) throws Exception {
        Document document = Jsoup.parse(new File("D:/tmp/10086/bills/1416884661262.html"), HttpUtils.GBK);
        Elements elements = document.select("#loginAgain div form");
        if (!elements.isEmpty()) {
            Element loginAgain = elements.get(0);
            Elements forms = loginAgain.select("div > form");
            if (!forms.isEmpty()) {
                Element form = forms.get(0);
                String url = form.attr("action");
                Elements inputs = form.select("input[type=hidden]");
                Map<String, Object> params = new HashMap<>();
                for (int i = 0; i < inputs.size(); i++) {
                    params.put(inputs.get(i).attr("name"), inputs.get(i).attr("value"));
                }
            }
        }
    }
}
