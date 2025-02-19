package top.tobyprime.nonplayercamera.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL11;
import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.common.SuperCamera;

public class SuperGameRenderer {
    public static void render(SuperCamera camera,float partialTicks, long nanoTime){
        var context = new RenderingContext(camera);
        RenderingManager.begin(context);

        renderLevel(camera,partialTicks, nanoTime, new PoseStack());

        RenderingManager.end();
    }

    public static void renderLevel(SuperCamera camera, float tickDelta, long limitTime, PoseStack matrices) {
        // todo: profile
        // todo: 半透明无法渲染，天气不渲染

        var client = Minecraft.getInstance();
        var gameRenderer = client.gameRenderer;
        client.getMainRenderTarget().bindWrite(true);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        PoseStack PoseStack = new PoseStack();
        PoseStack.last().pose().multiply(camera.getBasicProjectionMatrix());

        Matrix4f matrix4f = PoseStack.last().pose();
        gameRenderer.resetProjectionMatrix(matrix4f);

        matrices.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        matrices.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        Matrix3f matrix3f = matrices.last().normal().copy();
        if (matrix3f.invert()) {
            RenderSystem.setInverseViewRotationMatrix(matrix3f);
        }

        Minecraft.getInstance().levelRenderer.prepareCullFrustum(matrices, camera.getPosition(), camera.getBasicProjectionMatrix());
        Minecraft.getInstance().levelRenderer.renderLevel(matrices, tickDelta, limitTime, false, camera, gameRenderer, Minecraft.getInstance().gameRenderer.lightTexture(), matrix4f);
        Minecraft.getInstance().mainRenderTarget.unbindWrite();
    }
}
