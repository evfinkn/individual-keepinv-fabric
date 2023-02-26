package dev.evanfinken.individualkeepinv.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

// I wanted to extend ServerConfigList but couldn't easily extend ServerConfigEntry for KeepInvEntry
public class KeepInvList {
    public static final Logger LOGGER = LoggerFactory.getLogger("individual-keepinv");
    /** A map of <code>GameProfile</code>s to the <code>KeepInvEntry</code> for that player. */
    private final Map<GameProfile, Optional<Boolean>> map = new HashMap<>();

    public KeepInvList() {}

    /**
     * Gets the keep inventory preference of the player specified by <code>profile</code>.
     * <p>
     * This preference indicates if the user wants to keep their inventory when they die. The
     * <code>Optional</code> holds <code>true</code> if they do, <code>false</code> if they don't,
     * and is empty if they want to follow whatever the `keepInventory` gamerule is (the default).
     * 
     * @param profile The profile of the player to get the preference of.
     * @return The keep inventory preference of the player.
     */
    public Optional<Boolean> shouldKeepInventory(GameProfile profile) {
        if (!map.containsKey(profile)) {
            return Optional.empty();
        }
        return map.get(profile);
    }

    /**
     * Sets the keep inventory preference of the player.
     * <p>
     * If there is an entry for the player already, it is updated. Otherwise, an entry is added.
     * <p>
     * The preference indicates if the user wants to keep their inventory when they die. The
     * <code>Optional</code> should hold <code>true</code> if they do, <code>false</code> if they
     * don't, and be empty if they want to follow whatever the `keepInventory` gamerule is (the
     * default).
     * 
     * @param profile The profile of the player to set the preference of.
     * @param keepInventory The player's new preference.
     */
    public void setKeepInventory(GameProfile profile, Optional<Boolean> keepInventory) {
        map.put(profile, keepInventory);
    }

    /** Removes the entry entry for the specified profile. */
    public void remove(GameProfile profile) {
        map.remove(profile);
    }

    public JsonArray toJsonArray() {
        var jsonArray = new JsonArray();
        for (var entry : map.entrySet()) {
            GameProfile profile = entry.getKey();
            Optional<Boolean> keepInventory = entry.getValue();

            var jsonObject = new JsonObject();
            if (profile != null) {
                jsonObject.addProperty("uuid",
                        profile.getId() == null ? "" : profile.getId().toString());
                jsonObject.addProperty("name", profile.getName());
                jsonObject.addProperty("keepInventory", keepInventory.orElse(null));
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * Clears this list and fills it with the entries in <code>entries</code>.
     * 
     * @param entries A JSON array of objects representing players and their preferences.
     */
    public void loadFromJsonArray(JsonArray entries) {
        map.clear();
        for (JsonElement element : entries) {
            if (!element.isJsonObject()) {
                continue;
            }
            var playerJsonObject = element.getAsJsonObject();
            var profile = gameProfileFromJson(playerJsonObject);
            if (profile != null) {
                map.put(profile, keepInventoryFromJson(playerJsonObject));
            }
        }
    }

    /**
     * Attempts to create a <code>GameProfile</code> from a <code>JsonObject</code>.
     * 
     * @param json An object with the properties "uuid" and "name".
     * @return The created profile, or <code>null</code> if the info in <code>json</code> is
     *         invalid.
     */
    // This is copied from WhitelistEntry and OperatorEntry
    @Nullable
    private static GameProfile gameProfileFromJson(JsonObject json) {
        UUID uUID;
        if (!json.has("uuid") || !json.has("name")) {
            return null;
        }
        String string = json.get("uuid").getAsString();
        try {
            uUID = UUID.fromString(string);
        } catch (Throwable throwable) {
            return null;
        }
        return new GameProfile(uUID, json.get("name").getAsString());
    }

    /**
     * Attempts to get the keep inventory preference from a <code>JsonObject</code>.
     * 
     * @param json An object with the property "keepInventory".
     * @return The created optional, or <code>null</code> if the info in <code>json</code> is
     *         invalid.
     */
    private static Optional<Boolean> keepInventoryFromJson(JsonObject json) {
        if (json.has("keepInventory")) {
            return Optional.of(json.get("keepInventory").getAsBoolean());
        }
        return Optional.empty();
    }
}
