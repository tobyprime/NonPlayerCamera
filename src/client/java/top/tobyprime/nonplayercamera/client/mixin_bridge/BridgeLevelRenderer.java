package top.tobyprime.nonplayercamera.client.mixin_bridge;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface BridgeLevelRenderer {
    public RenderTarget getRenderTarget();

    public void setRenderTarget(RenderTarget renderTarget);
}
