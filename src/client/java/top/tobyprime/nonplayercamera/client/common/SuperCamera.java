package top.tobyprime.nonplayercamera.client.common;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.tobyprime.nonplayercamera.common.ServerCamera;
import top.tobyprime.nonplayercamera.networking.UpdateCameraC2S;
import top.tobyprime.nonplayercamera.networking.UpdateCameraPosC2S;

public class SuperCamera extends Camera {
    private ResourceKey<Level> dimension;
    private boolean enabled;
    private int viewDistance;
    private final ResourceLocation identifier;

    private BlockPos preUpdatedBlockPos;

    public RenderTarget target;

    public SuperCamera(ResourceLocation identifier){
        this.identifier = identifier;
    }

    public void setRotation(float yRot,float xRot){
        super.setRotation(yRot, xRot);
    }
    @Override
    public void setPosition(Vec3 pos){
        super.setPosition(pos);
        notifyPositionUpdated();
    }
    public ResourceKey<Level> getDimension(){
        return dimension;
    }
    public void enbale(){
        if (enabled == true){
            return;
        }
        enabled = true;

        notifyAllUpdated();
    }

    public void disable(){
        if (enabled == false) {
            return;
        }
        enabled = false;

        notifyAllUpdated();
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void setViewDistance(int distance){
        if (viewDistance == distance) {
            return;
        }
        this.viewDistance = distance;
        notifyAllUpdated();
    }

    public int getViewDistance(){
        return viewDistance;
    }

    public void notifyPositionUpdated(){
        if (preUpdatedBlockPos.closerThan(this.getBlockPosition(), 4)) {
            return;
        }
        Minecraft.getInstance().getConnection().send(new UpdateCameraPosC2S(this.identifier, this.getBlockPosition()));
        preUpdatedBlockPos = getBlockPosition();

    }
    public void notifyAllUpdated(){
        var serverCamera = new ServerCamera();
        serverCamera.enabled = this.enabled;
        serverCamera.dimension = this.dimension;
        serverCamera.pos = this.getBlockPosition();
        serverCamera.identifier = this.identifier;


        Minecraft.getInstance().getConnection().send(new UpdateCameraC2S(serverCamera));
    }
}
