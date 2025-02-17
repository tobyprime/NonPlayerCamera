package top.tobyprime.nonplayercamera.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import top.tobyprime.nonplayercamera.client.common.RenderingManager;

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
