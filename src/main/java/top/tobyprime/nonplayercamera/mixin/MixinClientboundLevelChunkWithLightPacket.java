package top.tobyprime.nonplayercamera.mixin;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.tobyprime.nonplayercamera.mixin_bridge.BridgeClientboundLevelChunkWithLightPacket;

import java.util.BitSet;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public class MixinClientboundLevelChunkWithLightPacket implements BridgeClientboundLevelChunkWithLightPacket {
    @Unique
    public ResourceKey<Level> getLevelKey() {
        return LevelKey;
    }


    @Unique
    ResourceKey<Level> LevelKey;

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at =@At("CTOR_HEAD"))
    private void onInit(FriendlyByteBuf buf, CallbackInfo ci) {
        this.LevelKey  = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;Z)V", at =@At("TAIL"))
    private void onInit(LevelChunk levelChunk, LevelLightEngine levelLightEngine, BitSet bitSet, BitSet bitSet2, boolean trustEdges, CallbackInfo ci){
        LevelKey = levelChunk.getLevel().dimension();
    }

    @Inject(method = "write", at = @At("HEAD"))
    public void write(FriendlyByteBuf buf, CallbackInfo ci) {
        buf.writeResourceLocation(LevelKey.location());
    }
}
