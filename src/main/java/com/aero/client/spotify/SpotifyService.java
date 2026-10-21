package com.aero.client.spotify;

import com.aero.client.spotify.SpotifyState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SpotifyService {
    private static final Logger LOG = LoggerFactory.getLogger((String)"p3nis/spotify");
    private static final int MAX_RESTARTS = 5;
    private final Path artPath;
    private final Path scriptPath;
    private volatile SpotifyState state = SpotifyState.INACTIVE;
    private volatile boolean stopped = false;
    private volatile Process process = null;
    private volatile BufferedWriter writer = null;
    private final AtomicInteger restarts = new AtomicInteger(0);

    public SpotifyService() {
        Path cfg = FabricLoader.getInstance().getConfigDir().resolve("p3nis");
        this.artPath = cfg.resolve("spotify_art.img");
        this.scriptPath = cfg.resolve("spotify_bridge.ps1");
    }

    public SpotifyState state() {
        return this.state;
    }

    public Path artPath() {
        return this.artPath;
    }

    public void start() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            LOG.info("SpotifyService: only supported on Windows");
            return;
        }
        Thread t = new Thread(this::runBridge, "p3nis-spotify-bridge");
        t.setDaemon(true);
        t.start();
    }

    public synchronized void stop() {
        this.stopped = true;
        if (this.process != null) {
            this.process.destroy();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void runBridge() {
        while (!this.stopped && this.restarts.get() < 5) {
            try {
                this.extractScript();
                ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", this.scriptPath.toString(), this.artPath.toString());
                pb.redirectErrorStream(false);
                SpotifyService spotifyService = this;
                synchronized (spotifyService) {
                    if (this.stopped) {
                        return;
                    }
                    this.process = pb.start();
                    this.writer = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream(), StandardCharsets.UTF_8));
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.process.getInputStream(), StandardCharsets.UTF_8));){
                    String line;
                    while ((line = reader.readLine()) != null) {
                        this.parseLine(line.trim());
                    }
                }
                this.process.waitFor();
            }
            catch (Exception e) {
                LOG.warn("SpotifyService bridge error: {}", (Object)e.toString());
            }
            this.state = SpotifyState.INACTIVE;
            this.restarts.incrementAndGet();
        }
    }

    private void extractScript() throws Exception {
        Files.createDirectories(this.scriptPath.getParent(), new FileAttribute[0]);
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("assets/p3nis/spotify_bridge.ps1");){
            if (in == null) {
                throw new IllegalStateException("spotify_bridge.ps1 not found in resources");
            }
            Files.write(this.scriptPath, in.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private void parseLine(String line) {
        if (line.isEmpty() || !line.startsWith("{")) {
            return;
        }
        try {
            JsonObject j = JsonParser.parseString((String)line).getAsJsonObject();
            if (!j.has("active") || !j.get("active").getAsBoolean()) {
                this.state = SpotifyState.INACTIVE;
                return;
            }
            this.state = new SpotifyState(true, j.has("title") ? j.get("title").getAsString() : "", j.has("artist") ? j.get("artist").getAsString() : "", j.has("posMs") ? j.get("posMs").getAsLong() : 0L, j.has("durMs") ? j.get("durMs").getAsLong() : 0L, j.has("playing") && j.get("playing").getAsBoolean(), j.has("canSeek") && j.get("canSeek").getAsBoolean(), j.has("artV") ? j.get("artV").getAsInt() : 0, j.has("vol") ? j.get("vol").getAsInt() : -1, System.nanoTime());
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private synchronized void send(String command) {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(command);
            this.writer.newLine();
            this.writer.flush();
        }
        catch (Exception e) {
            LOG.warn("SpotifyService send error: {}", (Object)e.toString());
        }
    }

    public void next() {
        this.send("NEXT");
    }

    public void previous() {
        this.send("PREV");
    }

    public void togglePlay() {
        this.send("PLAYPAUSE");
    }

    public void seekTo(long ms) {
        this.send("SEEK " + Math.max(0L, ms));
    }

    public void setVolume(int pct) {
        this.send("VOLUME " + Math.max(0, Math.min(100, pct)));
    }
}

