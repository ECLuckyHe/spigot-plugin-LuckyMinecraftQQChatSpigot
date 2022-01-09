package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacePlaceholderUtil {
    public static String replacePlaceholderWithString(String formatString, String ...strings) {
        if (strings.length % 2 != 0) {
            return null;
        }

        StringBuilder patternString = new StringBuilder();

        for (int i = 0; i < strings.length; i += 2) {
            patternString.append(strings[i]);
            if (i + 2 < strings.length) {
                patternString.append("|");
            }
        }

        Pattern p = Pattern.compile(patternString.toString());
        Matcher m = p.matcher(formatString);

        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            for (int i = 1; i < strings.length; i += 2) {
                if (m.group().equals(strings[i - 1])) {
                    m.appendReplacement(sb, strings[i]);
                }
            }
        }
        m.appendTail(sb);

        return sb.toString();
    }
}
