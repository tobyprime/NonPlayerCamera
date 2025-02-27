package top.tobyprime.nonplayercamera.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.render.RenderingContext;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "render", at=@At("TAIL"))
    private void injectRenderAtTail(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        RenderingManager.begin(new RenderingContext());
    }

    @Inject(method = "render", at=@At("RETURN"))
    private void injectRenderAtReturn(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        RenderingManager.end();
    }
}
