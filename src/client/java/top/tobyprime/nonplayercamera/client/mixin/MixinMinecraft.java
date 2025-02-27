package top.tobyprime.nonplayercamera.client.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.tobyprime.nonplayercamera.client.NonPlayerCameraModClientMain;
import top.tobyprime.nonplayercamera.client.common.LevelManager;
import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.common.SuperCamera;
import top.tobyprime.nonplayercamera.client.common.SuperChunkCache;
import top.tobyprime.nonplayercamera.client.render.SuperGameRenderer;


import static top.tobyprime.nonplayercamera.client.NonPlayerCameraModClientMain.needRender;
import static top.tobyprime.nonplayercamera.client.NonPlayerCameraModClientMain.testCamera;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow
    @Final
    public GameRenderer gameRenderer;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    private float pausePartialTick;

    @Shadow
    @Nullable
    public ClientLevel level;


    @Shadow
    public abstract RenderTarget getMainRenderTarget();

    // for test
    @Inject(method = "<init>", at = @At("TAIL"))
    public void injectInitAtTail(CallbackInfo ci) {
        NonPlayerCameraModClientMain.testFrameBuffer = new TextureTarget(1000, 1000, true, false);
    }

    @Inject(method = "setLevel", at = @At(value = "HEAD"))
    public void injectSetLevelAtHead(ClientLevel levelClient, CallbackInfo ci) {
        ((SuperChunkCache) levelClient.getChunkSource()).storages.remove(this.gameRenderer.mainCamera);
    }

    @Inject(method = "setLevel", at = @At(value = "RETURN"))
    public void injectSetLevelAtTail(ClientLevel levelClient, CallbackInfo ci) {
        Validate.isTrue(!RenderingManager.isEnvModified());
        var dimension = levelClient.dimension();

        var existingLevel = LevelManager.get(dimension);
        // todo: access widen
        if (existingLevel != null) {
            levelClient.chunkSource = existingLevel.chunkSource;
        }

        LevelManager.levelMap.put(dimension, levelClient);
        ((SuperChunkCache) levelClient.getChunkSource()).onMainCameraUpdated(this.gameRenderer.mainCamera);
    }

    // for test
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V", shift = At.Shift.AFTER))
    public void injectRender(boolean tick, CallbackInfo ci) {
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
        if (NonPlayerCameraModClientMain.testCamera == null) {
            var camera = new SuperCamera(new ResourceLocation("tst","test"));
            camera.setDimension(Minecraft.getInstance().player.getLevel().dimension());
            camera.target = NonPlayerCameraModClientMain.testFrameBuffer;
            camera.enable();
            testCamera = camera;
        }

        if (mc.getMainRenderTarget().width != NonPlayerCameraModClientMain.testFrameBuffer.width) {
            NonPlayerCameraModClientMain.testFrameBuffer = new TextureTarget(mc.getMainRenderTarget().width,mc.getMainRenderTarget().height, true, false);
            ((SuperCamera) NonPlayerCameraModClientMain.testCamera).target = NonPlayerCameraModClientMain.testFrameBuffer;
            ((SuperCamera) NonPlayerCameraModClientMain.testCamera).renderer.graphicsChanged();
        }

        var testCamera = NonPlayerCameraModClientMain.testCamera;

        testCamera.setPosition(Minecraft.getInstance().gameRenderer.mainCamera.getPosition());
        testCamera.setRotation(mc.gameRenderer.mainCamera.getYRot(), mc.gameRenderer.mainCamera.getXRot());

        SuperGameRenderer.render((SuperCamera) testCamera, pausePartialTick, Util.getNanos());
    }


}
