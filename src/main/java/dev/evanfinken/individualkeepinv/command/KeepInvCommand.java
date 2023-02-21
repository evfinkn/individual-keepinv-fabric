package dev.evanfinken.individualkeepinv.command;

import static net.minecraft.server.command.CommandManager.literal;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.evanfinken.individualkeepinv.IndividualKeepInv;
import dev.evanfinken.individualkeepinv.config.Config;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class KeepInvCommand {
    public static final Logger LOGGER = LoggerFactory.getLogger("individual-keepinv");

    /**
     * Gets the <code>GameProfile</code> of the player that executed the command
     * 
     * @param context The context containing the source of the command.
     * @return The <code>GameProfile</code> of the player that executed the command.
     */
    public static GameProfile getGameProfileFromContext(
            CommandContext<ServerCommandSource> context) {
        return context.getSource().getPlayer().getGameProfile();
    }

    /**
     * Gets the <code>String</code> for the <code>Optional</code>'s value, or "the default" if
     * empty.
     * <p>
     * In other words, returns <code>"true"</code> if the value in <code>optional</code> is
     * <code>true</code>, "false" if the value in <code>optional</code> is <code>false</code>, and
     * "the default" if <code>optional</code> is empty.
     * 
     * @param optional The <code>Optional</code> to get a string for.
     * @return The string of the value in <code>optional</code>.
     */
    public static String optionalToString(Optional<Boolean> optional) {
        return optional.isEmpty() ? "the default" : String.valueOf(optional.get());
    }

    /**
     * Sends a message to the source of a command.
     * 
     * @param context The context containing the source of the command.
     * @param message The message to send.
     */
    public static void sendMessage(CommandContext<ServerCommandSource> context, String message) {
        context.getSource().sendMessage(Text.literal(message));
    }

    /**
     * Registers the <code>/keepinv</code> command.
     * 
     * @param dispatcher The dispatcher to register the command to.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("keepinv")
                .requires(source -> hasKeepInvUserPermissionLevel(source)
                        || hasKeepInvOpPermissionLevel(source))
                .then(literal("info").executes(context -> executeGetInfo(context)))
                .then(literal("set")
                        .then(literal("true")
                                .executes(context -> executeSetKeepInventory(context, true)))
                        .then(literal("false")
                                .executes(context -> executeSetKeepInventory(context, false)))
                        .then(literal("default")
                                .executes(context -> executeSetKeepInventory(context, null))))
                .then(literal("on").requires(source -> hasKeepInvOpPermissionLevel(source))
                        .executes(context -> executeEnable(context)))
                .then(literal("off").requires(source -> hasKeepInvOpPermissionLevel(source))
                        .executes(context -> executeDisable(context)))
                .then(literal("reload").requires(source -> hasKeepInvOpPermissionLevel(source))
                        .executes(context -> executeReload(context))));
    }

    /**
     * Returns whether the source of the command has permission to run the mod's user level
     * commands.
     * 
     * @param source The source of the command.
     * @return Whether the source of the command has permission to run the mod's user level
     *         commands.
     */
    public static boolean hasKeepInvUserPermissionLevel(ServerCommandSource source) {
        return source.hasPermissionLevel(IndividualKeepInv.CONFIG.getUserPermissionLevel());
    }

    /**
     * @param source The source of the command.
     * @return Whether the source of the command has permission to run the mod's operator level
     *         commands.
     */
    public static boolean hasKeepInvOpPermissionLevel(ServerCommandSource source) {
        return source.hasPermissionLevel(IndividualKeepInv.CONFIG.getOpPermissionLevel());
    }

    /**
     * Executes the <code>/keepinv info</code> command.
     * <p>
     * The command tells the player that executed it whether the mod is enabled, and if it is, their
     * keep inventory preference.
     * 
     * @param context The context containing the source of the command.
     * @return <code>1</code>.
     */
    public static int executeGetInfo(CommandContext<ServerCommandSource> context) {
        Config CONFIG = IndividualKeepInv.CONFIG;
        boolean enabled = CONFIG.isEnabled();
        var status = enabled ? "enabled" : "disabled";
        var message = String.format("Individual KeepInv is %s.", status);
        if (enabled) {
            GameProfile profile = getGameProfileFromContext(context);
            String preference = optionalToString(CONFIG.shouldKeepInventory(profile));
            message += String.format(" Your keep inventory preference is %s.", preference);
        }
        sendMessage(context, message);
        return 1;
    }

    /**
     * Executes the <code>/keepinv set</code> command.
     * <p>
     * The command sets the keep inventory preference of the player that executed the command to
     * <code>keepInventory</code>.
     * 
     * @param context The context containing the source of the command.
     * @param keepInventory The player's keep inventory preference.
     * @return <code>1</code>.
     */
    public static int executeSetKeepInventory(CommandContext<ServerCommandSource> context,
            @Nullable Boolean keepInventory) {
        GameProfile profile = getGameProfileFromContext(context);
        Optional<Boolean> keepInventoryOptional = Optional.ofNullable(keepInventory);
        IndividualKeepInv.CONFIG.setKeepInventory(profile, keepInventoryOptional);
        String preference = optionalToString(keepInventoryOptional);
        sendMessage(context, String.format("Successfully set your keep inventory preference to %s.",
                preference));
        return 1;
    }

    /**
     * Enables the mod.
     * 
     * @param context The context containing the source of the command.
     * @return <code>1</code>.
     */
    public static int executeEnable(CommandContext<ServerCommandSource> context) {
        IndividualKeepInv.CONFIG.enable();
        sendMessage(context, "Enabled Individual KeepInv.");
        return 1;
    }

    /**
     * Disables the mod.
     * 
     * @param context The context containing the source of the command.
     * @return <code>1</code>.
     */
    public static int executeDisable(CommandContext<ServerCommandSource> context) {
        IndividualKeepInv.CONFIG.disable();
        sendMessage(context, "Disabled Individual KeepInv.");
        return 1;
    }

    /**
     * Reloads the mod's config file.
     * 
     * @param context The context containing the source of the command.
     * @return <code>1</code>.
     */
    public static int executeReload(CommandContext<ServerCommandSource> context) {
        try {
            IndividualKeepInv.CONFIG.load();
            LOGGER.info("Successfully reloaded the config file.");
            sendMessage(context, "Successfully reloaded the config file.");
        } catch (Exception e) {
            LOGGER.warn("Failed to reload the config file.", e);
            sendMessage(context, "Failed to reload the config file.");
        }
        return 1;
    }
}
