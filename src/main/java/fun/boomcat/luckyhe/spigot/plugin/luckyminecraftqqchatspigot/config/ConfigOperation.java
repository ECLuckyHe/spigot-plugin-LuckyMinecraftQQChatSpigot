package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigOperation {
    private static FileConfiguration config;

    public static void initFileConfiguration(FileConfiguration c) {
        config = c;
    }

    public static String getBotHost() {
        return config.getString("bot.host");
    }

    public static Integer getBotPort() {
        return config.getInt("bot.port");
    }

    public static Long getBotSessionId() {
        return config.getLong("bot.sessionId");
    }

    public static Integer getBotRetryTimes() {
        return config.getInt("bot.retryTimes");
    }

    public static String getServerName() {
        return config.getString("serverName");
    }

    public static String getFormatFromBot() {
        return config.getString("format.fromBot");
    }

    public static String getFormatFromBotMsgAtMe() {
        return config.getString("format.fromBotMsg.atMe");
    }

    public static String getFormatFromBotMsgAt() {
        return config.getString("format.fromBotMsg.at");
    }

    public static String getFormatFromBotMsgPic() {
        return config.getString("format.fromBotMsg.pic");
    }

    public static String getFormatFromBotMsgAtAll() {
        return config.getString("format.fromBotMsg.atAll");
    }

    public static String getFormatFromBotMsgQuoteReplyDisplay() {
        return config.getString("format.fromBotMsg.quoteReply.display");
    }

    public static String getFormatFromBotMsgQuoteReplyHover() {
        return config.getString("format.fromBotMsg.quoteReply.hover");
    }

    public static String getFormatFromBotMsgAnimeFace() {
        return config.getString("format.fromBotMsg.animeFace");
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

    public static String getInfoOnEnable() {
        return config.getString("info.onEnable");
    }

    public static String getInfoOnDisable() {
        return config.getString("info.onDisable");
    }

    public static String getInfoOnConnected() {
        return config.getString("info.onConnected");
    }

    public static String getInfoOnPingFail() {
        return config.getString("info.onPingFail");
    }

    public static String getInfoOnConnectionDisconnect() {
        return config.getString("info.onConnectionDisconnect");
    }

    public static String getInfoOnRequestError() {
        return config.getString("info.onRequestError");
    }

    public static String getInfoOnBotRequestClose() {
        return config.getString("info.onBotRequestClose");
    }

    public static List<String> getOnlinePlayersCommands() {
        return config.getStringList("onlinePlayers.command");
    }

    public static String getOnlinePlayersResponseFormat() {
        return config.getString("onlinePlayers.response.format");
    }

    public static String getOnlinePlayersResponseSeparator() {
        return config.getString("onlinePlayers.response.separator");
    }

    public static String getRconCommandPrefix() {
        return config.getString("rconCommand.prefix");
    }

    public static String getRconCommandUserPrefix() {
        return config.getString("rconCommand.userPrefix");
    }

    public static String getRconCommandUserBindPrefix() {
        return config.getString("rconCommand.userBindPrefix");
    }

    public static List<String> getRconCommandGetUserCommand() {
        return config.getStringList("rconCommand.getUserCommand");
    }

    public static Boolean getRconCommandEnabled() {
        return  config.getBoolean("rconCommand.enabled");
    }

    public static Integer getRconCommandPort() {
        return config.getInt("rconCommand.port");
    }

    public static String getRconCommandResultFormat() {
        return config.getString("rconCommand.resultFormat");
    }

    public static String getRconCommandPassword() {
        return config.getString("rconCommand.password");
    }

    public static String getAnnouncementFormat() {
        return config.getString("announcementFormat");
    }

    public static Boolean getWhitelistEnabled() {
        return config.getBoolean("whitelist.enabled");
    }

    public static String getWhitelistCorrectMessage() {
        return config.getString("whitelist.correctMessage");
    }

    public static String getWhitelistTryMessage() {
        return config.getString("whitelist.tryMessage");
    }
}
