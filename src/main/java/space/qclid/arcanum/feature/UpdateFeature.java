package space.qclid.arcanum.feature;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static space.qclid.arcanum.util.TextUtil.*;

public class UpdateFeature {

    private final JavaPlugin plugin;
    private File pendingUpdateFile = null;
    private CompletableFuture<Void> pendingDownload = null;

    /** GitHub repository whose latest release provides the update. */
    private static final String GITHUB_REPO = "Skullmc1/Arcanum";
    private static final String LATEST_RELEASE_URL = "https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest";
    private static final int    PROGRESS_INTERVAL = 10;
    private static final int    BAR_WIDTH = 20;

    public UpdateFeature(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Dev servers set -Darcanum.noupdate=true so a local build is never replaced by a release. */
    private static boolean updatesDisabled() {
        return Boolean.getBoolean("arcanum.noupdate");
    }

    public void checkForUpdates(CommandSourceStack source, boolean quiet) {
        if (updatesDisabled()) {
            if (!quiet) tell(source, C_GOLD + toSmallCaps("[Arcanum] Updates are disabled on this server."));
            return;
        }
        if (pendingUpdateFile != null) {
            tell(source, C_GOLD + toSmallCaps("[Arcanum] Update already downloaded — will apply on next restart."));
            return;
        }
        if (pendingDownload != null) {
            tell(source, C_GOLD + toSmallCaps("[Arcanum] A download is already in progress!"));
            return;
        }

        tell(source, C_GOLD + toSmallCaps("[Arcanum] Checking for updates…"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(LATEST_RELEASE_URL))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Arcanum-Updater")
            .GET().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
            if (response.statusCode() == 404) {
                tell(source, C_GOLD + toSmallCaps("[Arcanum] No release has been published yet."));
                return;
            }
            if (response.statusCode() != 200) {
                tell(source, C_RED + toSmallCaps("[Arcanum] GitHub returned status " + response.statusCode()));
                return;
            }
            try {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String latestVersion = json.get("tag_name").getAsString().replaceFirst("^[vV]", "");
                String downloadUrl   = findJarAsset(json);
                if (downloadUrl == null) {
                    tell(source, C_RED + toSmallCaps("[Arcanum] Latest release has no plugin jar attached."));
                    return;
                }

                if (!isNewer(latestVersion, plugin.getDescription().getVersion())) {
                    tell(source, C_GREEN + toSmallCaps("[Arcanum] You're up to date! (v" + plugin.getDescription().getVersion() + ")"));
                    return;
                }

                tell(source, C_GOLD + toSmallCaps("[Arcanum] New version v" + latestVersion + " found! Downloading…"));
                startDownload(source, downloadUrl, latestVersion);

            } catch (Exception e) {
                tell(source, C_RED + toSmallCaps("[Arcanum] Update check failed: " + e.getMessage()));
            }
        });
    }

    private void startDownload(CommandSourceStack source, String url, String version) {
        pendingDownload = CompletableFuture.runAsync(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("User-Agent", "Arcanum-Updater");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                long totalBytes = conn.getContentLengthLong();

                File tempFile = File.createTempFile("arcanum-update", ".jar");
                tempFile.deleteOnExit();

                byte[] buffer = new byte[8192];
                int read;
                long totalRead = 0;
                AtomicLong progress = new AtomicLong(0);

                var progressTask = plugin.getServer().getGlobalRegionScheduler()
                    .runAtFixedRate(plugin, task -> updateProgress(source, version, progress.get(), totalBytes), 1L, PROGRESS_INTERVAL);

                try (InputStream in = conn.getInputStream(); OutputStream out = new FileOutputStream(tempFile)) {
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        totalRead += read;
                        progress.set(totalRead);
                    }
                }

                progressTask.cancel();

                if (!looksLikePluginJar(tempFile)) {
                    tempFile.delete();
                    throw new IOException("downloaded file is not a valid plugin jar");
                }

                this.pendingUpdateFile = tempFile;

                plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
                    tell(source, C_GREEN + toSmallCaps("[Arcanum] Download complete! v" + version + " will be applied on next restart."));
                    plugin.getLogger().info("Update v" + version + " downloaded successfully.");
                });

                this.pendingDownload = null;

            } catch (Exception e) {
                tell(source, C_RED + toSmallCaps("[Arcanum] Download failed: " + e.getMessage()));
                plugin.getLogger().severe("Update download failed: " + e.getMessage());
                this.pendingDownload = null;
            }
        });
    }

    /** Picks the plugin jar (not sources/javadoc) from a GitHub release payload. */
    private static String findJarAsset(JsonObject release) {
        if (!release.has("assets")) return null;
        for (var el : release.getAsJsonArray("assets")) {
            JsonObject asset = el.getAsJsonObject();
            String name = asset.get("name").getAsString();
            if (name.endsWith(".jar") && !name.endsWith("-sources.jar") && !name.endsWith("-javadoc.jar")) {
                return asset.get("browser_download_url").getAsString();
            }
        }
        return null;
    }

    private static boolean looksLikePluginJar(File file) {
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(file)) {
            return zip.getEntry("paper-plugin.yml") != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateProgress(CommandSourceStack source, String version, long downloaded, long total) {
        if (!(source != null && source.getSender() instanceof Player player)) return;

        String bar;
        String suffix;
        if (total > 0) {
            int pct = (int) (downloaded * 100 / Math.max(total, 1));
            bar = buildBar(pct);
            suffix = pct + "%";
        } else {
            bar = buildBar(0);
            suffix = formatSize(downloaded);
        }

        player.sendActionBar(MM.deserialize(C_GOLD + toSmallCaps("Downloading v" + version + " ") + bar + " " + C_ORANGE + suffix));
    }

    private static String buildBar(int pct) {
        int filled = pct * BAR_WIDTH / 100;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filled; i++) sb.append("■");
        for (int i = filled; i < BAR_WIDTH; i++) sb.append("□");
        return sb.toString();
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private static void tell(CommandSourceStack source, String msg) {
        if (source != null) source.getSender().sendMessage(MM.deserialize(msg));
    }

    public void onShutdown() {
        if (pendingDownload != null) pendingDownload.join();
        if (pendingUpdateFile == null || !pendingUpdateFile.exists()) return;
        try {
            File currentJar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            plugin.getLogger().info(toSmallCaps("Attempting direct replacement of ") + currentJar.getName());
            try {
                Files.copy(pendingUpdateFile.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
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
            String[] latestParts = latest.split("[^\\d]");
            String[] currentParts = current.split("[^\\d]");
            int maxLen = Math.max(latestParts.length, currentParts.length);
            for (int i = 0; i < maxLen; i++) {
                int l = i < latestParts.length && !latestParts[i].isEmpty() ? Integer.parseInt(latestParts[i]) : 0;
                int c = i < currentParts.length && !currentParts[i].isEmpty() ? Integer.parseInt(currentParts[i]) : 0;
                if (l != c) return l > c;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not parse versions: latest=" + latest + ", current=" + current);
        }
        return false;
    }
}
