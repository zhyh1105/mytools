package com.amos.crawl;

/**
 * Created by lixin on 14-11-10.
 */
public interface LinkFilter {

    public boolean accept(String url);

}
