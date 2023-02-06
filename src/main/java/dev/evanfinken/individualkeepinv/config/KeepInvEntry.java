package dev.evanfinken.individualkeepinv.config;

import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

// I had wanted to extend ServerConfigEntry<GameProfile> but the getKey has default
// visibility (only accessible by classes in the same package) so I couldn't.
// Most of this code is copied from WhitelistEntry and OperatorEntry
public class KeepInvEntry {

    private final GameProfile profile;
    private Optional<Boolean> keepInventory;

    public KeepInvEntry(JsonObject json) {
        this(KeepInvEntry.profileFromJson(json),
                // can't use JsonHelper here because can't pass null for
                // `boolean defaultBoolean`
                json.has("keepInventory") ? json.get("keepInventory").getAsBoolean() : null);
    }

    public KeepInvEntry(GameProfile profile, @Nullable boolean keepInventory) {
        this(profile, Optional.ofNullable(keepInventory));
    }

    public KeepInvEntry(GameProfile profile, Optional<Boolean> keepInventory) {
        this.profile = profile;
        this.keepInventory = keepInventory;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public Optional<Boolean> shouldKeepInventory() {
        return keepInventory;
    }

    public void setKeepInventory(Optional<Boolean> keepInventory) {
        this.keepInventory = keepInventory;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        if (this.profile != null) {
            jsonObject.addProperty("uuid",
                    profile.getId() == null ? "" : profile.getId().toString());
            jsonObject.addProperty("name", profile.getName());
            jsonObject.addProperty("keepInventory", keepInventory.orElse(null));
        }
        return jsonObject;
    }

    // This is copied from WhitelistEntry and OperatorEntry
    @Nullable
    private static GameProfile profileFromJson(JsonObject json) {
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
}
