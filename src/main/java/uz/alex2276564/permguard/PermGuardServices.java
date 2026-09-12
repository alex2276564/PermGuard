package uz.alex2276564.permguard;

import uz.alex2276564.permguard.config.PermGuardConfigManager;
import uz.alex2276564.permguard.telegram.TelegramNotifier;
import uz.alex2276564.permguard.utils.adventure.MessageManager;
import uz.alex2276564.permguard.utils.runner.Runner;

import java.util.logging.Logger;

/**
 * Lightweight service container intended for command handlers only.
 * <p>
 * This record groups together the core services that are commonly needed
 * by multiple commands, so we don't have to pass 4-5 constructor parameters
 * every time.
 * <p>
 * IMPORTANT:
 * - Use this ONLY for commands and command-related classes.
 * - For listeners, utilities and other components prefer explicit
 * dependencies in constructors (no "god" service containers).
 */
public record PermGuardServices(
        Runner runner,
        PermGuardConfigManager configManager,
        MessageManager messageManager,
        TelegramNotifier telegramNotifier,
        Logger logger
) {
}