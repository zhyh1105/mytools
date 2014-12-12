package com.amos.crawl;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lixin on 14-11-10.
 */
public class HtmlParserTool {
    public static Set<String> extractLinks(String url, LinkFilter filter) {
        Set<String> links = new HashSet<String>();

        try {
            Parser parser = new Parser(url);
            parser.setEncoding("GBK");
            //过滤<frame>标签的filter,用来提取frame标签里的src属性
            NodeFilter framFilter = new NodeFilter() {
                @Override
                public boolean accept(Node node) {
                    if (node.getText().contains("frame src=")) {
                        return true;
                    } else {
                        return false;
                    }

                }
            };

            //OrFilter来设置过滤<a>标签和<frame>标签
            OrFilter linkFilter = new OrFilter(new NodeClassFilter(LinkTag.class), framFilter);
            //得到所有经过过滤的标签
            NodeList list = parser.extractAllNodesThatMatch(linkFilter);
            for (int i = 0; i < list.size(); i++) {
                Node tag = list.elementAt(i);
                if (tag instanceof LinkTag) {
                    tag = (LinkTag) tag;
                    String linkURL = ((LinkTag) tag).getLink();

                    //如果符合条件那么将url添加进去
                    if (filter.accept(linkURL)) {
                        links.add(linkURL);
                    }

                } else {//frame 标签
                    //frmae里src属性的链接,如<frame src="test.html" />
                    String frame = tag.getText();
                    int start = frame.indexOf("src=");
                    frame = frame.substring(start);

                    int end = frame.indexOf(" ");
                    if (end == -1) {
                        end = frame.indexOf(">");
                    }
                    String frameUrl = frame.substring(5, end - 1);
                    if (filter.accept(frameUrl)) {
                        links.add(frameUrl);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return links;
    }


}
