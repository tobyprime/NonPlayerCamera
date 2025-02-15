package top.tobyprime.nonplayercamera.client.mixin_bridge;

import org.spongepowered.asm.mixin.Unique;
import top.tobyprime.nonplayercamera.client.render.NonPlayerLevelRenderer;

public interface BridgeClientChunkMap {
    NonPlayerLevelRenderer getNonPlayerLevelRenderer();
    void setNonPlayerLevelRenderer(NonPlayerLevelRenderer nonPlayerLevelRenderer);

}
