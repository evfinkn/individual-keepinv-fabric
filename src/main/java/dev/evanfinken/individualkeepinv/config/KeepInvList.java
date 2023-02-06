package dev.evanfinken.individualkeepinv.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;

// I wanted to extend ServerConfigList but couldn't easily extend ServerConfigEntry
public class KeepInvList {
    private final Map<String, KeepInvEntry> map = new HashMap<>();

    public KeepInvList() {}

    public void add(KeepInvEntry entry) {
        map.put(entry.getProfile().toString(), entry);
    }

    public boolean contains(GameProfile profile) {
        return map.containsKey(profile.toString());
    }

    @Nullable
    public KeepInvEntry get(GameProfile profile) {
        return map.get(profile.toString());
    }

    public Optional<Boolean> shouldKeepInventory(GameProfile profile) {
        if (!contains(profile)) {
            return Optional.empty();
        }
        return get(profile).shouldKeepInventory();
    }

    public void setKeepInventory(GameProfile profile, Optional<Boolean> keepInventory) {
        if (contains(profile)) {
            get(profile).setKeepInventory(keepInventory);
        } else {
            add(new KeepInvEntry(profile, keepInventory));
        }
    }

    public void remove(GameProfile profile) {
        map.remove(profile.toString());
    }

    public Collection<KeepInvEntry> values() {
        return map.values();
    }

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
