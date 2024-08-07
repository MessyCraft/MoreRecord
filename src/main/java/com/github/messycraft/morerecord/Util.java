package com.github.messycraft.morerecord;

import com.github.messycraft.morerecord.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;

import java.io.*;
import java.time.LocalDateTime;

public final class Util {
    private Util() {}

    public static void sM(CommandSender sender, String msg) {
        msg = "&4[&e&lM&6&lR&4] &r" + msg;
        sender.sendMessage(changeColor(msg));
    }

    public static String changeColor(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void noPermission(CommandSender sender) {
        sM(sender, "&cYou don't have permission.");
    }

    public static FileConfiguration loadData(String time, String type) {
        if (getFileConByType(type) == null) {
            return null;
        }
        File dataFolder = new File(MoreRecord.getInstance().getDataFolder(), "data");
        File folder = new File(dataFolder, type);
        File file = new File(folder, time + "_" + type + ".mr");
        if (!file.exists()) {
            return null;
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void saveData(String time, String type) throws IOException {
        if (getFileConByType(type) == null) {
            throw new IOException("Unknown data type: " + type);
        }
        File dataFolder = new File(MoreRecord.getInstance().getDataFolder(), "data");
        File folder = new File(dataFolder, type);
        String recordTime = time;
        if (MoreRecord.lastLogDate.containsKey(type) && !time.equals(MoreRecord.lastLogDate.get(type))) {
            recordTime = MoreRecord.lastLogDate.get(type);
            MoreRecord.lastLogDate.put(type, time);
        }
        File file = new File(folder, recordTime + "_" + type + ".mr");
        if (!file.exists()) {
            file.createNewFile();
            Writer writer = new FileWriter(file);
            writer.write(getFileConByType(type).saveToString());
            writer.flush();
            writer.close();
            resetFileConByType(type);
        }
        else {
            boolean append = true;
            if (type.equalsIgnoreCase("give")) {
                append = false;
            }
            FileOutputStream fos = new FileOutputStream(file, append);
            byte[] bytes = getFileConByType(type).saveToString().getBytes();
            fos.write(bytes,0, bytes.length);
            fos.flush();
            fos.close();
            resetFileConByType(type);
        }
        if (type.equalsIgnoreCase("give")) {
            MoreRecord.giveFile = loadData(time, "give");
        }
    }

    public static void saveAllData(LocalDateTime time) throws IOException {
        String timeStr = getDateString(time);
        saveData(timeStr, "tp");
        saveData(timeStr, "qs");
        saveData(timeStr, "pay");
        saveData(timeStr, "login");
        saveData(timeStr, "cmd");
        saveData(timeStr, "give");
    }

    public static void saveAllData() throws IOException {
        saveAllData(LocalDateTime.now());
    }

    public static FileConfiguration getFileConByType(String type) {
        switch (type) {
            case "tp": return MoreRecord.tpFile;
            case "qs": return MoreRecord.qsFile;
            case "pay": return MoreRecord.payFile;
            case "login": return MoreRecord.loginFile;
            case "cmd": return MoreRecord.cmdFile;
            case "give": return MoreRecord.giveFile;
        }
        return null;
    }

    public static void resetFileConByType(String type) {
        switch (type) {
            case "tp": MoreRecord.tpFile = new YamlConfiguration(); break;
            case "qs": MoreRecord.qsFile = new YamlConfiguration(); break;
            case "pay": MoreRecord.payFile = new YamlConfiguration(); break;
            case "login": MoreRecord.loginFile = new YamlConfiguration(); break;
            case "cmd": MoreRecord.cmdFile = new YamlConfiguration(); break;
            case "give": MoreRecord.giveFile = new YamlConfiguration(); break;
        }
    }

    public static String getDateFromString(String s) {
        if (s.length() < 8 || s.length() > 10) {
            return null;
        }
        if (s.charAt(4) != '-') {
            return null;
        }
        int x = s.substring(5).indexOf("-") + 5;
        String ans = "";
        try {
            ans = ans + Integer.parseInt(s.substring(0, 4)) + "-";
            int month = Integer.parseInt(s.substring(5, x));
            if (month < 10) {
                ans = ans + "0" + month + "-";
            }
            else {
                ans = ans + month + "-";
            }
            int day = Integer.parseInt(s.substring(x + 1));
            if (day < 10) {
                ans = ans + "0" + day;
            }
            else {
                ans = ans + day;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return ans;
    }

    public static String getTimeFromString(String s) {
        if (s.length() < 3 || s.length() > 5) {
            return null;
        }
        if (!s.contains(":")) {
            return null;
        }
        String ans = "";
        try {
            int hour = Integer.parseInt(s.substring(0, s.indexOf(":")));
            if (hour < 10) {
                ans = ans + "0" + hour + "-";
            }
            else {
                ans = ans + hour + "-";
            }
            int minute = Integer.parseInt(s.substring(s.indexOf(":") + 1));
            if (minute < 10) {
                ans = ans + "0" + minute;
            }
            else {
                ans = ans + minute;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return ans;
    }

    public static String getDateString(LocalDateTime time) {
        String month = time.getMonthValue() + "";
        String day = time.getDayOfMonth() + "";
        if (month.length() == 1) {
            month = "0" + month;
        }
        if (day.length() == 1) {
            day = "0" + day;
        }
        return time.getYear() + "-" + month + "-" + day;
    }

    public static String getTimeString(LocalDateTime time) {
        String hour = time.getHour() + "";
        String minute = time.getMinute() + "";
        String second = time.getSecond() + "";
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        if (second.length() == 1) {
            second = "0" + second;
        }
        return hour + "-" + minute + "-" + second;
    }

    public static boolean isBetweenTime(String target, String time1, String time2) {
        int hour1 = Integer.parseInt(time1.substring(0, 2));
        int hour2 = Integer.parseInt(time2.substring(0, 2));
        int minute1 = Integer.parseInt(time1.substring(3));
        int minute2 = Integer.parseInt(time2.substring(3));
        if (hour1 > hour2) {
            return false;
        }
        if (hour1 == hour2) {
            if (minute1 > minute2) {
                return false;
            }
        }
        int hour = Integer.parseInt(target.substring(0, 2));
        int minute = Integer.parseInt(target.substring(3, 5));
        if (hour1 < hour && hour2 > hour) {
            return true;
        }
        if (hour1 > hour || hour2 < hour) {
            return false;
        }
        if (hour == hour1) {
            if (minute < minute1) {
                return false;
            }
        }
        if (hour == hour2) {
            return minute <= minute2;
        }
        return true;
    }

    public static void autoSetEnables() {
        HandlerList.unregisterAll(MoreRecord.getInstance());
        MoreRecord.enableCmd = MoreRecord.getInstance().getConfig().getBoolean("Enable.cmd");
        MoreRecord.enableGive = MoreRecord.getInstance().getConfig().getBoolean("Enable.give");
        MoreRecord.enableLogin = MoreRecord.getInstance().getConfig().getBoolean("Enable.login");
        MoreRecord.enableTp = MoreRecord.getInstance().getConfig().getBoolean("Enable.tp");
        MoreRecord.enableQs = MoreRecord.getInstance().getConfig().getBoolean("Enable.qs");
        MoreRecord.enablePay = MoreRecord.getInstance().getConfig().getBoolean("Enable.pay");
        if (MoreRecord.enableCmd)
            Bukkit.getPluginManager().registerEvents(new CommandListener(), MoreRecord.getInstance());
        if (MoreRecord.enableGive)
            Bukkit.getPluginManager().registerEvents(new ItemListener(), MoreRecord.getInstance());
        if (MoreRecord.enableLogin)
            Bukkit.getPluginManager().registerEvents(new LoginListener(), MoreRecord.getInstance());
        if (MoreRecord.enableTp)
            Bukkit.getPluginManager().registerEvents(new TpListener(), MoreRecord.getInstance());
        if (MoreRecord.enableQs && MoreRecord.hasQuickShop)
            Bukkit.getPluginManager().registerEvents(new PurchaseListener(), MoreRecord.getInstance());
        if (MoreRecord.enablePay && MoreRecord.hasVault)
            Bukkit.getPluginManager().registerEvents(new PayListener(), MoreRecord.getInstance());
    }
}
