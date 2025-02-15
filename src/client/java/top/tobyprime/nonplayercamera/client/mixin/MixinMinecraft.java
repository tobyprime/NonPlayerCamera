package top.tobyprime.nonplayercamera.client.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.tobyprime.nonplayercamera.client.NonPlayerCameraModClientMain;
import top.tobyprime.nonplayercamera.client.common.NonPlayerLevelManager;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeLevelRenderer;
import top.tobyprime.nonplayercamera.client.render.NonPlayerLevelRenderer;

import static top.tobyprime.nonplayercamera.client.NonPlayerCameraModClientMain.needRender;
import static top.tobyprime.nonplayercamera.client.NonPlayerCameraModClientMain.testCamera;
import static top.tobyprime.nonplayercamera.client.render.TestBlockEntityRenderer.closestBlock;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow @Final public GameRenderer gameRenderer;

    @Shadow @Nullable public LocalPlayer player;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onConstruct(CallbackInfo ci) {
        NonPlayerLevelRenderer.secondaryRenderBuffers = new RenderBuffers();
        NonPlayerCameraModClientMain.testFrameBuffer = new TextureTarget(1000,1000,true,false);

    }
    @Inject(method = "setLevel", at = @At(value = "RETURN"))
    public void onSetLevel(ClientLevel levelClient, CallbackInfo ci) {
        var recorded = NonPlayerLevelManager.getLevel(levelClient.dimension());
        if (levelClient.equals(recorded)) {
            return;
        }
        if (recorded != null) {
            NonPlayerLevelManager.clientLevelMap.remove(levelClient.dimension());
        }

        NonPlayerLevelManager.clientLevelMap.put(levelClient.dimension(), levelClient);



        var renderer = NonPlayerLevelRenderer.nonPlayerLevelRendererMap.get(levelClient.dimension());
        if (renderer != null) {
            renderer.setLevel(levelClient);
        }
    }
    @Inject(method = "getMainRenderTarget", at = @At("HEAD"), cancellable = true)
    public void getMainRenderTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (NonPlayerLevelRenderer.isNonPlayerRendering() && ( (BridgeLevelRenderer)NonPlayerLevelRenderer.currentNonPlayerRendering.levelRenderer).getRenderTarget()!=null) {
            var r = (BridgeLevelRenderer)NonPlayerLevelRenderer.currentNonPlayerRendering.levelRenderer;

            cir.setReturnValue(r.getRenderTarget());
        }
    }


    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V", shift = At.Shift.AFTER))
    public void render(boolean tick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.noRender || mc.level == null) {
            return;
        }
        if (!tick) {
            return;
        }
        if (!needRender) {
            return;
        }
        if (NonPlayerCameraModClientMain.testCamera==null){
            return;
        }
        NonPlayerCameraModClientMain.testCamera.setRotation(mc.gameRenderer.mainCamera.getYRot(),mc.gameRenderer.mainCamera.getXRot());
        if (closestBlock == null) {
            return;
        }
        if (NonPlayerCameraModClientMain.testFrameBuffer.width != Minecraft.getInstance().mainRenderTarget.width) {
            NonPlayerCameraModClientMain.testFrameBuffer = new TextureTarget( Minecraft.getInstance().mainRenderTarget.width,Minecraft.getInstance().mainRenderTarget.height,true,false);
        }
        var offset = player.position().subtract(Vec3.atCenterOf(closestBlock.getBlockPos()));

        var preTestCamera = NonPlayerCameraModClientMain.testCamera.getPosition();
        testCamera.setPosition(preTestCamera.add(offset));
        testCamera.levelRenderer.levelRenderer.tick();

        testCamera.levelRenderer.levelRenderer.needsUpdate();
        testCamera.levelRenderer.draw(NonPlayerCameraModClientMain.testCamera, NonPlayerCameraModClientMain.testFrameBuffer);
        testCamera.setPosition(preTestCamera);


    }
}
