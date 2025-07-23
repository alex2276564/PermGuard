package uz.alex2276564.permguard.config.data;

public record ConfigData(
        GeneralConfig general,
        TelegramConfig telegram,
        PermissionConfig permissions
) {}
