package com.github.messycraft.morerecord.listener;

import com.github.messycraft.morerecord.MoreRecord;
import com.github.messycraft.morerecord.Util;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class ItemListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent e) throws IOException {
        String dateStr = Util.getDateString(LocalDateTime.now());
        if (MoreRecord.lastLogDate.containsKey("give") && !dateStr.equals(MoreRecord.lastLogDate.get("give"))) {
            Util.saveData(dateStr, "give");
        }
        MoreRecord.lastLogDate.put("give", dateStr);
        FileConfiguration config = Util.getFileConByType("give");
        if (e.isCancelled()) {
            return;
        }
        String timeStr = Util.getTimeString(LocalDateTime.now());
        config.set(timeStr + ".player", e.getPlayer().getName());
        config.set(timeStr + ".item", e.getItemDrop().getUniqueId().toString());
        config.set("unknown-target." + e.getItemDrop().getUniqueId().toString(), timeStr);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickup(EntityPickupItemEvent e) throws IOException {
        String dateStr = Util.getDateString(LocalDateTime.now());
        if (MoreRecord.lastLogDate.containsKey("give") && !dateStr.equals(MoreRecord.lastLogDate.get("give"))) {
            Util.saveData(dateStr, "give");
        }
        MoreRecord.lastLogDate.put("give", dateStr);
        FileConfiguration config = Util.getFileConByType("give");
        ConfigurationSection target = config.getConfigurationSection("unknown-target");
        if (target == null) {
            return;
        }
        String timeStr;
        Set<String> uuids = target.getKeys(false);
        Iterator<String> it = uuids.iterator();
        while (it.hasNext()) {
            String uniqueId = it.next();
            if (Bukkit.getEntity(UUID.fromString(uniqueId)) == null) {
                config.set(config.getString("unknown-target." + uniqueId), null);
                config.set("unknown-target." + uniqueId, null);
            }
        }
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        if (uuids.contains(e.getItem().getUniqueId().toString())) {
            timeStr = target.getString(e.getItem().getUniqueId().toString());
            if (config.getString(timeStr + ".player") == null) {
                return;
            }
            if (config.getString(timeStr + ".player").equalsIgnoreCase(e.getEntity().getName())) {
                String uniqueId = e.getItem().getUniqueId().toString();
                config.set(config.getString("unknown-target." + uniqueId), null);
                config.set("unknown-target." + uniqueId, null);
                return;
            }
            config.set(timeStr + ".target", e.getEntity().getName());
            String name = e.getItem().getItemStack().getItemMeta().getDisplayName();
            config.set(timeStr + ".item", (name == null || name.isEmpty()) ? e.getItem().getItemStack().getType().name() : name);
            config.set(timeStr + ".amount", e.getItem().getItemStack().getAmount());
            config.set("unknown-target." + e.getItem().getUniqueId().toString(), null);
        }
    }
}
