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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
                final PlayerHasRestrictedPermissionEvent restrictedPermissionEvent = new PlayerHasRestrictedPermissionEvent(player, entry.permission, entry.cmd, entry.log, entry.kickMessage);
                Bukkit.getPluginManager().callEvent(restrictedPermissionEvent);
                if (restrictedPermissionEvent.isCancelled()) {
                    break;
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerHasRestrictedPermissionEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        String permission = event.getPermission();
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";

        String safeName = sanitizeForCommand(name);

        String cmd = event.getCmd()
                .replace("<player>", safeName)
                .replace("<permission>", permission);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);

        Component kickComponent = messageManager.parse(event.getKickMessage(), "permission", permission);
        player.kick(kickComponent);

        plugin.getRunner().runAsync(() -> {
            String date = java.time.ZonedDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

            if (event.isLog()) {
                logViolation(name, permission, ip, date);
            }

            telegramNotifier.sendNotification(name, permission, ip, date);

        });

        event.setCancelled(true);
    }

    private String sanitizeForCommand(String input) {
        if (input == null) return "";

        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // Blocking control characters
            if (Character.isISOControl(c)) continue;

            // Blocking dangerous characters for commands
            if (";&|`$()[]{}\"'\\<>\n\r§".indexOf(c) >= 0) {
                String msg = plugin.getConfigManager().getMessagesConfig()
                        .logging.dangerousCharBlocked
                        .replace("<char>", String.valueOf(c))
                        .replace("<input>", input);
                plugin.getLogger().warning(msg);
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }

    private void logViolation(String name, String permission, String ip, String date) {
        // Build message from template
        String logMessage = plugin.getConfigManager().getMessagesConfig()
                .logging.violationEntry
                .replace("<date>", date)
                .replace("<player>", name)
                .replace("<permission>", permission)
                .replace("<ip>", ip);

        plugin.getLogger().info(logMessage);

        // Ensure folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Use file name from main config
        String fileName = plugin.getConfigManager().getMainConfig().logging.violationsFile;
        Path logPath = plugin.getDataFolder().toPath().resolve(fileName);

        try {
            Files.writeString(
                    logPath,
                    logMessage + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            String msg = plugin.getConfigManager().getMessagesConfig()
                    .logging.fileWriteError
                    .replace("<error>", e.getMessage());
            plugin.getLogger().severe(msg);
        }
    }
}
