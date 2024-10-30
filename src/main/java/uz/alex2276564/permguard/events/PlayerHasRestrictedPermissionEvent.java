package uz.alex2276564.permguard.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerHasRestrictedPermissionEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancel = false;
    @Getter
    private String permission;
    @Getter
    private String cmd;
    @Getter
    private boolean log;
    @Getter
    private String kickMessage;

    public PlayerHasRestrictedPermissionEvent(@NotNull Player player, @NotNull String permission, @NotNull String cmd, boolean log, @NotNull String kickMessage) {
        super(player);
        this.permission = permission;
        this.cmd = cmd;
        this.log = log;
        this.kickMessage = kickMessage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
