package space.qclid.dashboard.feature;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static space.qclid.dashboard.util.TextUtil.*;

/**
 * Handles checking for plugin updates from the remote API,
 * downloading the new jar, and replacing it on server shutdown.
 */
public class UpdateFeature {

    private final JavaPlugin plugin;
    private File pendingUpdateFile = null;

    private static final String UPDATE_URL = "https://www.qclid.space/api/plugin-version";

    public UpdateFeature(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates(CommandSourceStack source, boolean quiet) {
        if (pendingUpdateFile != null) return;

        String startMsg = "Checking for updates...";
        String failMsg  = "Updating failed, will try again next time server restarts.";

        if (source != null) source.getSender().sendMessage(MM.deserialize(C_GOLD + toSmallCaps("[Dashboard] " + startMsg)));
        else if (!quiet) plugin.getLogger().info(startMsg);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(UPDATE_URL)).GET().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
            if (response.statusCode() != 200) {
                if (source != null) source.getSender().sendMessage(MM.deserialize(C_RED + toSmallCaps("[Dashboard] " + failMsg)));
                else if (!quiet) plugin.getLogger().warning(failMsg);
                return;
            }
            try {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String latestVersion  = json.get("version").getAsString();
                String downloadUrl    = json.get("downloadUrl").getAsString();

                if (isNewer(latestVersion, plugin.getDescription().getVersion())) {
                    String msg = "New update found! Downloading v" + latestVersion;
                    if (source != null) source.getSender().sendMessage(MM.deserialize(C_GOLD + toSmallCaps("[Dashboard] " + msg)));
                    else plugin.getLogger().info(msg);
                    downloadAndPrepareUpdate(downloadUrl);
                } else {
                    String upToDateMsg = "Plugin is up to date!";
                    if (source != null) source.getSender().sendMessage(MM.deserialize(C_GREEN + toSmallCaps("[Dashboard] " + upToDateMsg)));
                    else if (!quiet) plugin.getLogger().info(upToDateMsg);
                }
            } catch (Exception e) {
                if (source != null) source.getSender().sendMessage(MM.deserialize(C_RED + toSmallCaps("[Dashboard] " + failMsg)));
                else if (!quiet) plugin.getLogger().warning(failMsg + " (" + e.getMessage() + ")");
            }
        });
    }

    private void downloadAndPrepareUpdate(String url) {
        try {
            File tempFile = File.createTempFile("dashboard-update", ".jar");
            tempFile.deleteOnExit();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(tempFile.toPath())).thenAccept(res -> {
                if (res.statusCode() == 200) {
                    this.pendingUpdateFile = tempFile;
                    String msg = C_GOLD + toSmallCaps("Update downloaded! Replacing file on server shutdown.");
                    Bukkit.broadcast(MM.deserialize(msg), "dashboard.admin");
                    plugin.getLogger().info(msg);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to download update: " + e.getMessage());
        }
    }

    /**
     * Call this from {@code onDisable} to replace the jar on shutdown.
     */
    public void onShutdown() {
        if (pendingUpdateFile == null || !pendingUpdateFile.exists()) return;
        try {
            File currentJar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            plugin.getLogger().info(toSmallCaps("Attempting direct replacement of ") + currentJar.getName());
            try {
                Files.copy(pendingUpdateFile.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                // Fallback for Windows file locking: use the update folder
                File updateFolder = plugin.getServer().getUpdateFolderFile();
                if (!updateFolder.exists()) updateFolder.mkdirs();
                Files.copy(pendingUpdateFile.toPath(), new File(updateFolder, currentJar.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().warning(toSmallCaps("Direct overwrite locked. Scheduled replacement via update folder."));
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to replace JAR: " + e.getMessage());
        }
    }

    private boolean isNewer(String latest, String current) {
        try {
            return Double.parseDouble(latest) > Double.parseDouble(current);
        } catch (Exception e) {
            return !latest.equalsIgnoreCase(current);
        }
    }
}
