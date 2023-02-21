package dev.evanfinken.individualkeepinv.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.mojang.authlib.GameProfile;
import dev.evanfinken.individualkeepinv.IndividualKeepInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile,
            @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    /**
     * Intercepts the call in {@link ServerPlayerEntity#copyFrom} to get the "keepInventory"
     * gamerule.
     */
    @Redirect(method = "copyFrom", at = @At(value = "INVOKE",
            target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean onCopyFrom(GameRules rules, GameRules.Key<GameRules.BooleanRule> key) {
        return IndividualKeepInv.interceptGetKeepInventory(getGameProfile(), rules, key);
    }
}
