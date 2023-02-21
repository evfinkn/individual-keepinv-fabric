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

    /** The profile of the player this entry is for. */
    private final GameProfile profile;
    /**
     * The keep inventory preference of the player.
     * <p>
     * This preference indicates if the user wants to keep their inventory when they die. The
     * <code>Optional</code> holds <code>true</code> if they do, <code>false</code> if they don't,
     * and is empty if they want to follow whatever the <code>keepInventory</code> gamerule is (the
     * default).
     */
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

    /** Returns the <code>GameProfile</code> of the player this entry is for. */
    public GameProfile getProfile() {
        return profile;
    }

    /**
     * Gets the keep inventory preference of the player.
     * <p>
     * The preference indicates if the user wants to keep their inventory when they die. The
     * <code>Optional</code> should hold <code>true</code> if they do, <code>false</code> if they
     * don't, and be empty if they want to follow whatever the <code>keepInventory</code> gamerule
     * is (the default).
     * 
     * @return The keep inventory preference of the player.
     */
    public Optional<Boolean> shouldKeepInventory() {
        return keepInventory;
    }

    /**
     * Sets the keep inventory preference of the player.
     * <p>
     * The <code>Optional</code> holds <code>true</code> if they do, <code>false</code> if they
     * don't, and is empty if they want to follow whatever the <code>keepInventory</code> gamerule
     * is (the default).
     * 
     * @param keepInventory - The player's new preference.
     */
    public void setKeepInventory(Optional<Boolean> keepInventory) {
        this.keepInventory = keepInventory;
    }

    /**
     * Converts this entry to a <code>JsonObject</code>.
     * <p>
     * The object has the following properties:
     * <ol>
     * <li>uuid – The UUID of the player this entry is for.</li>
     * <li>name – The name of the player this entry is for.</li>
     * <li>keepInventory</li>
     * </ol>
     * 
     * @return The created object.
     */
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

    /**
     * Attempts to create a <code>GameProfile</code> from a <code>JsonObject</code>.
     * 
     * @param json An object with the properties "uuid" and "name".
     * @return The profile created from the info in <code>json<code>, or <code>null</code> if the
     *         info in <code>json</code> isn't valid.
     */
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
