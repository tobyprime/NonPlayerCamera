package top.tobyprime.nonplayercamera.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import top.tobyprime.nonplayercamera.client.common.LevelManager;
import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.common.SuperCamera;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeClientLevel;

public class RenderingContext {
    // public final SuperCamera camera;
    public final ClientLevel level;
    public final LevelRenderer levelRenderer;
    public final Camera camera;
    public Vec3 cameraEntityPos;
    public final RenderTarget renderTarget;
    public final RenderBuffers buffers;
    public final ParticleEngine particleEngine;
    public RenderingContext(){
        Validate.isTrue(!RenderingManager.isEnvModified());
        var client = Minecraft.getInstance();
        this.camera = client.gameRenderer.getMainCamera();
        this.level = client.level;
        this.levelRenderer = client.levelRenderer;
        if (client.cameraEntity != null) {
            this.cameraEntityPos = client.cameraEntity.position();
        }

        this.renderTarget = client.mainRenderTarget;
        this.buffers = client.gameRenderer.renderBuffers;
        this.particleEngine = client.particleEngine;

    }
    public RenderingContext(SuperCamera camera){
        this.camera = camera;
        this.level = LevelManager.get(camera.getDimension());
        this.cameraEntityPos = camera.getPosition();
        this.renderTarget = camera.target;
        this.buffers = camera.renderBuffers;
        this.levelRenderer = camera.renderer;
        this.particleEngine = ((BridgeClientLevel)this.level).getParticleEngine();
    }

    public void apply(){
        
        var client = Minecraft.getInstance();
        var gameRenderer = client.gameRenderer;
    
        gameRenderer.mainCamera = camera;
        
        client.level = this.level;
        client.level.levelRenderer = this.levelRenderer;

        client.levelRenderer = this.levelRenderer;
        client.levelRenderer.renderBuffers = this.buffers;

        if (cameraEntityPos!=null&& client.cameraEntity!=null)
        {
            client.cameraEntity.setPos(cameraEntityPos);
            client.cameraEntity.level = this.level;
        }
        
        client.mainRenderTarget = this.renderTarget;

        client.gameRenderer.renderBuffers = this.buffers;
        client.gameRenderer.mainCamera = this.camera;

        client.particleEngine = this.particleEngine;
        client.blockEntityRenderDispatcher.setLevel(this.level);

        renderTarget.bindWrite(true);
    }
}
