package top.tobyprime.nonplayercamera.client.mixin;


import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeCamera;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeChunkCacheStorage;

@Mixin(ClientChunkCache.Storage.class)
public class MixinStorage implements BridgeChunkCacheStorage {


    @Unique
    public ResourceKey<Level> dimension = null;


    @Inject(method = "inRange", at = @At("HEAD"), cancellable = true)
    void isInRadius(int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        if (dimension == null) {
            return;
        }

        Set<Camera> activatedCameras = RenderingManager.activatedCameras.get(dimension);
    
        for (Camera camera : activatedCameras) {          
            var viewDistance = ((BridgeCamera) camera).getRenderingData().viewDistance;
            var xInRange =  SectionPos.blockToSectionCoord(camera.getBlockPosition().getX()) <= viewDistance;
            var zInRange = SectionPos.blockToSectionCoord(camera.getBlockPosition().getZ()) <= viewDistance;
            if (xInRange && zInRange) {
                cir.setReturnValue(true);
                return;
            }
        }

        cir.setReturnValue(false);
    }


    @Override
    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    @Override
    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }
}
