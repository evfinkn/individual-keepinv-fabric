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

	public static boolean interceptGetKeepInventory(GameProfile profile, GameRules rules,
			GameRules.Key<GameRules.BooleanRule> key) {
		if (key.getName().equals("keepInventory") && CONFIG.isEnabled()) {
			return CONFIG.shouldKeepInventory(profile).orElse(rules.getBoolean(key));
		}
		return rules.getBoolean(key);
	}
}
