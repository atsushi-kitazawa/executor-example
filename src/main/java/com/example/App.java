package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App {

    private static CompletionService<Integer> executorCompletionService;

    public static void main(String[] args) {
        doMain();
    }

    private static void doMain() {
        execService();
    }

    private static void execService() {
        ExecutorService executorService = new ThreadPoolExecutor(4, 4,
                0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    private volatile int _count = 0;

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "Worker-" + _count++);
                    }
                }, new ThreadPoolExecutor.AbortPolicy());
        executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
        futures.add(executorCompletionService.submit(new MultiplyingTask("Task 1", 10, 20, 2 * 1000)));
        futures.add(executorCompletionService.submit(new MultiplyingTask("Task 2", 20, 30, 4 * 1000)));
        futures.add(executorCompletionService.submit(new MultiplyingTask("Task 3", 30, 40, 3 * 1000)));
        futures.add(executorCompletionService.submit(new MultiplyingTask("Task 4", 40, 50, 1 * 1000)));

        for (int i = 0; i < futures.size(); i++) {
            try {
                if (i == 1) {
                    executorCompletionService = new ExecutorCompletionService<>(executorService);
                    List<Future<Integer>> futuresSub = new ArrayList<Future<Integer>>();
                    futuresSub
                            .add(executorCompletionService.submit(new MultiplyingTask("Task 5", 50, 60, 5 * 1000)));
                }
                Future<Integer> f = executorCompletionService.take();
                Integer result = f.get();
                System.out.println("Result: " + result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }
}
