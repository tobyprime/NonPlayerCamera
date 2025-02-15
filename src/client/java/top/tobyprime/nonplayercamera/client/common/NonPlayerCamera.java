package top.tobyprime.nonplayercamera.client.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.tobyprime.nonplayercamera.client.render.NonPlayerLevelRenderer;
import top.tobyprime.nonplayercamera.common.ServerCamera;
import top.tobyprime.nonplayercamera.networking.UpdateCameraC2S;
import top.tobyprime.nonplayercamera.networking.UpdateCameraPosC2S;

public class NonPlayerCamera extends Camera {

    public NonPlayerLevelRenderer levelRenderer;

    private boolean enable = false;
    public ResourceLocation resourceLocation;
    public double fov = 70;
    public float zoom = 1.0f;
    public float zoomX;
    public float zoomY;
    public float cameraDepth = 0.05F;
    public int viewDistance = 8;

    public Vec3 preCameraPos = new Vec3(0, 0, 0);

    public Matrix4f getBasicProjectionMatrix(float renderTargetAspectRatio) {
        PoseStack PoseStack = new PoseStack();
        PoseStack.last().pose().setIdentity();
        if (this.zoom != 1.0F) {
            PoseStack.translate(this.zoomX, (-this.zoomY),0.0F);
            PoseStack.scale(this.zoom, this.zoom, 1.0F);
        }

        PoseStack.last().pose().multiply(Matrix4f.perspective(fov, renderTargetAspectRatio, cameraDepth, viewDistance*16));
        return PoseStack.last().pose();
    }

    public void applyPose(PoseStack matrices) {
        matrices.mulPose(Vector3f.XP.rotationDegrees(getXRot()));
        matrices.mulPose(Vector3f.YP.rotationDegrees(getYRot() + 180.0F));
    }

    public NonPlayerCamera(ResourceLocation ResourceLocation) {
        this.resourceLocation = ResourceLocation;
    }

    public void setLevel(ResourceKey<Level> Level) {
        levelRenderer = NonPlayerLevelRenderer.getOrCreateNonPlayerLevelRender(Level);
        update();
    }

    public void enable(){
        levelRenderer.activeCameras.add(this);
        enable = true;
        update();
    }

    public void disable(){
        levelRenderer.activeCameras.remove(this);
        enable = false;
        update();
    }

    public void setPosition(double x, double y, double z) {
        setPosition(new Vec3(x, y, z));
    }

    public void setPosition(Vec3 pos) {
        super.setPosition(pos);
        if (!preCameraPos.closerThan(pos, 8)){
            Minecraft.getInstance().getConnection().send(new UpdateCameraPosC2S(resourceLocation, new BlockPos(this.getPosition())));
            preCameraPos = pos;
        }
    }

    public void update() {
        var serverCamera = new ServerCamera();
        serverCamera.resourceLocation = resourceLocation;
        serverCamera.level = levelRenderer.level.dimension();
        serverCamera.enabled = enable;
        serverCamera.pos = getBlockPosition();
        serverCamera.viewDistance = viewDistance;

        Minecraft.getInstance().getConnection().send(new UpdateCameraC2S(serverCamera));
    }


    @Override
    public void setRotation(float yrot, float xrot) {
        super.setRotation(yrot, xrot);
    }
}
