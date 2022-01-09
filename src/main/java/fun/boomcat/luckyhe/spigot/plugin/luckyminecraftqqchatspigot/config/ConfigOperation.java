package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigOperation {
    private static FileConfiguration config;

    public static void initFileConfiguration(FileConfiguration c) {
        config = c;
    }

    public static String getBotHost() {
        return config.getString("bot.host");
    }

    public static int getBotPort() {
        return config.getInt("bot.port");
    }

    public static long getBotSessionId() {
        return config.getLong("bot.sessionId");
    }

    public static int getBotRetryTimes() {
        return config.getInt("bot.retryTimes");
    }

    public static String getServerName() {
        return config.getString("serverName");
    }

    public static String getFormatFromBot() {
        return config.getString("format.fromBot");
    }

    public static String getFormatJoin() {
        return config.getString("format.join");
    }

    public static String getFormatQuit() {
        return config.getString("format.quit");
    }

    public static String getFormatMessage() {
        return config.getString("format.message");
    }

    public static String getFormatDeath() {
        return config.getString("format.death");
    }

    public static String getFormatKick() {
        return config.getString("format.kick");
    }
}
