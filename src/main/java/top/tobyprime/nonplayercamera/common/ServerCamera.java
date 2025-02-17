package top.tobyprime.nonplayercamera.common;


import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class ServerCamera {
    public ResourceLocation identifier;
    public BlockPos pos;
    public ServerPlayer player;
    public boolean enabled;
    public int viewDistance;
    public ResourceKey<Level> dimension;


    public BlockPos lastPos;
}
