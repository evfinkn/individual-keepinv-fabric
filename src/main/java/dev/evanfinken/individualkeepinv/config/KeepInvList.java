package dev.evanfinken.individualkeepinv.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;

// I wanted to extend ServerConfigList but couldn't easily extend ServerConfigEntry for KeepInvEntry
public class KeepInvList {
    /** A map of <code>GameProfile</code>s to the <code>KeepInvEntry</code> for that player. */
    private final Map<String, KeepInvEntry> map = new HashMap<>();

    public KeepInvList() {}

    /** Adds an entry to this list. */
    public void add(KeepInvEntry entry) {
        map.put(entry.getProfile().toString(), entry);
    }

    /** Returns whether this list has an entry for <code>profile</code>. */
    public boolean contains(GameProfile profile) {
        return map.containsKey(profile.toString());
    }

    /** Gets the entry for <code>profile</code>, or <code>null</code> if there's no entry. */
    @Nullable
    public KeepInvEntry get(GameProfile profile) {
        return map.get(profile.toString());
    }

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
        if (!contains(profile)) {
            return Optional.empty();
        }
        return get(profile).shouldKeepInventory();
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
        if (contains(profile)) {
            get(profile).setKeepInventory(keepInventory);
        } else {
            add(new KeepInvEntry(profile, keepInventory));
        }
    }

    /** Removes the entry entry for the specified profile. */
    public void remove(GameProfile profile) {
        map.remove(profile.toString());
    }

    /** Returns the entries of this list. */
    public Collection<KeepInvEntry> values() {
        return map.values();
    }

    /**
     * Clears this list and fills it with the entries in <code>entries</code>.
     * 
     * @param entries A JSON array of JSON objects, each representing a <code>KeepInvEntry</code>.
     * @see KeepInvEntry
     */
    public void loadFromJsonArray(JsonArray entries) {
        map.clear();
        for (JsonElement playerJsonElement : entries) {
            if (!playerJsonElement.isJsonObject()) {
                continue;
            }
            var playerJsonObject = playerJsonElement.getAsJsonObject();
            var keepInvListEntry = new KeepInvEntry(playerJsonObject);
            if (keepInvListEntry.getProfile() != null) {
                map.put(keepInvListEntry.getProfile().toString(), keepInvListEntry);
            }
        }
    }
}
