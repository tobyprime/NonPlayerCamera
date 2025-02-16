package top.tobyprime.nonplayercamera.client.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeRenderChunk;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public class MixinRenderChunk implements BridgeRenderChunk {
    @Shadow private AABB bb;
    ClientLevel level;

    @Unique
    long preRenderTime = 0;

    @Unique
    public long getPreRenderTime() {
        return preRenderTime;
    }
    
    @Unique
    public void setPreRenderTime(long preRenderTime) {
        this.preRenderTime = preRenderTime;
    }

    @Inject(method = "<init>", at = @At("CTOR_HEAD"))
    public void init(ChunkRenderDispatcher chunkRenderDispatcher, int index, int x, int y, int z, CallbackInfo ci) {
        level = chunkRenderDispatcher.level;
    }

    @Inject(method = "getDistToPlayerSqr", at=@At("RETURN"), cancellable = true)
    private void getDistToPlayerSqr(CallbackInfoReturnable<Double> cir) {
        double d = bb.minX + (double)8.0F - RenderingManager.getCurrentCamera().getPosition().x;
        double e = bb.minY + (double)8.0F - RenderingManager.getCurrentCamera().getPosition().y;
        double f = bb.minZ + (double)8.0F - RenderingManager.getCurrentCamera().getPosition().z;
        cir.setReturnValue(d*d+e*e+f*f);
    }
}
