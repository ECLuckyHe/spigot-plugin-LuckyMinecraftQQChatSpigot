package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarInt;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarLong;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ByteUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketReceiveUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.AsyncCaller;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftFontStyleCode;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftMessageUtil;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class MinecraftConnectionThread extends Thread {
    private final String serverName = ConfigOperation.getServerName();
    private final long sessionId = ConfigOperation.getBotSessionId();
    private final String sessionName;
    private final int heartbeatGap;
    private int heartbeatCount = 0;

    private final Queue<Packet> sendQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Packet> receiveQueue = new ConcurrentLinkedQueue<>();

    private final Server server;
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final Logger logger;

    private final String formatFromBot = ConfigOperation.getFormatFromBot();

    private boolean isConnected = true;
    private boolean isStop = false;
    private final CountDownLatch cdl = new CountDownLatch(4);

    private void logInfo(String threadName, String info) {
        logger.info("[" + threadName + "] " + info);
    }

    private void logWarning(String threadName, String warning) {
        logger.info("[" + threadName + "] " + warning);
    }

    public void sendClosePacket(String info) {
        VarInt packetId = new VarInt(0xF0);
        VarIntString string = new VarIntString(info);
        sendQueue.add(new Packet(new VarInt(packetId.getBytesLength() + string.getBytesLength()), packetId, string.getBytes()));
    }

    public void addSendQueue(Packet packet) {
        sendQueue.add(packet);
    }

    public boolean isStop() {
        return isStop;
    }

    @Override
    public void run() {
        AsyncCaller.run(() -> {
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
                    logWarning(threadName, "线程出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logWarning(threadName, "Socket关闭失败");
                    }
                }
            }

            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

        AsyncCaller.run(() -> {
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
                    logWarning(threadName, "线程出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logWarning(threadName, "Socket关闭失败");
                    }
                }
            }

            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

        AsyncCaller.run(() -> {
            String threadName = "接收处理";
            logInfo(threadName, "线程启动");

            while (isConnected) {
                try {
                    Packet packet = receiveQueue.poll();
                    if (packet != null) {
                        switch (packet.getId().getValue()) {
                            case 0x20:
//                                回应心跳包
                                VarLong ping = new VarLong(packet.getData());
                                VarInt pongPacketId = new VarInt(0x20);
                                sendQueue.add(new Packet(
                                        new VarInt(pongPacketId.getBytesLength() + ping.getBytesLength()),
                                        pongPacketId,
                                        ping.getBytes()
                                ));
                                heartbeatCount = 0;
                                break;
                            case 0x21:
//                                收到要求提供玩家在线信息数据的数据包
                                VarLong groupId = new VarLong(packet.getData());

                                VarInt onlinePlayerPacketId = new VarInt(0x21);
                                Collection<? extends Player> onlinePlayerList = MinecraftMessageUtil.getOnlinePlayerList();
                                VarInt onlinePlayers = new VarInt(onlinePlayerList.size());
                                byte[][] onlinePlayerData = new byte[onlinePlayers.getValue()][];

                                int index = 0;
                                for (Player player : onlinePlayerList) {
                                    onlinePlayerData[index] = new VarIntString(player.getName()).getBytes();
                                    index += 1;
                                }

                                byte[] mergeAll = ByteUtil.byteMergeAll(onlinePlayerData);

                                sendQueue.add(new Packet(
                                        new VarInt(onlinePlayerPacketId.getBytesLength() + groupId.getBytesLength() + onlinePlayers.getBytesLength() + mergeAll.length),
                                        onlinePlayerPacketId,
                                        ByteUtil.byteMergeAll(groupId.getBytes(), onlinePlayers.getBytes(), mergeAll)
                                ));
                                break;
                            case 0xF0:
//                                关闭包
                                VarIntString exitMsg = new VarIntString(packet.getData());
                                logInfo(threadName, "对方要求断开连接，原因：" + exitMsg.getContent());

                                MinecraftMessageUtil.sendMinecraftMessage(
                                        MinecraftFontStyleCode.LIGHT_PURPLE + "[LuckyChat] " +
                                                MinecraftFontStyleCode.RED + "服务端要求断开连接，原因：" + exitMsg.getContent()
                                );

                                isConnected = false;
                                socket.close();
                                break;
                            case 0x11:
//                                原封不动发送到服内的消息包
                                VarIntString msg = new VarIntString(packet.getData());
                                MinecraftMessageUtil.sendMinecraftMessage(msg.getContent());
                                break;
                            case 0x10:
//                                来自群内的消息
                                ConnectionPacketReceiveUtil.handleMessageFromBot(formatFromBot, packet, sessionName);
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isConnected = false;
                    logWarning(threadName, "出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logWarning(threadName, "Socket关闭失败");
                    }
                }
            }

            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

        AsyncCaller.run(() -> {
            String threadName = "心跳检测";
            logInfo(threadName, "线程启动");

            while (isConnected) {
                try {
                    heartbeatCount += 1;
                    Thread.sleep(1000L);

//                    如果超过心跳包发送频率+5秒以上，则断开连接
                    if (heartbeatGap + 5 < heartbeatCount) {
                        logWarning(threadName, "已经" + heartbeatCount + "秒未接收到心跳包，开始关闭连接（服务端发送心跳包频率为" + heartbeatGap + "秒）");
                        socket.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    isConnected = false;
                    logWarning(threadName, "出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logWarning(threadName, "Socket关闭失败");
                    }
                }
            }

            cdl.countDown();
            logInfo(threadName, "结束工作");
        });

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
        isStop = true;
    }

    public MinecraftConnectionThread(Server server, Socket socket, Logger logger, String sessionName, int heartbeatGap) throws IOException {
        this.server = server;
        this.socket = socket;
        this.logger = logger;
        this.sessionName = sessionName;
        this.heartbeatGap = heartbeatGap;

        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.outputStream = new BufferedOutputStream(socket.getOutputStream());
    }
}
