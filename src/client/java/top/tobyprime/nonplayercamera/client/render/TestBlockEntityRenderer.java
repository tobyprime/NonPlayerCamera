package top.tobyprime.nonplayercamera.client.render;

import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import top.tobyprime.nonplayercamera.NonPlayerCameraModMain;
import top.tobyprime.nonplayercamera.client.NonPlayerCameraModClientMain;
import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.common.SuperCamera;

import static top.tobyprime.nonplayercamera.client.NonPlayerCameraModClientMain.needRender;
import static top.tobyprime.nonplayercamera.client.NonPlayerCameraModClientMain.testCamera;

public class TestBlockEntityRenderer implements BlockEntityRenderer<NonPlayerCameraModMain.TestBlockEntity> {
    public TestBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {

    }



    private static void renderQuad(PoseStack matrices, float width, float height) {
        Matrix4f matrix = matrices.last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        float halfW = width / 2;
        float halfH = height / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        buffer.vertex(matrix, -halfW, -halfH, 0).uv(1, 0).endVertex();
        buffer.vertex(matrix, halfW, -halfH, 0).uv(0, 0).endVertex();
        buffer.vertex(matrix, halfW, halfH, 0).uv(0, 1).endVertex();
        buffer.vertex(matrix, -halfW, halfH, 0).uv(1, 1).endVertex();
        tesselator.end();
    }



    @Override
    public void render(NonPlayerCameraModMain.TestBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
         if (RenderingManager.isEnvModified()){
             return;
         }

         var fov = (float)NonPlayerCameraModClientMain.testFrameBuffer.width/ NonPlayerCameraModClientMain.testFrameBuffer.height;

         RenderSystem.setShaderTexture(0, NonPlayerCameraModClientMain.testFrameBuffer.getColorTextureId());
         needRender = true;

         matrices.pushPose();

         RenderSystem.enableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.disableBlend();

         renderQuad(matrices, (float) 2*fov, (float) 2);

         RenderSystem.enableBlend();
         RenderSystem.enableCull();
         RenderSystem.disableDepthTest();

         matrices.popPose();}



}
