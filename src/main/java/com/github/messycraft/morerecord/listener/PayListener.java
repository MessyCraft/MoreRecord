package com.github.messycraft.morerecord.listener;

import com.github.messycraft.morerecord.MoreRecord;
import com.github.messycraft.morerecord.Util;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.time.LocalDateTime;

public class PayListener implements Listener {

    private Economy econ;

    public PayListener() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onUseCommand(PlayerCommandPreprocessEvent e) throws IOException {
        String dateStr = Util.getDateString(LocalDateTime.now());
        if (MoreRecord.lastLogDate.containsKey("pay") && !dateStr.equals(MoreRecord.lastLogDate.get("pay"))) {
            Util.saveData(dateStr, "pay");
        }
        MoreRecord.lastLogDate.put("pay", dateStr);
        if (!MoreRecord.hasVault || econ == null) {
            return;
        }
        if (
            e.getMessage().startsWith("/pay ") ||
            e.getMessage().startsWith("/cmi pay ") ||
            e.getMessage().startsWith("/cmi:cmi pay ") ||
            e.getMessage().startsWith("/cmi:pay ") ||
            e.getMessage().startsWith("/essentials:pay ")
        ) {
            if (e.getMessage().endsWith("pay ")) {
                return;
            }
            String sub = e.getMessage().substring(e.getMessage().indexOf("pay") + 4);
            if (!sub.contains(" ")) {
                return;
            }
            String targetName = sub.substring(0, sub.indexOf(" "));
            if (e.getPlayer().getName().equalsIgnoreCase(targetName)) {
                return;
            }
            if (sub.length() - targetName.length() == 1) {
                return;
            }
            String other = sub.substring(sub.indexOf(" ") + 1);
            String coinStr = other;
            if (other.contains(" ")) {
                coinStr = other.substring(0, other.indexOf(" "));
            }
            double coin;
            try {
                coin = Double.parseDouble(coinStr);
            } catch (NumberFormatException ex) {
                return;
            }
            double nowBalance = econ.getBalance(e.getPlayer());
            OfflinePlayer offlinePlayer = e.getPlayer();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (nowBalance == econ.getBalance(offlinePlayer)) {
                        return;
                    }
                    double ans = nowBalance - econ.getBalance(offlinePlayer) - coin;
                    if (ans < 1 && ans > -1) {
                        String timeStr = Util.getTimeString(LocalDateTime.now());
                        MoreRecord.payFile.set(timeStr + ".player", offlinePlayer.getName());
                        MoreRecord.payFile.set(timeStr + ".target", targetName.toLowerCase());
                        MoreRecord.payFile.set(timeStr + ".pay", String.format("%.2f", coin));
                    }
                }
            }.runTaskLaterAsynchronously(MoreRecord.getInstance(), 8);
        }
    }

}
