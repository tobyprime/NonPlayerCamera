package top.tobyprime.nonplayercamera.client.common;

import com.mojang.blaze3d.pipeline.RenderTarget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import top.tobyprime.nonplayercamera.client.mixin.MixinLevelRenderer;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeLevelRenderer;
import top.tobyprime.nonplayercamera.common.ServerCamera;
import top.tobyprime.nonplayercamera.networking.UpdateCameraC2S;
import top.tobyprime.nonplayercamera.networking.UpdateCameraPosC2S;

import java.util.HashSet;

public class SuperCamera extends Camera {
    private ResourceKey<Level> dimension;
    private boolean enabled;
    private int viewDistance = 12;
    private final ResourceLocation identifier;
    public RenderBuffers renderBuffers = new RenderBuffers();
    private BlockPos preUpdatedBlockPos = new BlockPos(0, 0, 0);
    public final boolean isolatedStorage = false;
    public RenderTarget target;
    public double fov = 70;
    public float cameraDepth = 0.05F;
    public LevelRenderer renderer;
    public ClientLevel level;

    public SuperCamera(ResourceLocation identifier) {
        this.identifier = identifier;
        this.renderBuffers = new RenderBuffers();
        this.renderer = new LevelRenderer(Minecraft.getInstance(), renderBuffers);
        Minecraft.getInstance().resourceManager.registerReloadListener(this.renderer);

        ((BridgeLevelRenderer) this.renderer).setCamera(this);
        this.renderer.graphicsChanged();
    }

    public Matrix4f getBasicProjectionMatrix() {
        PoseStack PoseStack = new PoseStack();
        PoseStack.last().pose().setIdentity();

        PoseStack.last().pose().multiply(Matrix4f.perspective(fov, (float) target.width / target.height, cameraDepth, viewDistance * 16));
        return PoseStack.last().pose();
    }

    @Override
    public void setRotation(float yRot, float xRot) {
        super.setRotation(yRot, xRot);
    }

    @Override
    public void setPosition(Vec3 pos) {
        super.setPosition(pos);
        notifyPositionUpdated();
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public void setDimension(ResourceKey<Level> dimension) {
        if (this.dimension == dimension) {
            return;
        }
        var preDimension = this.dimension;
        if (isolatedStorage){

            if (this.level != null) {
                ((SuperChunkCache)this.level.getChunkSource()).removeCamera(this);
            }
        }


        this.dimension = dimension;
        this.level = LevelManager.get(dimension);
        this.renderer.setLevel(this.level);
        this.renderer.getChunkRenderDispatcher().setCamera(this.getPosition());
        LevelManager.onCameraLevelUpdated(this, preDimension);

        if (!isolatedStorage) {return;}
        Validate.notNull(this.level);

        ((SuperChunkCache)this.level.getChunkSource()).addCamera(this);

        notifyAllUpdated();
    }

    public void enable() {
        if (enabled) {
            return;
        }
        enabled = true;

        notifyAllUpdated();
    }

    public void disable() {
        if (!enabled) {
            return;
        }
        enabled = false;

        notifyAllUpdated();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setViewDistance(int distance) {
        if (viewDistance == distance) {
            return;
        }
        this.viewDistance = distance;
        notifyAllUpdated();
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public void notifyPositionUpdated() {
        if (LevelManager.get(dimension) == null) return;
        if (!isolatedStorage) {
            return;
        }
        ((SuperChunkCache)level.getChunkSource()).onCameraMoved(this);

        if (preUpdatedBlockPos.closerThan(this.getBlockPosition(), 4)) {
            return;
        }
        Minecraft.getInstance().getConnection().send(new UpdateCameraPosC2S(this.identifier, this.getBlockPosition()));
        preUpdatedBlockPos = getBlockPosition();

    }

    public void notifyAllUpdated() {
        if (LevelManager.get(dimension) == null) return;

        if (!isolatedStorage) {
            return;
        }


        var serverCamera = new ServerCamera();
        serverCamera.enabled = this.enabled;
        serverCamera.dimension = this.dimension;
        serverCamera.pos = this.getBlockPosition();
        serverCamera.identifier = this.identifier;

        Minecraft.getInstance().getConnection().send(new UpdateCameraC2S(serverCamera));
    }
}
