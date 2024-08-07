package com.github.messycraft.morerecord.listener;

import com.github.messycraft.morerecord.MoreRecord;
import com.github.messycraft.morerecord.Util;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.api.event.ShopSuccessPurchaseEvent;

import java.io.IOException;
import java.time.LocalDateTime;

public class PurchaseListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void exec(ShopSuccessPurchaseEvent e) throws IOException {
        String dateStr = Util.getDateString(LocalDateTime.now());
        if (MoreRecord.lastLogDate.containsKey("qs") && !dateStr.equals(MoreRecord.lastLogDate.get("qs"))) {
            Util.saveData(dateStr, "qs");
        }
        MoreRecord.lastLogDate.put("qs", dateStr);
        FileConfiguration config = Util.getFileConByType("qs");
        if (Bukkit.getOfflinePlayer(e.getShop().getOwner()).getName()
                .equalsIgnoreCase(Bukkit.getPlayer(e.getPurchaser()).getName())) {
            return;
        }
        String timeStr = Util.getTimeString(LocalDateTime.now());
        config.set(timeStr + ".owner", Bukkit.getOfflinePlayer(e.getShop().getOwner()).getName());
        config.set(timeStr + ".player", Bukkit.getPlayer(e.getPurchaser()).getName());
        config.set(timeStr + ".item", e.getShop().getItem());
        config.set(timeStr + ".amount", e.getAmount());
        config.set(timeStr + ".price", e.getShop().getPrice());
    }

}
