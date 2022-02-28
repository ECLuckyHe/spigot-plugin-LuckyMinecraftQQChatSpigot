package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util;

import java.util.ArrayList;
import java.util.List;

public class UserCommandUtil {
    public static List<String> splitCommand(String command) {
        List<String> res = new ArrayList<>();
        while (!command.equals("")) {
            String[] split = command.split("\\s+");
            res.add(split[0]);
            command = command.substring(split[0].length()).trim();
        }
        return res;
    }

    public static String craftCommand(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append(" ");
        }
        return sb.toString().trim();
    }

    public static List<String> getCommandArgList(String command) {
        List<String> strings = splitCommand(command);
        List<String> temp = new ArrayList<>();
        for (String s : strings) {
            if (s.matches("#\\{\\S+}")) {
                temp.add(s);
            }
        }

        List<String> res = new ArrayList<>();
        for (String s : temp) {
            if (!res.contains(s)) {
                res.add(s);
            }
        }
        return res;
    }
}
