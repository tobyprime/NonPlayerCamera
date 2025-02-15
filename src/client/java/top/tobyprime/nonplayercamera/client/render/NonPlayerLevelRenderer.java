package top.tobyprime.nonplayercamera.client.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL11;
import top.tobyprime.nonplayercamera.client.common.NonPlayerCamera;
import top.tobyprime.nonplayercamera.client.common.NonPlayerLevelManager;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeLevelRenderer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NonPlayerLevelRenderer {
    public static NonPlayerLevelRenderer onCreatingLevelRenderer = null;
    public static RenderBuffers secondaryRenderBuffers;
    public static Map<ResourceKey<Level>, NonPlayerLevelRenderer> nonPlayerLevelRendererMap = new HashMap<>();
    public static NonPlayerLevelRenderer currentNonPlayerRendering = null;
    public static NonPlayerCamera currentNonPlayerCamera = null;
    public static boolean isNonPlayerRendering() {
        return currentNonPlayerRendering != null;
    }

    public ClientLevel level;

    public LevelRenderer levelRenderer;

    public Set<NonPlayerCamera> activeCameras = new HashSet<>();

    public LightTexture lightTexture;


    NonPlayerLevelRenderer() {
        this.levelRenderer = new LevelRenderer(Minecraft.getInstance(), secondaryRenderBuffers);
    }

    public static NonPlayerLevelRenderer getOrCreateNonPlayerLevelRender(ResourceKey<Level> dimension) {
        if (nonPlayerLevelRendererMap.containsKey(dimension)) {
            return nonPlayerLevelRendererMap.get(dimension);
        }

        var nonPlayerLevelRenderer = new NonPlayerLevelRenderer();
        ClientLevel level;
        if (Minecraft.getInstance().level.dimension() == dimension) {
            level = Minecraft.getInstance().level;
        } else {
            onCreatingLevelRenderer = nonPlayerLevelRenderer;
            level = NonPlayerLevelManager.getOrCreateLevel(dimension, nonPlayerLevelRenderer.levelRenderer);
            onCreatingLevelRenderer = null;
        }

        nonPlayerLevelRenderer.setLevel(level);
        nonPlayerLevelRendererMap.put(dimension, nonPlayerLevelRenderer);
        nonPlayerLevelRenderer.levelRenderer.onResourceManagerReload(Minecraft.getInstance().getResourceManager());

        return nonPlayerLevelRenderer;
    }


    public void setLevel(ClientLevel Level) {
        this.level = Level;
        this.levelRenderer.setLevel(Level);

        var client = Minecraft.getInstance();

        if (Level != client.level) {
            lightTexture = new LightTexture(client.gameRenderer, client);
        } else {
            lightTexture = client.gameRenderer.lightTexture();
        }
    }

    public void setInverseViewRotationMatrixIfNeeded(PoseStack poseStack) {
        Matrix3f matrix3f = poseStack.last().normal().copy();
        if (matrix3f.invert()) {
            RenderSystem.setInverseViewRotationMatrix(matrix3f);
        }
    }

    public void render(NonPlayerCamera camera, float tickDelta, long limitTime, PoseStack matrixStack, float aspectRatio) {
        currentNonPlayerRendering = this;
        currentNonPlayerCamera = camera;

        this.lightTexture.updateLightTexture(tickDelta);

        var client = Minecraft.getInstance();
        var gameRenderer = client.gameRenderer;
        var player = client.player;
        var cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) {
            Validate.notNull(client.player);
            cameraEntity = client.player;
        }

        // 备份属性
        var preLevelRenderer = client.levelRenderer;
        var preCamera = gameRenderer.getMainCamera();
        var preCameraEntityPos = cameraEntity.position();
        var preLevel = client.level;
        var prePlayerNoPhysics = false;
        if (player != null) prePlayerNoPhysics = player.noPhysics;
        var preRenderBuffers = client.renderBuffers;
        var preLevelLevelRenderer = level.levelRenderer;

        // 替换 client 与 renderer 属性
        setupLevel(level);
        client.levelRenderer = levelRenderer;
        client.renderBuffers = secondaryRenderBuffers;
        cameraEntity.setPos(camera.getPosition());
        gameRenderer.mainCamera = camera;
        level.levelRenderer = this.levelRenderer;



        if (player != null) player.noPhysics = true;

        RenderSystem.enableBlend();


        // 渲染世界
        var projectionMatrix = setupRenderMatrixAndGetProjectionMatrix(camera, aspectRatio, matrixStack);
        levelRenderer.renderLevel(matrixStack, tickDelta, limitTime, false, camera, gameRenderer, this.lightTexture, projectionMatrix);

        // 恢复 client 与 renderer 属性
        setupLevel(preLevel);

        client.renderBuffers = preRenderBuffers;
        client.levelRenderer = preLevelRenderer;

        cameraEntity.setPos(preCameraEntityPos);
        gameRenderer.mainCamera = preCamera;
        level.levelRenderer = preLevelLevelRenderer;

        if (player != null) player.noPhysics = prePlayerNoPhysics;
        RenderSystem.disableBlend();

        currentNonPlayerRendering = null;
        currentNonPlayerCamera = null;
    }

    public void setupLevel(ClientLevel level) {
        var client = Minecraft.getInstance();
        if (client.getCameraEntity() != null) {
            client.getCameraEntity().level = level;
        }
        client.level = level;
        client.particleEngine.setLevel(level);
        client.blockEntityRenderDispatcher.setLevel(level);
    }

    public void setupRenderPos(Vec3 pos) {
        var client = Minecraft.getInstance();
        var cameraEntity = client.getCameraEntity();

        if (cameraEntity != null) {
            cameraEntity.setPos(pos);
        }

    }

    public Matrix4f setupRenderMatrixAndGetProjectionMatrix(NonPlayerCamera camera, float aspectRatio, PoseStack matrixStack) {

        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().multiply(camera.getBasicProjectionMatrix(aspectRatio));

        Matrix4f projectionMatrix = poseStack.last().pose();
        RenderSystem.setProjectionMatrix(projectionMatrix);


        camera.applyPose(matrixStack);

        setInverseViewRotationMatrixIfNeeded(poseStack);
        levelRenderer.prepareCullFrustum(matrixStack, camera.getPosition(), camera.getBasicProjectionMatrix(aspectRatio));

        return projectionMatrix;
    }

    public void draw(NonPlayerCamera camera, TextureTarget framebuffer) {

        var client = Minecraft.getInstance();
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        ((BridgeLevelRenderer) this.levelRenderer).setRenderTarget(framebuffer);

        var preRenderTarget = Minecraft.getInstance().mainRenderTarget;
        var preWindowWidth = Minecraft.getInstance().window.getWidth();
        var preWindowHeight = Minecraft.getInstance().window.getHeight();

        client.mainRenderTarget = framebuffer;
        client.window.setWidth(framebuffer.width);
        client.window.setHeight(framebuffer.height);

        if (levelRenderer.transparencyChain == null || (levelRenderer.transparencyChain.screenTarget.height != framebuffer.height || levelRenderer.transparencyChain.screenTarget.width != framebuffer.width)) {
            levelRenderer.initTransparency();
            levelRenderer.resize(framebuffer.width, framebuffer.height);
        }

        framebuffer.bindWrite(true);
        render(camera, Minecraft.getInstance().getFrameTime(), Util.getNanos(), new PoseStack(), (float) framebuffer.width / framebuffer.height);

        ((BridgeLevelRenderer) this.levelRenderer).setRenderTarget(null);
        framebuffer.unbindWrite();

        client.mainRenderTarget = preRenderTarget;
        client.window.setWidth(preWindowWidth);
        client.window.setHeight(preWindowHeight);
    }
}
