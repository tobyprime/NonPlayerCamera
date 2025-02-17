package top.tobyprime.nonplayercamera.client.mixin;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import top.tobyprime.nonplayercamera.client.common.RenderingManager;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public class MixinRenderChunk{
    @Shadow private AABB bb;

    @Inject(method = "getDistToPlayerSqr", at=@At("RETURN"), cancellable = true)
    private void injectGetDistToPlayerSqr(CallbackInfoReturnable<Double> cir) {
        double d = bb.minX + (double)8.0F - RenderingManager.getCurrentCamera().getPosition().x;
        double e = bb.minY + (double)8.0F - RenderingManager.getCurrentCamera().getPosition().y;
        double f = bb.minZ + (double)8.0F - RenderingManager.getCurrentCamera().getPosition().z;
        cir.setReturnValue(d*d+e*e+f*f);
    }
}
