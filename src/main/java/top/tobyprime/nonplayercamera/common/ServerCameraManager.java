package top.tobyprime.nonplayercamera.common;


import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import top.tobyprime.nonplayercamera.mixin_bridge.BridgeChunkMap;
import top.tobyprime.nonplayercamera.utils.Global;
import top.tobyprime.nonplayercamera.utils.Helper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerCameraManager {


    // todo: expiryTicks
    public static final TicketType<ChunkPos> CAMERA_CHUNK_TICKET =
            TicketType.create(Global.MOD_ID+":camera_chunk_ticket", Comparator.comparingLong(ChunkPos::toLong),5);

    public static Map<UUID, Map<ResourceLocation, ServerCamera>> camerasPerPlayer = new HashMap<>();

    public static void tick(){
        for (Map.Entry<UUID, Map<ResourceLocation, ServerCamera>> entry : camerasPerPlayer.entrySet()){
            for (Map.Entry<ResourceLocation, ServerCamera> cameraEntry : entry.getValue().entrySet()){
                tickCamera(cameraEntry.getValue());
            }
        }
    }

    public static void tickCamera(ServerCamera camera) {
        if (!camera.enabled) return;

        var chunkStorage = (BridgeChunkMap) camera.player.getServer().getLevel(camera.dimension).getChunkSource().chunkMap;
        var chunkTicketManager = ((ChunkMap)chunkStorage).getDistanceManager();
        var chunkPos = new ChunkPos(camera.pos);
        chunkTicketManager.addTicket(CAMERA_CHUNK_TICKET,chunkPos,camera.viewDistance,chunkPos);
    }

    public static void onUpdateCamera(ServerCamera camera) {
        var uuid = camera.player.getUUID();


        if (!camerasPerPlayer.containsKey(uuid)) {
            camerasPerPlayer.put(uuid, new HashMap<>());
        }

        var cameras = camerasPerPlayer.get(uuid);
        var newChunkStorage = (BridgeChunkMap) camera.player.getServer().getLevel(camera.dimension).getChunkSource().chunkMap;

        if (cameras.containsKey(uuid)) {
            var preCamera = cameras.get(camera.identifier);
            var preChunkStorage = (BridgeChunkMap) camera.player.getServer().getLevel(preCamera.dimension).getChunkSource().chunkMap;
            preChunkStorage.getActivateCameras().remove(preCamera);
            cameras.replace(camera.identifier, camera);
            if (camera.enabled){
                newChunkStorage.getActivateCameras().add(camera);
            }
            return;
        }

        cameras.put(camera.identifier, camera);
        if (camera.enabled){
            newChunkStorage.getActivateCameras().add(camera);
        }
    }

    @Nullable
    public static ServerCamera tryGetCamera(UUID uuid,ResourceLocation ResourceLocation) {
        var cameras = camerasPerPlayer.get(uuid);
        if (cameras == null) {
            Helper.warn("No camera '" + ResourceLocation + "' found for player " + uuid);
            return null;
        }
        var camera = cameras.get(ResourceLocation);
        if (camera == null) {
            Helper.warn("No camera '" + ResourceLocation + "' found for player " + uuid);
            return null;
        }
        return camera;
    }

    public static void onUpdateCameraPosition(ServerPlayer player, ResourceLocation ResourceLocation, BlockPos newPos) {
        var camera = tryGetCamera(player.getUUID(), ResourceLocation);
        if (camera == null) {
            return;
        }
        camera.pos = newPos;
    }

    public static void onRemoveCamera(ServerPlayer player, ResourceLocation cameraId) {
        var camera = tryGetCamera(player.getUUID(), cameraId);
        if (camera == null) {
            return;
        }
        var server = camera.player.getServer();
        var cameraServerLevel = server.getLevel(camera.dimension);

        var chunkStorage = (BridgeChunkMap) cameraServerLevel.getChunkSource().chunkMap;
        chunkStorage.getActivateCameras().remove(camera);
        camerasPerPlayer.get(player.getUUID()).remove(cameraId);

    }

}
