package dev.evanfinken.individualkeepinv.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.mojang.authlib.GameProfile;
import dev.evanfinken.individualkeepinv.IndividualKeepInv;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    public abstract GameProfile getGameProfile();

    public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Intercepts the call in {@link PlayerEntity#dropInventory} to get the "keepInventory"
     * gamerule.
     */
    @Redirect(method = "dropInventory", at = @At(value = "INVOKE",
            target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean onDropInventory(GameRules rules, GameRules.Key<GameRules.BooleanRule> key) {
        return IndividualKeepInv.interceptGetKeepInventory(getGameProfile(), rules, key);
    }

    /**
     * Intercepts the call in {@link PlayerEntity#getExperienceToDrop} to get the "keepInventory" gamerule.
     */
    @Redirect(method = "getExperienceToDrop", at = @At(value = "INVOKE",
            target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean onGetExperienceToDrop(GameRules rules, GameRules.Key<GameRules.BooleanRule> key) {
        return IndividualKeepInv.interceptGetKeepInventory(getGameProfile(), rules, key);
    }
}
