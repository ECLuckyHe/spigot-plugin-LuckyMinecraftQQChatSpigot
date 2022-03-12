package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.QqOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BindQqUtil {
    private static final List<Map<String, Object>> bindList = new ArrayList<>();

    public static List<Map<String, Object>> getBindList() {
        return bindList;
    }

    public static void addBind(long qq, String mcid) {
        synchronized (bindList) {
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("qq", qq);
            newMap.put("mcid", mcid);
            getBindList().add(newMap);
        }
    }

    public static List<Long> getQqsByMcid(String mcid) {
        List<Long> res;
        synchronized (bindList) {
            List<Map<String, Object>> bindList = getBindList();
            res = new ArrayList<>();
            for (Map<String, Object> map : bindList) {
                if (map.get("mcid").equals(mcid)) {
                    res.add(((Long) map.get("qq")));
                }
            }
        }

        return res;
    }

    public static void confirmBind(long qq, String mcid) throws IOException {
        synchronized (bindList) {
            List<Map<String, Object>> bindList = getBindList();

            List<Map<String, Object>> toBeDel = new ArrayList<>();
            for (Map<String, Object> map : bindList) {
                String existMcid = (String) map.get("mcid");
                long existQq = (long) map.get("qq");

                if (existMcid.equals(mcid) && existQq == qq) {
                    toBeDel.add(map);
                    QqOperation.bind(existQq, existMcid);
                }
            }

            for (Map<String, Object> delMap : toBeDel) {
                bindList.remove(delMap);
            }
        }
    }

    public static void denyBind(long qq, String mcid) {
        synchronized (bindList) {
            getBindList().removeIf(o -> ((long) o.get("qq")) == qq && o.get("mcid").equals(mcid));
        }
    }
}
