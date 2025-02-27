package top.tobyprime.nonplayercamera.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.tobyprime.nonplayercamera.client.common.LevelManager;
import top.tobyprime.nonplayercamera.client.common.SuperCamera;
import top.tobyprime.nonplayercamera.client.common.SuperChunkCache;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeClientLevel;

import java.util.Objects;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel implements BridgeClientLevel {

    @Shadow
    @Mutable
    public ClientChunkCache chunkSource;
    @Unique
    public ParticleEngine levelParticleEngine;

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/ClientLevel;chunkSource:Lnet/minecraft/client/multiplayer/ClientChunkCache;"))
    public void setChunkSource(ClientLevel instance, ClientChunkCache value) {
        this.chunkSource = new SuperChunkCache(instance);
    }


    @Inject(method = "setBlocksDirty", at = @At("HEAD"))
    void setBlockDirty(BlockPos blockPos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        var dimension = ((ClientLevel) (Object) this).dimension();
        for (var camera : LevelManager.getCamerasInDimension(dimension)) {
            camera.renderer.setBlockDirty(blockPos, oldState, newState);
        }
        if (dimension == Minecraft.getInstance().level.dimension()) {
            Minecraft.getInstance().levelRenderer.setBlockDirty(blockPos, oldState, newState);
        }
    }

    @Inject(method = "setSectionDirtyWithNeighbors", at = @At("HEAD"))
    void setSectionDirtyWithNeighbors(int sectionX, int sectionY, int sectionZ, CallbackInfo ci) {
        var dimension = ((ClientLevel) (Object) this).dimension();
        for (var camera : LevelManager.getCamerasInDimension(dimension)) {
            camera.renderer.setSectionDirtyWithNeighbors(sectionX, sectionY, sectionZ);
        }
        if (dimension == Minecraft.getInstance().level.dimension()) {
            Minecraft.getInstance().levelRenderer.setSectionDirtyWithNeighbors(sectionX, sectionY, sectionZ);
        }
    }


    /// Begin impl for `BridgeClientLevel`


    @Override
    public ParticleEngine getParticleEngine() {
        return Objects.requireNonNullElseGet(this.levelParticleEngine, () -> Minecraft.getInstance().particleEngine);
    }

    @Override
    public void setParticleEngine(ParticleEngine particleEngine) {
        levelParticleEngine = particleEngine;
    }

}
