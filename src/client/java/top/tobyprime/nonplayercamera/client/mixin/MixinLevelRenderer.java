package top.tobyprime.nonplayercamera.client.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeLevelRenderer;
import top.tobyprime.nonplayercamera.client.render.NonPlayerLevelRenderer;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer implements BridgeLevelRenderer {
    @Shadow
    @Nullable
    private ClientLevel level;

    @Shadow
    @Nullable
    private ViewArea viewArea;

    @Shadow
    @Final
    private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum;

    @Shadow
    public int lastViewDistance;

    @Shadow
    @Nullable
    private ChunkRenderDispatcher chunkRenderDispatcher;

    public RenderTarget getRenderTarget() {
        return renderTarget;
    }

    public void setRenderTarget(RenderTarget renderTarget) {
        this.renderTarget = renderTarget;
    }

    @Unique
    RenderTarget renderTarget = null;

    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)V", at = @At("HEAD"))
    public void addParticle(ParticleOptions options, boolean force, boolean decreased, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfo ci) {
        if (!this.equals(Minecraft.getInstance().levelRenderer)) return;
        var nplr = NonPlayerLevelRenderer.nonPlayerLevelRendererMap.get(level.dimension());
        if (nplr == null || nplr.levelRenderer.equals(Minecraft.getInstance().levelRenderer)) return;
        nplr.levelRenderer.addParticle(options, force, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Inject(method = "allChanged", at = @At("HEAD"))
    void allChanged(CallbackInfo ci) {
        if (!this.equals(Minecraft.getInstance().levelRenderer)) {
            return;
        }
        for (var v : NonPlayerLevelRenderer.nonPlayerLevelRendererMap.values()) {
            v.levelRenderer.allChanged();
        }

    }

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    RenderTarget redirectGetMainRenderTargetInRenderLevel(Minecraft instance) {
        if (this.renderTarget != null) {
            return this.renderTarget;
        }
        return instance.getMainRenderTarget();
    }

    @Redirect(method = "initTransparency", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getWidth()I"))
    int redirectGetWidthInInitTransparency(Window instance) {
        if (this.renderTarget != null) {
            return this.renderTarget.width;
        }
        return instance.getWidth();
    }

    @Redirect(method = "initTransparency", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getHeight()I"))
    int redirectGetHeightInInitTransparency(Window instance) {
        if (this.renderTarget != null) {
            return this.renderTarget.height;
        }
        return instance.getHeight();
    }

    @Redirect(method = "initTransparency", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    RenderTarget redirectGetMainRenderTargetInInitTransparency(Minecraft instance) {
        if (this.renderTarget != null) {
            return this.renderTarget;
        }
        return instance.getMainRenderTarget();
    }

    @Inject(method = "setBlockDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V", at =@At("HEAD"))
    void setBlockDirty(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci){
        var nplr = NonPlayerLevelRenderer.nonPlayerLevelRendererMap.get(level.dimension());
        if (nplr == null || nplr.levelRenderer.equals(this)) return;

        nplr.levelRenderer.setBlockDirty(pos, oldState, newState);
    }
    @Inject(method = "setSectionDirtyWithNeighbors", at =@At("HEAD"))
    void setSectionDirtyWithNeighbors(int sectionX, int sectionY, int sectionZ, CallbackInfo ci){
        var nplr = NonPlayerLevelRenderer.nonPlayerLevelRendererMap.get(level.dimension());
        if (nplr == null || nplr.levelRenderer.equals(this)) return;

        nplr.levelRenderer.setSectionDirtyWithNeighbors(sectionX, sectionY, sectionZ);
    }
    @Inject(method = "setSectionDirty(III)V", at =@At("HEAD"))
    void setSectionDirty(int sectionX, int sectionY, int sectionZ, CallbackInfo ci){
        var nplr = NonPlayerLevelRenderer.nonPlayerLevelRendererMap.get(level.dimension());
        if (nplr == null || nplr.levelRenderer.equals(this)) return;

        nplr.levelRenderer.setSectionDirty(sectionX, sectionY, sectionZ);
    }
}
