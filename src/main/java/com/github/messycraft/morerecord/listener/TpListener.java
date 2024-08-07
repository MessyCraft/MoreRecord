package com.github.messycraft.morerecord.listener;

import com.github.messycraft.morerecord.MoreRecord;
import com.github.messycraft.morerecord.Util;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TpListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void exec(PlayerTeleportEvent e) throws IOException {
        String dateStr = Util.getDateString(LocalDateTime.now());
        if (MoreRecord.lastLogDate.containsKey("tp") && !dateStr.equals(MoreRecord.lastLogDate.get("tp"))) {
            Util.saveData(dateStr, "tp");
        }
        MoreRecord.lastLogDate.put("tp", dateStr);
        FileConfiguration config = Util.getFileConByType("tp");
        List<PlayerTeleportEvent.TeleportCause> list = new ArrayList<>();
        list.add(PlayerTeleportEvent.TeleportCause.SPECTATE);
        list.add(PlayerTeleportEvent.TeleportCause.COMMAND);
        list.add(PlayerTeleportEvent.TeleportCause.PLUGIN);
        if (!list.contains(e.getCause())) {
            return;
        }
        String timeStr = Util.getTimeString(LocalDateTime.now());
        config.set(timeStr + ".player", e.getPlayer().getName());
        config.set(timeStr + ".cause", e.getCause().name());
        config.set(timeStr + ".from-x", String.format("%.1f",e.getFrom().getX()));
        config.set(timeStr + ".from-y", String.format("%.1f",e.getFrom().getY()));
        config.set(timeStr + ".from-z", String.format("%.1f",e.getFrom().getZ()));
        config.set(timeStr + ".from-world", e.getFrom().getWorld().getName());
        config.set(timeStr + ".to-x", String.format("%.1f",e.getTo().getX()));
        config.set(timeStr + ".to-y", String.format("%.1f",e.getTo().getY()));
        config.set(timeStr + ".to-z", String.format("%.1f",e.getTo().getZ()));
        config.set(timeStr + ".to-world", e.getTo().getWorld().getName());
    }

}
