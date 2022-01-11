package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AsyncCaller {
    private static ExecutorService pool = Executors.newCachedThreadPool();

    public static void run(Runnable r) {
        pool.execute(r);
    }

    private static class MyFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            return t;
        }
    }
}
