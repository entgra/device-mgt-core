package io.entgra.device.mgt.core.device.mgt.common.notification.mgt;

import java.util.concurrent.*;

public class NotificationWorker {
    private final BlockingQueue<Notification> taskQueue;
    private final ThreadPoolExecutor executor;
    private boolean isInitialized = false;

    public NotificationWorker() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.executor = new ThreadPoolExecutor(2, 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    private synchronized void startWorker() {
        if (!isInitialized) {
            isInitialized = true;
            System.out.println("Notification Service Worker Thread initialized.");

            executor.submit(() -> {
                try {
                    while (true) {
                        Notification nextTask = taskQueue.take();
                        System.out.println("New task added; processing in a separate thread.");
                        executor.submit(() -> processNotification(nextTask));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Notification processing thread was interrupted, terminating.");
                }
            });
        }
    }

    public synchronized void addNotificationTask(Notification notification) {
        taskQueue.offer(notification);
        startWorker();
    }

    private void processNotification(Notification notification) {
        try {
            System.out.println("Processing task: " + notification);
        } catch (Exception e) {
            System.err.println("Failed to process notification: " + notification + " due to " + e.getMessage());
        }
        //The logic should be included in the service layer it will be moved in the relevant milestone --> SSE through notification service
    }

}
