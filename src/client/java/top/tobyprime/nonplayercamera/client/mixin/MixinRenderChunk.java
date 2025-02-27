package top.tobyprime.nonplayercamera.client.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeRenderChunk;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public class MixinRenderChunk implements BridgeRenderChunk {
    @Shadow private AABB bb;
    @Unique
    private Camera camera;
    @Unique
    private long renderTime = 0;

    @ModifyVariable(method = "<init>", at = @At("CTOR_HEAD"), ordinal = 0, argsOnly = true)
    private ChunkRenderDispatcher modifyInitVariable(ChunkRenderDispatcher value) {
        this.camera = RenderingManager.getCurrentContext().camera;
        return value;
    }

    @Inject(method = "getDistToPlayerSqr", at=@At("HEAD"), cancellable = true)
    private void injectGetDistToPlayerSqr(CallbackInfoReturnable<Double> cir) {
        double d = bb.minX + (double)8.0F - camera.getPosition().x;
        double e = bb.minY + (double)8.0F - camera.getPosition().y;
        double f = bb.minZ + (double)8.0F - camera.getPosition().z;
        cir.setReturnValue(d*d+e*e+f*f);
    }

    @Override
    public long getPreRenderTime() {
        return renderTime;
    }

    @Override
    public void setPreRenderTime(long preRenderTime) {
        renderTime=preRenderTime;
    }
}
