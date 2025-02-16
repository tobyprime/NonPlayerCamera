package top.tobyprime.nonplayercamera.client.render;

import org.apache.commons.lang3.Validate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import top.tobyprime.nonplayercamera.client.common.CameraRenderingData;
import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeCamera;

public class GameRendererForCamera {
    public static void render(Camera camera,float partialTicks, long nanoTime){
        var context = new RenderingContext(camera);
        RenderingManager.begin(context);

        Minecraft.getInstance().gameRenderer.renderLevel(partialTicks, nanoTime, new PoseStack());

        RenderingManager.end();
    }

    public static void setupRenderPos(Vec3 pos) {
        var client = Minecraft.getInstance();
        var cameraEntity = client.getCameraEntity();

        if (cameraEntity != null) {
            cameraEntity.setPos(pos);
        }
    }
}
