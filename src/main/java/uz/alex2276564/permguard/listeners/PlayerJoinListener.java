package uz.alex2276564.permguard.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.config.configs.permissionsconfig.PermissionsConfig;
import uz.alex2276564.permguard.events.PlayerHasRestrictedPermissionEvent;
import uz.alex2276564.permguard.utils.TelegramNotifier;
import uz.alex2276564.permguard.utils.adventure.MessageManager;

import java.io.*;
import java.util.Date;

public class PlayerJoinListener implements Listener {
    private final PermGuard plugin;
    private final TelegramNotifier telegramNotifier;
    private final MessageManager messageManager;

    public PlayerJoinListener(PermGuard plugin) {
        this.plugin = plugin;
        this.telegramNotifier = new TelegramNotifier(plugin);
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        for (PermissionsConfig.PermissionEntry entry : plugin.getConfigManager().getAllPermissions()) {
            if (!entry.permission.equals("*") && player.hasPermission("*")) {
                MessagesConfig msg = plugin.getConfigManager().getMessagesConfig();
                Component kickComponent = messageManager.parse(msg.general.wildcardPermissionConflict);
                player.kick(kickComponent);
                return;
            }

            if (player.hasPermission(entry.permission)) {
                final PlayerHasRestrictedPermissionEvent restrictedPermissionEvent =
                        new PlayerHasRestrictedPermissionEvent(player, entry.permission, entry.cmd, entry.log, entry.kickMessage);
                Bukkit.getPluginManager().callEvent(restrictedPermissionEvent);
                if(restrictedPermissionEvent.isCancelled()) {
                    break;
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerHasRestrictedPermissionEvent event) {
        Player player = event.getPlayer();
        String permission = event.getPermission();
        String kickMessage = event.getKickMessage().replace("%permission%", permission);
        String cmd = event.getCmd().replace("%player%", player.getName()).replace("%permission%", permission);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);

        Component kickComponent = messageManager.parse(kickMessage);
        player.kick(kickComponent);

        plugin.getRunner().runAsync(() -> {

            if (event.isLog()) {
                logViolation(player, permission);
            }

            telegramNotifier.sendNotification(player, permission);
        });

        event.setCancelled(true);
    }

    private void logViolation(Player player, String permission) {
        String logMessage = String.format("[%s] Player %s tried to join with restricted permission %s from IP %s",
                new Date(), player.getName(), permission, player.getAddress().getAddress().getHostAddress());

        plugin.getLogger().info(logMessage);

        File logFile = new File(plugin.getDataFolder(), "violations.log");
        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(logMessage);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not write to log file: " + e.getMessage());
        }
    }
}
