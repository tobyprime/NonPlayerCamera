package top.tobyprime.nonplayercamera.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.tobyprime.nonplayercamera.client.common.SuperChunkCache;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeClientLevel;

import java.util.Objects;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public class MixinClientLevel implements BridgeClientLevel {
    @Shadow
    public ClientChunkCache chunkSource;

    @Unique
    public ParticleEngine levelParticleEngine;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void injectInit(ClientPacketListener connection, ClientLevel.ClientLevelData clientLevelData, ResourceKey dimension, Holder dimensionType, int viewDistance, int serverSimulationDistance, Supplier profiler, LevelRenderer levelRenderer, boolean isDebug, long biomeZoomSeed, CallbackInfo ci) {
        this.chunkSource = new SuperChunkCache((ClientLevel) (Object) this);
    }

    @Override
    public ParticleEngine getParticleEngine() {
        return Objects.requireNonNullElseGet(this.levelParticleEngine, () -> Minecraft.getInstance().particleEngine);
    }

    @Override
    public void setParticleEngine(ParticleEngine particleEngine) {
        levelParticleEngine = particleEngine;
    }
}
