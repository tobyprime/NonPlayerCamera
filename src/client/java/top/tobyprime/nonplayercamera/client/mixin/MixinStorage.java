package top.tobyprime.nonplayercamera.client.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.tobyprime.nonplayercamera.client.common.NonPlayerCamera;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeClientChunkMap;
import top.tobyprime.nonplayercamera.client.render.NonPlayerLevelRenderer;

@Mixin(ClientChunkCache.Storage.class)
public class MixinStorage implements BridgeClientChunkMap {
    @Unique
    public NonPlayerLevelRenderer getNonPlayerLevelRenderer() {
        return nonPlayerLevelRenderer;
    }

    @Unique
    public void setNonPlayerLevelRenderer(NonPlayerLevelRenderer nonPlayerLevelRenderer) {
        this.nonPlayerLevelRenderer = nonPlayerLevelRenderer;
    }

    @Unique
    public NonPlayerLevelRenderer nonPlayerLevelRenderer = null;

    @Inject(method = "getChunk", at = @At("RETURN"), cancellable = true)
    void getChunk(int index, CallbackInfoReturnable<LevelChunk> cir) {
        if (nonPlayerLevelRenderer == null || cir.getReturnValue() != null) {
            return;
        }

        if (nonPlayerLevelRenderer.level.dimension() == Minecraft.getInstance().level.dimension()) {
            var mainChunkMap = Minecraft.getInstance().level.getChunkSource().storage;
            cir.setReturnValue(mainChunkMap.getChunk(index));
        }

    }


    @Inject(method = "inRange", at = @At("HEAD"), cancellable = true)
    void isInRadius(int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        if (nonPlayerLevelRenderer == null) {
            return;
        }

        for (NonPlayerCamera camera : nonPlayerLevelRenderer.activeCameras) {
            var cameraChunkPos = new ChunkPos(camera.getBlockPosition());
            if (Math.abs(chunkX - cameraChunkPos.x) <= camera.viewDistance && Math.abs(chunkZ - cameraChunkPos.z) <= camera.viewDistance) {
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }
}
