package top.tobyprime.nonplayercamera.client.common;


import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Minecraft;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class CameraRenderingData {
    public RenderTarget target;
    public ResourceKey<Level> dimension;

    public double fov = 70;
    public float zoom = 1.0f;
    public float zoomX;
    public float zoomY;
    public float cameraDepth = 0.05F;
    public int viewDistance = 8;
    public boolean toScreen = true;

    public static CameraRenderingData getBaseCameraRenderingData(){
        CameraRenderingData data = new CameraRenderingData();
        Minecraft client = Minecraft.getInstance();
        
        data.target = client.getMainRenderTarget();
        data.dimension = client.level.dimension();
        data.fov = client.options.fov;
        data.viewDistance = client.options.getEffectiveRenderDistance();
        data.toScreen = true;
        
        return data;
    }
}
