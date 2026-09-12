package uz.alex2276564.permguard.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import uz.alex2276564.permguard.config.PermGuardConfigManager;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.config.configs.permissionsconfig.CompiledPermissions;
import uz.alex2276564.permguard.config.configs.permissionsconfig.PermissionsConfig;
import uz.alex2276564.permguard.events.PlayerHasRestrictedPermissionEvent;
import uz.alex2276564.permguard.telegram.TelegramNotifier;
import uz.alex2276564.permguard.utils.SecurityUtils;
import uz.alex2276564.permguard.utils.adventure.MessageManager;
import uz.alex2276564.permguard.utils.runner.Runner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class PlayerJoinListener implements Listener {

    private final PermGuardConfigManager configManager;
    private final Runner runner;
    private final TelegramNotifier telegramNotifier;
    private final MessageManager messageManager;
    private final Logger logger;
    private final Path dataFolder;

    public PlayerJoinListener(PermGuardConfigManager configManager,
                              Runner runner,
                              TelegramNotifier telegramNotifier,
                              MessageManager messageManager,
                              Logger logger,
                              Path dataFolder) {
        this.configManager = configManager;
        this.runner = runner;
        this.telegramNotifier = telegramNotifier;
        this.messageManager = messageManager;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @EventHandler(
            priority = EventPriority.LOWEST
    )
    public void on(PlayerJoinEvent event) {
        var player = event.getPlayer();

        CompiledPermissions compiled = configManager.getCompiledPermissions();

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
                MessagesConfig msg = configManager.getMessagesConfig();
                Component kickComponent = messageManager.parse(msg.general.wildcardPermissionConflict);
                runner.runAtEntity(player, () -> player.kick(kickComponent));
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

        String safeName = SecurityUtils.sanitize(
                name,
                SecurityUtils.SanitizeType.PLAYER_NAME
        );
        String safeIp = SecurityUtils.sanitize(
                ip,
                SecurityUtils.SanitizeType.IP_ADDRESS
        );

        String cmd = event.getCmd()
                .replace("<player>", safeName)
                .replace("<permission>", permission);

        runner.runGlobal(() ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
        );

        Component kickComponent = messageManager.parse(event.getKickMessage(), "permission", permission);
        runner.runAtEntity(player, () -> player.kick(kickComponent));

        runner.runAsync(() -> {
            String date = ZonedDateTime.now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

            if (event.isLog()) {
                logViolation(name, safeName, permission, safeIp, date);
            }
            telegramNotifier.sendNotification(safeName, permission, safeIp, date);
        });

        event.setCancelled(true);
    }

    private void logViolation(String name, String safeName, String permission, String safeIp, String date) {
        // Decide whether to sanitize the player name for logs
        boolean sanitize = configManager
                .getMainConfig()
                .logging
                .sanitizePlayerNames;

        String nameForLog = sanitize
                ? safeName
                : name;

        String logMessage = configManager.getMessagesConfig()
                .logging.violationEntry
                .replace("<date>", date)
                .replace("<player>", nameForLog)
                .replace("<permission>", permission)
                .replace("<ip>", safeIp);

        logger.info(logMessage);

        try {
            Files.createDirectories(dataFolder);
        } catch (IOException ignored) {
        }

        String fileName = configManager.getMainConfig().logging.violationsFile;
        Path logPath = dataFolder.resolve(fileName);

        try {
            Files.writeString(
                    logPath,
                    logMessage + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            String msg = configManager.getMessagesConfig()
                    .logging.fileWriteError
                    .replace("<error>", e.getMessage());
            logger.severe(msg);
        }
    }
}