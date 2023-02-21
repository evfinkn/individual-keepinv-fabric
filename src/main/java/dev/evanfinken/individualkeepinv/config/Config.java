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

    /** The path to the config file to load from and save to. */
    private final Path path;
    /**  */
    private boolean enabled = false;
    /** Permission level required for a user to set their own keep inventory preference. */
    private int userPermissionLevel = 0;
    /** Permission level required for a user to enable / disable and reload the mod. */
    private int opPermissionLevel = 3;
    /** The list storing each player's keep inventory preference. */
    private KeepInvList keepInvList = new KeepInvList();

    public Config() {
        this(DEFAULT_PATH);
    }

    public Config(Path path) {
        this.path = path;
    }

    /** Gets the <code>Path</code> to this config's file. */
    public Path getPath() {
        return path;
    }

    /** Returns whether the mod is enabled. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Enables the mod. */
    public void enable() {
        enabled = true;
        try {
            save();
            LOGGER.info("Successfully enabled the mod.");
        } catch (IOException e) {
            LOGGER.warn("Failed to save the config after enabling the mod.", e);
        }
    }

    /** Disables the mod. */
    public void disable() {
        enabled = false;
        try {
            save();
            LOGGER.info("Successfully disabled the mod.");
        } catch (IOException e) {
            LOGGER.warn("Failed to save the config after disabling the mod.", e);
        }
    }

    /** Gets the permission level required to run <code>/keepinv set</code>. */
    public int getUserPermissionLevel() {
        return userPermissionLevel;
    }

    /**
     * Gets the permission level required to run <code>/keepinv on</code>,
     * <code>/keepinv off</code>, and <code>/keepinv reload</code>.
     */
    public int getOpPermissionLevel() {
        return opPermissionLevel;
    }

    /** Sets the permission level required to run <code>/keepinv set</code>. */
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

    /**
     * Returns the <code>Optional</code> of the player's keep inventory preference.
     * <p>
     * The <code>Optional</code> either has a <code>Boolean</code> value representing the player's
     * preference, or if the <code>Optional</code> is empty, the player's preference is the default.
     * 
     * @param profile The player to get the preference of.
     * @return the <code>Optional</code> of the player's keep inventory preference.
     */
    public Optional<Boolean> shouldKeepInventory(GameProfile profile) {
        return keepInvList.shouldKeepInventory(profile);
    }

    /**
     * Sets the keep inventory preference of a player.
     * 
     * @param profile The <code>GameProfile</code> of the player.
     * @param keepInventory The player's new keep inventory preference.
     */
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

    /**
     * Saves this config to the file at <code>path</code>.
     * <p>
     * If a file at <code>path</code> exists, it is overwritten. Otherwise, one is created. The
     * config is saved as a JSON object containing the following properties:
     * <ol>
     * <li>enabled</li>
     * <li>userPermissionLevel</li>
     * <li>opPermissionLevel</li>
     * <li>players – An array of objects containing the name, UUID, and preference of each
     * player.</li>
     * </ol>
     * 
     * @throws IOException
     */
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

    /**
     * Loads the config file if it exists.
     * <p>
     * This sets <code>enabled</code>, <code>userPermissionLevel</code>,
     * <code>opPermissionLevel</code>, and adds an entry in <code>keepInvList</code> for each entry
     * in the "players" property of the JSON in the config.
     * 
     * @throws IOException
     */
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
