package top.tobyprime.nonplayercamera.client.render;

import java.util.logging.Level;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import top.tobyprime.nonplayercamera.client.common.LevelManager;
import top.tobyprime.nonplayercamera.client.common.SuperCamera;

public class RenderingContext {
    // public final SuperCamera camera;
    public ClientLevel level;
    public Camera camera;
    public Vec3 cameraEntityPos;
    public RenderTarget renderTarget;
    public RenderingContext(){
        var client = Minecraft.getInstance();
        this.camera = (SuperCamera) client.gameRenderer.getMainCamera();
        this.level = client.level;
        this.cameraEntityPos = client.cameraEntity.position();
        this.renderTarget = client.mainRenderTarget;
    }
    public RenderingContext(SuperCamera camera){
        this.camera = camera;

        this.level = LevelManager.get(camera.getDimension());
        this.cameraEntityPos = camera.getPosition();
        this.renderTarget = camera.target;
    }

    public void apply(){
        
        var client = Minecraft.getInstance();
        var gameRenderer = client.gameRenderer;
    
        gameRenderer.mainCamera = camera;
        
        client.level = level;
        client.levelRenderer = level.levelRenderer;
        
        client.cameraEntity.setPos(cameraEntityPos);
        client.cameraEntity.level = level;
        
        client.mainRenderTarget = renderTarget;

        client.particleEngine.setLevel(level);
        client.blockEntityRenderDispatcher.setLevel(level);
        
        renderTarget.bindWrite(false);
    }
}
