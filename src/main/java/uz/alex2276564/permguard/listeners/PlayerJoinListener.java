package uz.alex2276564.permguard.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.config.configs.permissionsconfig.CompiledPermissions;
import uz.alex2276564.permguard.config.configs.permissionsconfig.PermissionsConfig;
import uz.alex2276564.permguard.events.PlayerHasRestrictedPermissionEvent;
import uz.alex2276564.permguard.utils.SecurityUtils;
import uz.alex2276564.permguard.utils.TelegramNotifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PlayerJoinListener implements Listener {
    private final PermGuard plugin;
    private final TelegramNotifier telegramNotifier;

    public PlayerJoinListener(PermGuard plugin) {
        this.plugin = plugin;
        this.telegramNotifier = new TelegramNotifier(plugin);
    }

    @EventHandler(
            priority = EventPriority.LOWEST
    )
    public void on(PlayerJoinEvent event) {
        var player = event.getPlayer();

        CompiledPermissions compiled = plugin.getConfigManager().getCompiledPermissions();

        // 1) '*' first — the most critical case
        if (player.hasPermission("*")) {
            var starEntry = compiled.wildcard();
            if (starEntry != null) {
                var e = new PlayerHasRestrictedPermissionEvent(
                        player, starEntry.permission, starEntry.cmd, starEntry.log, starEntry.kickMessage
                );
                Bukkit.getPluginManager().callEvent(e);
            } else {
                // Fallback if wildcard not configured
                MessagesConfig msg = plugin.getConfigManager().getMessagesConfig();
                Component kickComponent = plugin.getMessageManager().parse(msg.general.wildcardPermissionConflict);
                plugin.getRunner().runAtEntity(player, () -> player.kick(kickComponent));
            }
            return;
        }

        // 2) Regular permissions — already pre-filtered and deduped
        for (PermissionsConfig.PermissionEntry entry : compiled.regular()) {
            if (player.hasPermission(entry.permission)) {
                var e = new PlayerHasRestrictedPermissionEvent(
                        player, entry.permission, entry.cmd, entry.log, entry.kickMessage
                );
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled()) break;
            }
        }
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void on(PlayerHasRestrictedPermissionEvent event) {
        var player = event.getPlayer();
        var name = player.getName();
        var permission = event.getPermission();
        var ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";

        String safeName = SecurityUtils.sanitize(name, SecurityUtils.SanitizeType.PLAYER_NAME);

        String cmd = event.getCmd()
                .replace("<player>", safeName)
                .replace("<permission>", permission);

        plugin.getRunner().runGlobal(() ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
        );

        Component kickComponent = plugin.getMessageManager().parse(event.getKickMessage(), "permission", permission);
        plugin.getRunner().runAtEntity(player, () -> player.kick(kickComponent));

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

    private void logViolation(String name, String permission, String ip, String date) {
        // Decide whether to sanitize the player name for logs
        boolean sanitize = plugin.getConfigManager()
                .getMainConfig()
                .logging
                .sanitizePlayerNames;

        String nameForLog = sanitize
                ? SecurityUtils.sanitize(name, SecurityUtils.SanitizeType.PLAYER_NAME)
                : name;

        // Build message from template
        String logMessage = plugin.getConfigManager().getMessagesConfig()
                .logging.violationEntry
                .replace("<date>", date)
                .replace("<player>", nameForLog)
                .replace("<permission>", permission)
                .replace("<ip>", ip);

        plugin.getLogger().info(logMessage);

        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

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
