package top.tobyprime.nonplayercamera.client.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceKey;
import top.tobyprime.nonplayercamera.client.render.RenderingContext;

public class RenderingManager {
    public static Stack<RenderingContext> renderingContextStack = new Stack<>();
    public static Map<ResourceKey<Level>, Set<Camera>> activatedCameras = new HashMap<>();
    public static Map<Camera, LevelRenderer> levelRendererMap = new HashMap<>();
    public static RenderingContext mainRenderingContext;

    public static boolean isEnvModified(){
        return !renderingContextStack.isEmpty();
    }

    public static Camera getCurrentCamera(){
        var ctx =renderingContextStack.empty()?renderingContextStack.peek(): mainRenderingContext;
        if (ctx == null){
            return Minecraft.getInstance().gameRenderer.getMainCamera();
        }
        return ctx.camera;
    }
    public static void begin(RenderingContext context) {
        if (mainRenderingContext==null) {
            mainRenderingContext = new RenderingContext();
        }

        context.apply();
        renderingContextStack.push(context);
    }

    public static void end() {
        renderingContextStack.pop();
        peekOrMain().apply();
        if (renderingContextStack.empty()) {
            mainRenderingContext = null;
        }
    }

    public static RenderingContext peekOrMain() {
        if (renderingContextStack.empty()) {
            return mainRenderingContext;

        }
        return renderingContextStack.peek();

    }
}
