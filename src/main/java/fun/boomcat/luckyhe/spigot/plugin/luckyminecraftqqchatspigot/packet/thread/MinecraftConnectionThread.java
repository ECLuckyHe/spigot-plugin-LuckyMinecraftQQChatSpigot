package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketReceiveUtil;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class MinecraftConnectionThread extends Thread {
    private final String serverName = ConfigOperation.getServerName();

    private final Queue<Packet> sendQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Packet> receiveQueue = new ConcurrentLinkedQueue<>();

    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final Logger logger;

    private boolean isConnected = true;
    private final CountDownLatch cdl = new CountDownLatch(3);

    public void logInfo(String threadName, String info) {
        logger.info("[" + threadName + "] " + info);
    }

    @Override
    public void run() {
        Thread sendThread = new Thread(() -> {
//            此内容与bot端的发送线程差别不大
            String threadName = "发送线程";
            logInfo(threadName, "线程启动");

            while (isConnected) {
                try {
                    Packet packet = sendQueue.poll();
                    if (packet != null) {
                        outputStream.write(packet.getBytes());
                        outputStream.flush();

                        if (packet.getId().getValue() == 0xF0) {
                            logInfo(threadName, "发送关闭包，内容" + new VarIntString(packet.getData()).getContent());
                            isConnected  = false;
                            socket.close();
                        }
                    }
                } catch (Exception e) {
                    isConnected = false;
                    e.printStackTrace();
                    logInfo(threadName, "线程出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logInfo(threadName, "Socket关闭失败");
                    }
                }
            }

            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

        Thread receiveThread = new Thread(() -> {
            String threadName = "接收线程";

            while (isConnected) {
                try {
                    if (inputStream.available() > 0) {
                        Packet packet = ConnectionPacketReceiveUtil.getPacket(inputStream);
                        receiveQueue.add(packet);
                    }
                } catch (Exception e) {
                    isConnected = false;
                    e.printStackTrace();
                    logInfo(threadName, "线程出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logInfo(threadName, "Socket关闭失败");
                    }
                }
            }

            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

        Thread receiveHandlerThread = new Thread(() -> {
            String threadName = "接收处理";
            logInfo(threadName, "线程启动");

            while (isConnected) {
                try {
                    Packet packet = receiveQueue.poll();
                    if (packet != null) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isConnected = false;
                    logInfo(threadName, "出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logInfo(threadName, "Socket关闭失败");
                    }
                }
            }

            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

//        启动线程
        sendThread.start();
        receiveThread.start();
        receiveHandlerThread.start();

//        等待所有线程结束
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logInfo("总线程", "所有线程均已停止工作");

        while (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logInfo("Socket", "已确认关闭");
    }

    public MinecraftConnectionThread(Socket socket, Logger logger) throws IOException {
        this.socket = socket;
        this.logger = logger;

        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.outputStream = new BufferedOutputStream(socket.getOutputStream());
    }
}
