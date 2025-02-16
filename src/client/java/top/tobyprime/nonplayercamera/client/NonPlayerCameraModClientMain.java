package top.tobyprime.nonplayercamera.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.Camera;
import top.tobyprime.nonplayercamera.NonPlayerCameraModMain;

import top.tobyprime.nonplayercamera.client.render.TestBlockEntityRenderer;

public class NonPlayerCameraModClientMain implements ClientModInitializer {
    public static TextureTarget testFrameBuffer;
    public static boolean needRender = false;
    public static Camera testCamera;
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(NonPlayerCameraModMain.TEST_BLOCK_ENTITY, TestBlockEntityRenderer::new);
    }
}
