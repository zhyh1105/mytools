package com.puhui.crawler.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorServiceTest {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        final List<Future<Boolean>> fs = new ArrayList<>();

        fs.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Thread.sleep(1000);
                System.out.println("1");
                Thread.sleep(1000);
                return true;
            }
        }));
        fs.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Thread.sleep(1000);
                System.out.println("2");
                return true;
            }
        }));
        fs.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Thread.sleep(1000);
                System.out.println("3");
                return true;
            }
        }));
        fs.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Thread.sleep(1000);
                System.out.println("4");
                return true;
            }
        }));
        fs.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Thread.sleep(1000);
                System.out.println("5");
                return true;
            }
        }));
        EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (Future<Boolean> f : fs) {
                    f.get();
                }
                System.out.println("6");
                return null;
            }
        });
    }
}
