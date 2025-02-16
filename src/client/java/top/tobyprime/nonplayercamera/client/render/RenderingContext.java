package top.tobyprime.nonplayercamera.client.render;

import java.util.logging.Level;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import top.tobyprime.nonplayercamera.client.common.LevelManager;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeCamera;

public class RenderingContext {
    public final Camera camera;

    public RenderingContext(Camera camera){
        this.camera = camera;
    }

    public void apply(){
        var renderingData = ((BridgeCamera) camera).getRenderingData();
        var client = Minecraft.getInstance();
        var gameRenderer = client.gameRenderer;
        var dimension = renderingData.dimension;
        
        var level = LevelManager.get(dimension);
        var levelRenderer = level.levelRenderer;
    

        gameRenderer.mainCamera = camera;
        
        client.level = level;
        client.levelRenderer = levelRenderer;
        
        client.cameraEntity.setPos(camera.getPosition());
        client.cameraEntity.level = level;
        
        client.mainRenderTarget = renderingData.target;

        client.particleEngine.setLevel(level);
        client.blockEntityRenderDispatcher.setLevel(level); 
    }
}
