package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static String getRealCommand(String content, String command, String mapping) {
//        获取实际执行指令
        List<String> contentSplit = splitCommand(content);
        List<String> commandSplit = splitCommand(command);
        List<String> commandArgList = getCommandArgList(command);
        List<String> mappingSplit = splitCommand(mapping);
        List<String> mappingArgList = getCommandArgList(mapping);
        Map<String, String> map = new HashMap<>();

        if (contentSplit.size() != commandSplit.size()) {
            return null;
        }

        int len = contentSplit.size();
        for (int i = 0; i < len; i++) {
            if (!commandArgList.contains(commandSplit.get(i))) {
                if (!contentSplit.get(i).equals(commandSplit.get(i))) {
                    return null;
                }
            } else {
//                是参数
                String s = map.get(commandSplit.get(i));
                if (s == null) {
                    map.put(commandSplit.get(i), contentSplit.get(i));
                } else {
                    if (!s.equals(contentSplit.get(i))) {
                        return null;
                    }
                }
            }
        }

        StringBuilder res = new StringBuilder();
        for (String s : mappingSplit) {
            if (!mappingArgList.contains(s)) {
                res.append(s).append(" ");
            } else {
                res.append(map.get(s)).append(" ");
            }
        }

        return res.toString();
    }
}
