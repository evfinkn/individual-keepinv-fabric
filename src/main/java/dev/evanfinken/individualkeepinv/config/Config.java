package dev.evanfinken.individualkeepinv.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.JsonHelper;

public class Config {
    public static final Logger LOGGER = LoggerFactory.getLogger("individual-keepinv");
    public static final Path DEFAULT_PATH = Path.of("config/individualKeepInventory.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private boolean enabled = false;
    /**
     * Permission level required for a user to set their own keep inventory preference.
     */
    private int userPermissionLevel = 0;
    private int opPermissionLevel = 3;
    private KeepInvList keepInvList = new KeepInvList();

    public Config() {
        this(DEFAULT_PATH);
    }

    public Config(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
        try {
            save();
            LOGGER.info("Successfully enabled the mod.");
        } catch (IOException e) {
            LOGGER.warn("Failed to save the config after enabling the mod.", e);
        }
    }

    public void disable() {
        enabled = false;
        try {
            save();
            LOGGER.info("Successfully disabled the mod.");
        } catch (IOException e) {
            LOGGER.warn("Failed to save the config after disabling the mod.", e);
        }
    }

    public int getUserPermissionLevel() {
        return userPermissionLevel;
    }

    public int getOpPermissionLevel() {
        return opPermissionLevel;
    }

    public void setUserPermissionLevel(int userPermissionLevel) {
        this.userPermissionLevel = userPermissionLevel;
        try {
            save();
            LOGGER.info("Successfully set userPermissionLevel to {}.", userPermissionLevel);
        } catch (IOException e) {
            LOGGER.warn("Failed to save the config after setting userPermissionLevel to {}.",
                    userPermissionLevel, e);
        }
    }

    public Optional<Boolean> shouldKeepInventory(GameProfile profile) {
        return keepInvList.shouldKeepInventory(profile);
    }

    public void setKeepInventory(GameProfile profile, Optional<Boolean> keepInventory) {
        keepInvList.setKeepInventory(profile, keepInventory);
        try {
            save();
            LOGGER.info("Successfully set {}'s keep inventory preference to {}.", profile.getName(),
                    keepInventory);
        } catch (IOException e) {
            LOGGER.warn(
                    "Failed to save the config after setting {}'s keep inventory preference to {}.",
                    profile.getName(), keepInventory, e);
        }
    }

    public void save() throws IOException {
        var configJson = new JsonObject();
        var players = new JsonArray();
        keepInvList.values().stream().map(entry -> entry.toJsonObject()).forEach(players::add);
        configJson.addProperty("enabled", enabled);
        configJson.addProperty("userPermissionLevel", userPermissionLevel);
        configJson.addProperty("opPermissionLevel", opPermissionLevel);
        configJson.add("players", players);
        Files.writeString(path, GSON.toJson(configJson));
    }

    public void load() throws IOException {
        if (Files.notExists(path)) {
            return; // nothing to load
        }
        JsonObject configJson = GSON.fromJson(Files.readString(path), JsonObject.class);
        enabled = JsonHelper.getBoolean(configJson, "enabled", false);
        userPermissionLevel = JsonHelper.getInt(configJson, "userPermissionLevel", 0);
        opPermissionLevel = JsonHelper.getInt(configJson, "opPermissionLevel", 3);
        if (JsonHelper.hasArray(configJson, "profiles")) {
            keepInvList.loadFromJsonArray(configJson.get("players").getAsJsonArray());
        }
    }
}
