package top.tobyprime.nonplayercamera.client.mixin_bridge;

import org.spongepowered.asm.mixin.Unique;

public interface BridgeRenderChunk {

    public long getPreRenderTime() ;
    public void setPreRenderTime(long preRenderTime) ;
}
