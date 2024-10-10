package uz.alex2276564.permguard.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.utils.ConfigManager;

import java.io.*;
import java.util.Date;
import java.util.Map;

public class JoinListener implements Listener {

    @EventHandler(
            priority = EventPriority.LOWEST,
            ignoreCancelled = true
    )
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (Map<String, Object> entry: ConfigManager.getRestrictedPermissions()) {
            String permission = (String) entry.get("permission");
            if (player.hasPermission(permission)) {
                String cmd = ((String) entry.get("cmd")).replace("%player%", player.getName()).replace("%permission%", permission);
                boolean log = (boolean) entry.get("log");
                String kickMessage = ((String) entry.get("kickMessage")).replace("%permission%", permission);

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                if (log) {
                    logViolation(player, permission);
                }

                player.kickPlayer(kickMessage);
                break;
            }
        }

    }
    private void logViolation(Player player, String permission) {
        String logMessage = String.format("[%s] Player %s tried to join with restricted permission %s from IP %s",
                new Date(), player.getName(), permission, player.getAddress().getAddress().getHostAddress());

        PermGuard.getInstance().getLogger().info(logMessage);

        File logFile = new File(PermGuard.getInstance().getDataFolder(), "violations.log");
        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(logMessage);
        } catch (IOException e) {
            PermGuard.getInstance().getLogger().severe("Could not write to log file: " + e.getMessage());
        }
    }
}
