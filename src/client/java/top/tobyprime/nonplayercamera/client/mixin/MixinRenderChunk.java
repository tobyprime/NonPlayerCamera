package top.tobyprime.nonplayercamera.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public class MixinRenderChunk{
    @Shadow private AABB bb;
    private Vec3 camera;

    @ModifyVariable(method = "<init>", at = @At("CTOR_HEAD"), ordinal = 0, argsOnly = true)
    private ChunkRenderDispatcher modifyInitVariable(ChunkRenderDispatcher value) {
        this.camera = value.getCameraPosition();
        return value;
    }

    @Inject(method = "getDistToPlayerSqr", at=@At("RETURN"), cancellable = true)
    private void injectGetDistToPlayerSqr(CallbackInfoReturnable<Double> cir) {
        double d = bb.minX + (double)8.0F - camera.x;
        double e = bb.minY + (double)8.0F - camera.y;
        double f = bb.minZ + (double)8.0F - camera.z;
        cir.setReturnValue(d*d+e*e+f*f);
    }
}
