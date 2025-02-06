package uz.alex2276564.permguard.utils;

import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.permguard.task.Runner;

import java.net.HttpURLConnection;

public class UpdateChecker {
    private final JavaPlugin plugin;
    private final String githubRepo;
    private final Runner runner;

    public UpdateChecker(JavaPlugin plugin, String githubRepo, Runner runner) {
        this.plugin = plugin;
        this.githubRepo = githubRepo;
        this.runner = runner;
    }

    public void checkForUpdates() {
        runner.runAsync(() -> {
            try {
                String latestVersion = getLatestVersion();
                if (!latestVersion.equals(plugin.getDescription().getVersion())) {
                    plugin.getLogger().info("");
                    plugin.getLogger().info("New version available: " + latestVersion);
                    plugin.getLogger().info("You are running version: " + plugin.getDescription().getVersion());
                    plugin.getLogger().info("Download the latest version from: https://github.com/" + githubRepo + "/releases");
                    plugin.getLogger().info("");
                } else {
                    plugin.getLogger().info("You are running the latest version of " + plugin.getDescription().getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String getLatestVersion() throws Exception {
        String apiUrl = "https://api.github.com/repos/" + githubRepo + "/releases/latest";
        HttpUtils.HttpResponse response = HttpUtils.getResponse(apiUrl, "MinecraftPlugin");

        if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
            JsonObject jsonObject = response.getJsonBody();
            return jsonObject.get("tag_name").getAsString();
        } else {
            throw new Exception("GitHub API returned HTTP " + response.getResponseCode());
        }
    }
}