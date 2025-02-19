package top.tobyprime.nonplayercamera.client.mixin;

import net.minecraft.client.Minecraft;
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
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        double d = bb.minX + (double)8.0F - camera.getPosition().x;
        double e = bb.minY + (double)8.0F - camera.getPosition().y;
        double f = bb.minZ + (double)8.0F - camera.getPosition().z;
        cir.setReturnValue(d*d+e*e+f*f);
    }
}
