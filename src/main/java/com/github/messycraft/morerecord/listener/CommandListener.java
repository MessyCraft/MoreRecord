package com.github.messycraft.morerecord.listener;

import com.github.messycraft.morerecord.MoreRecord;
import com.github.messycraft.morerecord.Util;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.IOException;
import java.time.LocalDateTime;

public class CommandListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onCommandInput(PlayerCommandPreprocessEvent e) throws IOException {
        String dateStr = Util.getDateString(LocalDateTime.now());
        if (MoreRecord.lastLogDate.containsKey("cmd") && !dateStr.equals(MoreRecord.lastLogDate.get("cmd"))) {
            Util.saveData(dateStr, "cmd");
        }
        MoreRecord.lastLogDate.put("cmd", dateStr);
        FileConfiguration config = Util.getFileConByType("cmd");
        String timeStr = Util.getTimeString(LocalDateTime.now());
        config.set(timeStr + ".player", e.getPlayer().getName());
        config.set(timeStr + ".cmd", e.getMessage());
    }

}
