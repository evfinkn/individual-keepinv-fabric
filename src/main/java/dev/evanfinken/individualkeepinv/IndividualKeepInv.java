package dev.evanfinken.individualkeepinv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.authlib.GameProfile;
import dev.evanfinken.individualkeepinv.command.KeepInvCommand;
import dev.evanfinken.individualkeepinv.config.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.world.GameRules;

public class IndividualKeepInv implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("individual-keepinv");
	public static final Config CONFIG = new Config();

	/** Initializes the mod, loading the config and registering the command. */
	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Individual KeepInv");

		try {
			CONFIG.load();
			LOGGER.info("Successfully loaded config file.");
		} catch (Exception e) {
			LOGGER.warn("Failed loading the config file.", e);
		}

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> KeepInvCommand.register(dispatcher));
	}

	/**
	 * Intercepts requests for the "keepInventory" gamerule, returning the player's preference, or
	 * otherwise the requested gamerule.
	 * 
	 * @param profile The profile of the player to get the entry for.
	 * @param rules The world's gamerules.
	 * @param key The gamerule being asked for.
	 * @return If the gamerule being asked for is "keepInventory", returns whether a player should
	 *         keep their inventory when they die. If the keep inventory preference of the player is
	 *         non-<code>null</code>, their preference is returned. Otherwise, if their preference
	 *         is <code>null</code> the "keepInventory" gamerule is returned. If the gamerule being
	 *         asked for is any other gamerule, that gamerule is returned.
	 */
	public static boolean interceptGetKeepInventory(GameProfile profile, GameRules rules,
			GameRules.Key<GameRules.BooleanRule> key) {
		if (key.getName().equals("keepInventory") && CONFIG.isEnabled()) {
			return CONFIG.shouldKeepInventory(profile).orElse(rules.getBoolean(key));
		}
		return rules.getBoolean(key);
	}
}
