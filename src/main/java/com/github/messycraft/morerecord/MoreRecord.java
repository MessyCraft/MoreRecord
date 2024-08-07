package com.github.messycraft.morerecord;

import com.github.messycraft.morerecord.bstats.Metrics;
import com.github.messycraft.morerecord.command.MainCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MoreRecord extends JavaPlugin {

    public static String version;
    private static MoreRecord instance;
    public static boolean hasVault = false;
    public static boolean hasQuickShop = false;
    public static int taskId;

    public static boolean enableCmd;
    public static boolean enableGive;
    public static boolean enableLogin;
    public static boolean enablePay;
    public static boolean enableQs;
    public static boolean enableTp;

    public static FileConfiguration tpFile = new YamlConfiguration();
    public static FileConfiguration payFile = new YamlConfiguration();
    public static FileConfiguration loginFile = new YamlConfiguration();
    public static FileConfiguration qsFile = new YamlConfiguration();
    public static FileConfiguration cmdFile = new YamlConfiguration();
    public static FileConfiguration giveFile = new YamlConfiguration();

    public static Map<String, String> lastLogDate = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        version = getDescription().getVersion();
        try {
            if (Integer.parseInt(getServer().getBukkitVersion().split("\\.")[1]) < 12) {
                getLogger().severe("本插件只适用于 1.12+ 服务器核心.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } catch (NumberFormatException ignored) {}
        getLogger().info("Running MoreRecord v" + version + " by ImCur_ .");
        saveDefaultConfig();
        reloadConfig();
        saveDefaultDataFolder();
        instance = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                getLogger().info("Loading...");
                if (getServer().getPluginManager().isPluginEnabled("Vault")) {
                    hasVault = true;
                    getLogger().info("  - Using Vault API!");
                }
                else {
                    getLogger().warning("[!] 未检测到插件 Vault");
                    getLogger().warning("你将无法记录关于玩家之间支付游戏币的内容");
                }
                if (getServer().getPluginManager().isPluginEnabled("QuickShop")) {
                    hasQuickShop = true;
                    getLogger().info("  - Using QuickShop API!");
                }
                else {
                    getLogger().warning("[!] 未检测到插件 QuickShop");
                    getLogger().warning("你将无法记录关于玩家使用商店购买物品的内容");
                }
            }
        }.runTask(this);
        if (Util.loadData(Util.getDateString(LocalDateTime.now()), "give") != null) {
            giveFile = Util.loadData(Util.getDateString(LocalDateTime.now()), "give");
            lastLogDate.put("give", Util.getDateString(LocalDateTime.now()));
        }
        Util.autoSetEnables();
        getCommand("morerecord").setExecutor(new MainCommand());
        getCommand("morerecord").setTabCompleter(new MainCommand());

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                autoSave();
            }
        };
        runnable.runTaskTimerAsynchronously(this,
                100L,
                (long) (getConfig().getDouble("auto-save-minute") * 1200L));
        taskId = runnable.getTaskId();

        new Metrics(this, 22908);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        HandlerList.unregisterAll(this);
        try {
            Util.saveAllData();
        } catch (IOException ignored) {}
        getLogger().info("Data saved.");
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Plugin has been unloaded.");
    }

    private void saveDefaultDataFolder() {
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        List<File> fileList = new ArrayList<>();
        fileList.add(new File(dataFolder, "tp"));
        fileList.add(new File(dataFolder, "pay"));
        fileList.add(new File(dataFolder, "login"));
        fileList.add(new File(dataFolder, "qs"));
        fileList.add(new File(dataFolder, "cmd"));
        fileList.add(new File(dataFolder, "give"));
        for (int i=0; i<fileList.size(); i++) {
            if (!fileList.get(i).exists()) {
                fileList.get(i).mkdir();
            }
        }
        File readMe = new File(dataFolder, "README.txt");
        if (readMe.exists()) {
            readMe.delete();
        }
        try {
            Files.copy(getResource("README.txt"), readMe.toPath());
        } catch (IOException ignored) {}
    }

    public static void autoSave() {
        try {
            Util.saveAllData();
            instance.getLogger().info("正在保存记录的数据...[下一次保存在" + String.format("%.1f", instance.getConfig().getDouble("auto-save-minute")) + "分钟后]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MoreRecord getInstance() {
        return instance;
    }
}
