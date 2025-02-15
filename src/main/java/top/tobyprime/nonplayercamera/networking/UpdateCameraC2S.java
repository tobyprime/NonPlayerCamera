package top.tobyprime.nonplayercamera.networking;


import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import top.tobyprime.nonplayercamera.common.ServerCamera;
import top.tobyprime.nonplayercamera.common.ServerCameraManager;
import top.tobyprime.nonplayercamera.utils.Global;
import top.tobyprime.nonplayercamera.utils.Helper;

public class UpdateCameraC2S implements Packet<ServerGamePacketListener> {
    public static final ResourceLocation ID = new ResourceLocation(Global.MOD_ID, "update_camera");

    ServerCamera camera;

    public UpdateCameraC2S(ServerCamera camera) {
        this.camera = camera;
    }

    public UpdateCameraC2S(FriendlyByteBuf buf) {
        camera = new ServerCamera();
        camera.resourceLocation = buf.readResourceLocation();
        camera.pos = buf.readBlockPos();

        camera.enabled = buf.readBoolean();
        camera.viewDistance = buf.readInt();
        camera.level = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(ID);
        buf.writeBlockPos(camera.pos);

        buf.writeBoolean(camera.enabled);
        buf.writeInt(camera.viewDistance);
        buf.writeResourceLocation(camera.level.location());
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        this.camera.player = ((ServerGamePacketListenerImpl) listener).player;
        ServerCameraManager.onUpdateCamera(camera);
    }

}
