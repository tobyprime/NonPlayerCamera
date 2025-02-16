package top.tobyprime.nonplayercamera.client.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import top.tobyprime.nonplayercamera.client.render.RenderingContext;

public class RenderingManager {
    public static Stack<RenderingContext> renderingContextStack = new Stack<>();
    public static Map<ResourceKey<Level>, Set<Camera>> activatedCameras = new HashMap<>();
    public static RenderingContext mainRenderingContext;

    public static Camera getCurrentCamera(){
        if (renderingContextStack.empty()) {
            if (mainRenderingContext == null){
                return Minecraft.getInstance().gameRenderer.getMainCamera();
            }
            return mainRenderingContext.camera;
        }
        return renderingContextStack.peek().camera;
    }
    public static void begin(RenderingContext context) {
        if (mainRenderingContext==null) {
            mainRenderingContext = new RenderingContext(Minecraft.getInstance().gameRenderer.mainCamera);
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
        var context = renderingContextStack.peek();
        if (context != null) {
            return context;
        }
        return mainRenderingContext;
    }
}
