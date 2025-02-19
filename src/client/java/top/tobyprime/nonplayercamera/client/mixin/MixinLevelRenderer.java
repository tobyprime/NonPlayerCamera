package top.tobyprime.nonplayercamera.client.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.tobyprime.nonplayercamera.client.common.SuperCamera;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeLevelRenderer;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer implements BridgeLevelRenderer {
    @Unique
    public SuperCamera camera;

    @Redirect(method = "initTransparency", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget getMainRenderTarget(Minecraft minecraft) {
        if (camera == null) {
            return minecraft.getMainRenderTarget();
        }
        return camera.target;
    }

    @Override
    public void setCamera(SuperCamera camera) {
        this.camera = camera;
    }

    @Override
    public SuperCamera getCamera() {
return this.camera;
    }
}
