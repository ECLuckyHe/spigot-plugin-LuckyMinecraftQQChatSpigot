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

    public static String getRealCommandByContent(List<Map<String, String>> commandMaps, String content) {
        for (Map<String, String> commandMap : commandMaps) {
            String realCommand = getRealCommand(content, commandMap.get("command"), commandMap.get("mapping"));
            if (realCommand != null) {
                return realCommand;
            }
        }

        return null;
    }

    private static String getRealCommand(String content, String command, String mapping) {
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
//                不是参数，则为普通单词，判断是否完全符合
                if (!contentSplit.get(i).equals(commandSplit.get(i))) {
                    return null;
                }
            } else {
//                是参数
                String s = map.get(commandSplit.get(i));
                if (s == null) {
//                    #{xxx} -> aaa  参数和实际内容对应
                    map.put(commandSplit.get(i), contentSplit.get(i));
                } else {
                    if (!s.equals(contentSplit.get(i))) {
//                        同一个参数不同的位置内容不同
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

        return res.toString().trim();
    }
}
