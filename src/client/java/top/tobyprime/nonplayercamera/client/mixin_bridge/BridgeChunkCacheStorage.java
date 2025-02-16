package top.tobyprime.nonplayercamera.client.mixin_bridge;


import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface BridgeChunkCacheStorage {
    ResourceKey<Level> getDimension();
    void setDimension(ResourceKey<Level> dimension);

}
