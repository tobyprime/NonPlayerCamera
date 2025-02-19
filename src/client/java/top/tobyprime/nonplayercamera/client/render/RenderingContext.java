package top.tobyprime.nonplayercamera.client.render;

import java.util.logging.Level;

import com.mojang.blaze3d.pipeline.RenderTarget;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL11;
import top.tobyprime.nonplayercamera.client.common.LevelManager;
import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.common.SuperCamera;

public class RenderingContext {
    // public final SuperCamera camera;
    public ClientLevel level;
    public LevelRenderer levelRenderer;
    public Camera camera;
    public Vec3 cameraEntityPos;
    public RenderTarget renderTarget;
    public RenderBuffers buffers;

    public RenderingContext(){
        Validate.isTrue(!RenderingManager.isEnvModified());
        var client = Minecraft.getInstance();
        this.camera = client.gameRenderer.getMainCamera();
        this.level = client.level;
        this.levelRenderer = client.levelRenderer;

        this.cameraEntityPos = client.cameraEntity.position();
        this.renderTarget = client.mainRenderTarget;
        this.buffers = client.gameRenderer.renderBuffers;
    }
    public RenderingContext(SuperCamera camera){
        this.camera = camera;
        this.level = LevelManager.get(camera.getDimension());
        this.cameraEntityPos = camera.getPosition();
        this.renderTarget = camera.target;
        this.buffers = camera.renderBuffers;
        this.levelRenderer = camera.renderer;
    }

    public void apply(){
        
        var client = Minecraft.getInstance();
        var gameRenderer = client.gameRenderer;
    
        gameRenderer.mainCamera = camera;
        
        client.level = level;
        client.level.levelRenderer = levelRenderer;

        client.levelRenderer = levelRenderer;
        client.levelRenderer.renderBuffers = buffers;

        client.cameraEntity.setPos(cameraEntityPos);
        client.cameraEntity.level = level;
        
        client.mainRenderTarget = renderTarget;

        client.gameRenderer.renderBuffers = buffers;
        client.gameRenderer.mainCamera = camera;

        client.particleEngine.setLevel(level);
        client.blockEntityRenderDispatcher.setLevel(level);

        renderTarget.bindWrite(true);
    }
}
