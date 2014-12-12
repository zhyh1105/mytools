package com.amos.crawl;

import java.util.LinkedList;

/**
 * Created by lixin on 14-7-9.
 */
public class Queue {

    //使用链表实现队列
    private LinkedList queueList = new LinkedList();


    //入队列
    public void enQueue(Object object) {
        queueList.addLast(object);
    }

    //出队列
    public Object deQueue() {
        return queueList.removeFirst();
    }

    //判断队列是否为空
    public boolean isQueueEmpty() {
        return queueList.isEmpty();
    }

    //判断队列是否包含ject元素..
    public boolean contains(Object object) {
        return queueList.contains(object);
    }

    //判断队列是否为空
    public boolean empty() {
        return queueList.isEmpty();
    }

}
