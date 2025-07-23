package uz.alex2276564.permguard.config.defaults;

public final class ConfigDefaults {
    // General settings defaults
    public static final boolean SHUTDOWN_ON_DISABLE = true;
    public static final boolean CONFIG_DEBUG = false;

    // Telegram defaults
    public static final boolean TELEGRAM_ENABLED = false;
    public static final String BOT_TOKEN = "your_bot_token_here";
    public static final String CHAT_IDS = "123456789,987654321";
    public static final int MAX_RETRIES = 0;
    public static final long RETRY_DELAY = 1100L;
    public static final String MESSAGE =
            "⚠️ Security Alert!\\n\\nPlayer %player% tried to join with restricted permission %permission% and was kicked\\n\\n📍 Details:\\n👤 Player: %player%\\n🔒 Permission: %permission%\\n🌐 IP: %ip%\\n🗺️ Country: %country%\\n⏰ Time: %date% (Server time)\\n\\n❗If this wasn't authorized by you, please take immediate action to secure your server.";

    private ConfigDefaults() {}
}
