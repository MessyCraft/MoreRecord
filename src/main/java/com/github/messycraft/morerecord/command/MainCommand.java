package com.github.messycraft.morerecord.command;

import com.github.messycraft.morerecord.MoreRecord;
import com.github.messycraft.morerecord.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Util.sM(sender, "&e&lMore&6&lRecord &7v" + MoreRecord.version + " &3by ImCur_");
            Util.sM(sender, "&8Type \"&a/mr help&8\" for command help.");
        }
        else {
            if (args[0].equalsIgnoreCase("save")) {
                if (!sender.hasPermission("morerecord.save")) {
                    Util.noPermission(sender);
                    return true;
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            Util.saveAllData();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.runTaskAsynchronously(MoreRecord.getInstance());
                Util.sM(sender, "&c&lData saved.");
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("morerecord.reload")) {
                    Util.noPermission(sender);
                    return true;
                }
                MoreRecord.getInstance().reloadConfig();
                Util.autoSetEnables();
                Util.sM(sender, "&c&lConfiguration reloaded successfully!");
                Bukkit.getScheduler().cancelTask(MoreRecord.taskId);
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        MoreRecord.autoSave();
                    }
                };
                runnable.runTaskTimerAsynchronously(MoreRecord.getInstance(),
                        1L,
                        (long) (MoreRecord.getInstance().getConfig().getDouble("auto-save-minute") * 1200L));
                MoreRecord.taskId = runnable.getTaskId();
            }
            else if (args[0].equalsIgnoreCase("help")) {
                Util.sM(sender, "&a> &3/mr help: &b显示此帮助.");
                Util.sM(sender, "&a> &3/mr reload: &b重载配置文件.");
                Util.sM(sender, "&a> &3/mr save: &b立即保存一次数据.");
                Util.sM(sender, "&a> &3/mr query <type> <other>: &b查询记录的数据.");
                Util.sM(sender, "&a> &8使用 &3/mr query &8获取该命令更多详细参数");
            }
            else if (args[0].equalsIgnoreCase("query")) {
                if (args.length == 1) {
                    Util.sM(sender, "&a> &3/mr query pay" + (MoreRecord.enablePay && MoreRecord.hasVault ? "" : " &7[未启用]"));
                    Util.sM(sender, "&a> &3/mr query tp" + (MoreRecord.enableTp ? "" : " &7[未启用]"));
                    Util.sM(sender, "&a> &3/mr query login" + (MoreRecord.enableLogin ? "" : " &7[未启用]"));
                    Util.sM(sender, "&a> &3/mr query qs" + (MoreRecord.enableQs && MoreRecord.hasQuickShop ? "" : " &7[未启用]"));
                    Util.sM(sender, "&a> &3/mr query cmd" + (MoreRecord.enableCmd ? "" : " &7[未启用]"));
                    Util.sM(sender, "&a> &3/mr query give" + (MoreRecord.enableGive ? "" : " &7[未启用]"));
                    Util.sM(sender, "&7#: 参数列表 ->");
                    Util.sM(sender, "&7  - [date] (必填)");
                    Util.sM(sender, "&7  - <time1>-<time2> (选填)");
                    Util.sM(sender, "&7  - player={player_name} (选填)");
                    Util.sM(sender, "&7  - page={page} (一般不需要填)");
                    Util.sM(sender, "&7#: date格式 yyyy-MM-dd, time格式 hh:mm (起始时间和结束时间)");
                    Util.sM(sender, "&7#: e.g. &8/mr query qs 2022-08-09 15:00-18:00 player=TestPlayer");
                }
                else {
                    if (args.length < 3) {
                        Util.sM(sender, "&c缺少必填参数: [data] (yyyy-MM-dd)");
                    }
                    else {
                        String date = Util.getDateFromString(args[2]);
                        if (date == null) {
                            Util.sM(sender, "&c日期 \"" + args[2] + "\" 格式错误(yyyy-MM-dd)!");
                            return true;
                        }
                        String type = args[1];
                        if (!sender.hasPermission("morerecord.query." + type)) {
                            Util.noPermission(sender);
                            return true;
                        }
                        if (Util.getFileConByType(type) == null) {
                            Util.sM(sender, "&c记录类型 \"" + type + "\" 不存在!");
                            return true;
                        }
                        Util.sM(sender, "&3&l查询中 请稍等待...");
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (date.equals(Util.getDateString(LocalDateTime.now()))) {
                                    try {
                                        Util.saveData(date, type);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                FileConfiguration data = Util.loadData(date, type);
                                if (data == null) {
                                    Util.sM(sender, "&c指定日期 \"" + date + "\" 不存在!");
                                    return;
                                }
                                String player = null;
                                int page = 1;
                                String time1 = null;
                                String time2 = null;
                                boolean useTime = false;
                                boolean usePlayer = false;
                                if (args.length > 3) {
                                    for (int i=3; i<args.length; i++) {
                                        if (args[i].length() > 7 && args[i].substring(0, 7).equalsIgnoreCase("player=")) {
                                            player = args[i].substring(7);
                                            usePlayer = true;
                                        }
                                        else if (args[i].length() > 5 && args[i].substring(0, 5).equalsIgnoreCase("page=")) {
                                            try {
                                                page = Integer.parseInt(args[i].substring(5));
                                                if (page < 1) {
                                                    page = 1;
                                                }
                                            } catch (NumberFormatException e) {
                                                Util.sM(sender, "&c未知的参数: \"" + args[i] + "\"");
                                                return;
                                            }
                                        }
                                        else if (args[i].contains("-") &&
                                                Util.getTimeFromString(args[i].substring(0, args[i].indexOf("-"))) != null &&
                                                Util.getTimeFromString(args[i].substring(args[i].indexOf("-") + 1)) != null) {
                                            time1 = Util.getTimeFromString(args[i].substring(0, args[i].indexOf("-")));
                                            time2 = Util.getTimeFromString(args[i].substring(args[i].indexOf("-") + 1));
                                            useTime = true;
                                        }
                                        else {
                                            Util.sM(sender, "&c未知的参数: \"" + args[i] + "\"");
                                            return;
                                        }
                                    }
                                }
                                Set<String> set = data.getKeys(false);
                                set.remove("unknown-target");
                                Iterator<String> it = set.iterator();
                                List<String> list = new ArrayList<>();
                                while (it.hasNext()) {
                                    String targetTime = it.next();
                                    if (useTime && !Util.isBetweenTime(targetTime, time1, time2)) {
                                        continue;
                                    }
                                    if (usePlayer && !data.getString(targetTime + ".player").equalsIgnoreCase(player)) {
                                        continue;
                                    }
                                    if (type.equalsIgnoreCase("give") && data.get(targetTime + ".target") == null) {
                                        continue;
                                    }
                                    list.add(targetTime);
                                }
                                int limit = MoreRecord.getInstance().getConfig().getInt("query-max-lines");
                                int startData = (page-1) * limit;
                                if (startData >= list.size()) {
                                    if (page == 1) {
                                        Util.sM(sender, "&b未查找到符合筛选条件的数据.");
                                        return;
                                    }
                                    Util.sM(sender, "&c页数 " + page + " 不存在!");
                                    return;
                                }
                                Util.sM(sender, "&8&l----------------------------------------");
                                Util.sM(sender, "&a类型 " + type + " 的记录(" + date + "):");
                                String info = "&9* ";
                                if (usePlayer) {
                                    info = info + "Player=" + player;
                                }
                                if (useTime) {
                                    String split = usePlayer?" , ":"";
                                    info = info + split + "StartTime=" + time1.replace("-", ":") + " , EndTime=" + time2.replace("-", ":");
                                }
                                if (!info.equals("&9* ")) {
                                    Util.sM(sender, info);
                                }
                                switch (type) {
                                    case "pay":
                                        if (!MoreRecord.hasVault) {
                                            Util.sM(sender, "&c[!] 未检测到前置 Vault, 你可能无法查询相关内容.");
                                        }
                                        for (int i=startData; i < startData+limit && i < list.size(); i++) {
                                            Util.sM(sender, "&2&l" + (i+1) + ". &3时间: &b" + list.get(i).replace("-", ":"));
                                            Util.sM(sender, "  &7- &3支付者: &b" + data.getString(list.get(i) + ".player"));
                                            Util.sM(sender, "  &7- &3被支付者: &b" + data.getString(list.get(i) + ".target"));
                                            Util.sM(sender, "  &7- &3金额(保留两位): &b" + data.getString(list.get(i) + ".pay"));
                                        }
                                        break;
                                    case "tp":
                                        for (int i=startData; i < startData+limit && i < list.size(); i++) {
                                            Util.sM(sender, "&2&l" + (i+1) + ". &3时间: &b" + list.get(i).replace("-", ":"));
                                            Util.sM(sender, "  &7- &3玩家: &b" + data.getString(list.get(i) + ".player"));
                                            Util.sM(sender, "  &7- &3传送方式: &b" + data.getString(list.get(i) + ".cause"));
                                            Util.sM(sender, "  &7- &3起始位置: &b坐标["
                                                    + data.getString(list.get(i) + ".from-x") + ", "
                                                    + data.getString(list.get(i) + ".from-y") + ", "
                                                    + data.getString(list.get(i) + ".from-z") + "] 世界["
                                                    + data.getString(list.get(i) + ".from-world") + "]"
                                            );
                                            Util.sM(sender, "  &7- &3传送后位置: &b坐标["
                                                    + data.getString(list.get(i) + ".to-x") + ", "
                                                    + data.getString(list.get(i) + ".to-y") + ", "
                                                    + data.getString(list.get(i) + ".to-z") + "] 世界["
                                                    + data.getString(list.get(i) + ".to-world") + "]"
                                            );
                                        }
                                        break;
                                    case "login":
                                        for (int i=startData; i < startData+limit && i < list.size(); i++) {
                                            Util.sM(sender, "&2&l" + (i+1) + ". &3时间: &b" + list.get(i).replace("-", ":"));
                                            Util.sM(sender, "  &7- &3登录玩家: &b" + data.getString(list.get(i) + ".player"));
                                            Util.sM(sender, "  &7- &3登录IP: &b" + data.getString(list.get(i) + ".ip"));
                                        }
                                        break;
                                    case "qs":
                                        if (!MoreRecord.hasQuickShop) {
                                            Util.sM(sender, "&c[!] 未检测到前置 QuickShop, 你可能无法查询相关内容.");
                                        }
                                        for (int i=startData; i < startData+limit && i < list.size(); i++) {
                                            Util.sM(sender, "&2&l" + (i+1) + ". &3时间: &b" + list.get(i).replace("-", ":"));
                                            Util.sM(sender, "  &7- &3购买者: &b" + data.getString(list.get(i) + ".player"));
                                            Util.sM(sender, "  &7- &3商店所有者: &b" + data.getString(list.get(i) + ".owner"));
                                            String displayName = data.getItemStack(list.get(i) + ".item").getItemMeta().getDisplayName();
                                            if (displayName == null || displayName.equals("")) {
                                                displayName = data.getItemStack(list.get(i) + ".item").getType().name();
                                            }
                                            Util.sM(sender, "  &7- &3购买物品: &b[&7" + displayName + "&b]");
                                            Util.sM(sender, "  &7- &3购买数量: &b" + data.getString(list.get(i) + ".amount"));
                                            Util.sM(sender, "  &7- &3物品单价: &b" + data.getString(list.get(i) + ".price"));
                                        }
                                        break;
                                    case "give":
                                        for (int i=startData; i < startData+limit && i < list.size(); i++) {
                                            Util.sM(sender, "&2&l" + (i+1) + ". &3时间: &b" + list.get(i).replace("-", ":"));
                                            Util.sM(sender, "  &7- &3扔出物品玩家: &b" + data.getString(list.get(i) + ".player"));
                                            Util.sM(sender, "  &7- &3拾取物品者: &b" + data.getString(list.get(i) + ".target"));
                                            Util.sM(sender, "  &7- &3给予物品: &b[&7" + data.getString(list.get(i) + ".item") + "&b]");
                                            Util.sM(sender, "  &7- &3给予数量: &b" + data.getInt(list.get(i) + ".amount"));
                                        }
                                        break;
                                    case "cmd":
                                        for (int i=startData; i < startData+limit && i < list.size(); i++) {
                                            Util.sM(sender, "&2&l" + (i+1) + ". &3时间: &b" + list.get(i).replace("-", ":"));
                                            Util.sM(sender, "  &7- &3玩家: &b" + data.getString(list.get(i) + ".player"));
                                            Util.sM(sender, "  &7- &3使用命令: &b" + data.getString(list.get(i) + ".cmd"));
                                        }
                                        break;
                                    default:
                                        Util.sM(sender, "&c记录类型 \"" + type + "\" 不存在!");
                                        return;
                                }
                                TextComponent lastPage = new TextComponent(Util.changeColor("&7&n<< 上一页"));
                                TextComponent nextPage = new TextComponent(Util.changeColor("&7&n下一页 >>"));
                                String cmd = "/mr query " + type + " " + date;
                                if (usePlayer) {
                                    cmd = cmd + " player=" + player;
                                }
                                if (useTime) {
                                    cmd = cmd + " " + time1.replace("-", ":") + "-" + time2.replace("-", ":");
                                }
                                if (page > 1) {
                                    lastPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd + " page=" + (page - 1)));
                                }
                                nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd + " page=" + (page+1)));
                                sender.spigot().sendMessage(
                                        new TextComponent(Util.changeColor("&4[&e&lM&6&lR&4] &r")),
                                        lastPage,
                                        new TextComponent(Util.changeColor(
                                        "   &8&l(" + page + "/"
                                            + (list.size() / limit + (list.size() % limit != 0 ? 1 : 0))
                                            + ")   ")),
                                        nextPage);
                                Util.sM(sender, "&8&l----------------------------------------");
                            }
                        }.runTaskAsynchronously(MoreRecord.getInstance());
                    }
                }
            }
            else {
                Util.sM(sender, "&cUnknown sub command.");
                Util.sM(sender, "&8Type \"&a/mr help&8\" for command help.");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if ("reload".startsWith(args[0])) {
                list.add("reload");
            }
            if ("help".startsWith(args[0])) {
                list.add("help");
            }
            if ("query".startsWith(args[0])) {
                list.add("query");
            }
            if ("save".startsWith(args[0])) {
                list.add("save");
            }
            return list;
        }
        if (args.length == 2) {
            List<String> list = new ArrayList<>();
            if (args[0].equalsIgnoreCase("query")) {
                if ("pay".startsWith(args[1])) {
                    list.add("pay");
                }
                if ("give".startsWith(args[1])) {
                    list.add("give");
                }
                if ("qs".startsWith(args[1])) {
                    list.add("qs");
                }
                if ("cmd".startsWith(args[1])) {
                    list.add("cmd");
                }
                if ("tp".startsWith(args[1])) {
                    list.add("tp");
                }
                if ("login".startsWith(args[1])) {
                    list.add("login");
                }
            }
            return list;
        }
        if (args.length == 3) {
            String date = Util.getDateString(LocalDateTime.now());
            if (date.startsWith(args[2])) {
                List<String> list = new ArrayList<>();
                list.add(date);
                return list;
            }
        }
        return new ArrayList<>();
    }
}
