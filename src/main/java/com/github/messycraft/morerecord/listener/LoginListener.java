package com.github.messycraft.morerecord.listener;

import com.github.messycraft.morerecord.MoreRecord;
import com.github.messycraft.morerecord.Util;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.io.IOException;
import java.time.LocalDateTime;

public class LoginListener implements Listener {

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) throws IOException {
        String dateStr = Util.getDateString(LocalDateTime.now());
        if (MoreRecord.lastLogDate.containsKey("login") && !dateStr.equals(MoreRecord.lastLogDate.get("login"))) {
            Util.saveData(dateStr, "login");
        }
        MoreRecord.lastLogDate.put("login", dateStr);
        FileConfiguration config = Util.getFileConByType("login");
        String timeStr = Util.getTimeString(LocalDateTime.now());
        config.set(timeStr + ".player", e.getName());
        config.set(timeStr + ".ip", e.getAddress().toString());
    }

}
