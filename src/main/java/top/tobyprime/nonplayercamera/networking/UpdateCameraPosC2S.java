package top.tobyprime.nonplayercamera.networking;


import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import top.tobyprime.nonplayercamera.common.ServerCameraManager;
import top.tobyprime.nonplayercamera.utils.Global;

public class UpdateCameraPosC2S implements Packet<ServerGamePacketListener> {
    public static final ResourceLocation ID = new ResourceLocation(Global.MOD_ID, "update_camera_pos");

    public ResourceLocation cameraId;
    public BlockPos pos;

    public UpdateCameraPosC2S(ResourceLocation cameraId, BlockPos pos) {
        this.cameraId = cameraId;
        this.pos = pos;
    }

    public UpdateCameraPosC2S(FriendlyByteBuf buf) {
        cameraId = buf.readResourceLocation();
        pos = buf.readBlockPos();
    }


    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.cameraId);
        buf.writeBlockPos(this.pos);
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        var player = ((ServerGamePacketListenerImpl)listener).player;
        ServerCameraManager.onUpdateCameraPosition(player, cameraId, pos);
    }

}
