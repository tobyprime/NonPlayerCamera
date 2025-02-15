package top.tobyprime.nonplayercamera.mixin_bridge;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface BridgeClientboundLevelChunkWithLightPacket {
    ResourceKey<Level> getLevelKey();
}
