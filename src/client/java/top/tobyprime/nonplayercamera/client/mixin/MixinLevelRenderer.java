package top.tobyprime.nonplayercamera.client.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.tobyprime.nonplayercamera.client.common.SuperCamera;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeLevelRenderer;
import top.tobyprime.nonplayercamera.client.render.LevelRendererUtils;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer implements BridgeLevelRenderer {
    @Shadow @Nullable private ClientLevel level;
    @Shadow @Nullable private ViewArea viewArea;
    @Shadow public int lastViewDistance;
    @Shadow public ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum;
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
