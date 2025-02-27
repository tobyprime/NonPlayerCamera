package top.tobyprime.nonplayercamera.client.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.tobyprime.nonplayercamera.utils.Helper;

/**
 * 原版仅存储玩家周围的区块，修改为对每个相机都存储其周围的区块。{@link top.tobyprime.nonplayercamera.client.mixin.MixinClientLevel#setChunkSource} 中将原版ClientChunkCache替换为此类
 */
public class SuperChunkCache extends ClientChunkCache {
    public final ConcurrentMap<Camera, Storage> storages = new ConcurrentHashMap<>();
    public final ClientLevel level;

    public SuperChunkCache(ClientLevel level) {
        super(level, 0);
        this.level = level;
        this.storage = null;
    }

    public void reAssignRemovedStorageData(Storage storage) {
        if (storage == null) {
            return;
        }
        for (int k = 0; k < storage.chunks.length(); ++k) {
            LevelChunk levelChunk = storage.chunks.get(k);
            if (levelChunk != null) {
                ChunkPos chunkPos = levelChunk.getPos();
                var targetStorage = getStorageAt(chunkPos.x, chunkPos.z);
                if (targetStorage != null) {
                    targetStorage.replace(targetStorage.getIndex(chunkPos.x, chunkPos.z), levelChunk);
                }
            }
        }
    }

    public void fullyUpdateStorageForCamera(Camera camera) {
        var oldStorage = storages.get(camera);
        var storage = new Storage(getCameraViewDistance(camera) * 2);

        var cameraChunkPos = new ChunkPos(camera.getBlockPosition());

        storage.viewCenterX = cameraChunkPos.x;
        storage.viewCenterZ = cameraChunkPos.z;

        storages.put(camera, storage);

        if (oldStorage != null) {
            reAssignRemovedStorageData(oldStorage);
        }
        Helper.dbg("fully updating storage");
    }



    public void onCameraViewDistanceChanged(Camera camera) {
        var oldStorage = storages.get(camera);
        if (oldStorage.chunkRadius == getCameraViewDistance(camera) * 2) return;
        fullyUpdateStorageForCamera(camera);
    }

    public boolean shouldMoveStorage(Storage storage, Camera camera) {
        var cameraChunkPos = new ChunkPos(camera.getBlockPosition());

        var x = storage.viewCenterX - cameraChunkPos.x;
        var z = storage.viewCenterZ - cameraChunkPos.z;
        var t = storage.chunkRadius / 2;
        return x * x + z * z > t * t;
    }

    public static int getCameraViewDistance(Camera camera) {
        return camera instanceof SuperCamera ?
                ((SuperCamera) camera).getViewDistance() :
                Minecraft.getInstance().options.getEffectiveRenderDistance();
    }

    public void onCameraMoved(Camera camera) {
        var oldStorage = storages.get(camera);
        if (oldStorage != null && !shouldMoveStorage(oldStorage, camera)) {
            return;
        }
        fullyUpdateStorageForCamera(camera);
    }

    public void addCamera(Camera camera) {
        if (storages.containsKey(camera)) {
            return;
        }
        fullyUpdateStorageForCamera(camera);
    }

    public void removeCamera(Camera camera) {
        var storage = storages.remove(camera);
        reAssignRemovedStorageData(storage);
    }

    public static SuperChunkCache mainCameraSuperChunkCache;

    public static void onMainCameraUpdated(Camera camera) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        var currentSuperChunkCache = (SuperChunkCache) RenderingManager.getCurrentContext().level.getChunkSource();

        if (mainCameraSuperChunkCache != null) {
            if (mainCameraSuperChunkCache != currentSuperChunkCache) {
                mainCameraSuperChunkCache.removeCamera(camera);
            }
            return;
        }
        if (!currentSuperChunkCache.storages.containsKey(camera)) {
            currentSuperChunkCache.addCamera(camera);
        }
        var chunkSource = currentSuperChunkCache.storages.get(camera);

        if (getCameraViewDistance(camera) * 2 != chunkSource.chunkRadius) {
            currentSuperChunkCache.fullyUpdateStorageForCamera(camera);
            return;
        }

        currentSuperChunkCache.onCameraMoved(camera);

    }

    public Storage getStorageAt(int x, int z) {
        Storage clstStorage = null;
        float minDistance = Float.MAX_VALUE;

        for (var storage : storages.values()) {
            var dstSqr = storage.viewCenterX * storage.viewCenterX + storage.viewCenterZ * storage.viewCenterZ;

            if (storage.inRange(x, z) && dstSqr < minDistance) {
                minDistance = dstSqr;
                clstStorage = storage;
            }
        }
        return clstStorage;
    }

    @Override
    public void onLightUpdate(LightLayer layer, SectionPos pos) {
        for (var camera : LevelManager.getCamerasInDimension(this.level.dimension())) {
            camera.renderer.setSectionDirty(pos.x(), pos.y(), pos.z());
        }
        if (this.level == Minecraft.getInstance().level) {
            Minecraft.getInstance().levelRenderer.setSectionDirty(pos.x(), pos.y(), pos.z());
        }
    }

    @Override
    public LevelChunk replaceWithPacketData(int x, int z, FriendlyByteBuf buffer, CompoundTag tag,
                                            Consumer<BlockEntityTagOutput> consumer) {
        var storage = getStorageAt(x, z);
        if (storage == null) {
            Helper.warn("fail load chunk at {" + x + ", " + z + "}: no oldStorage found");

            return null;
        }

        int i = storage.getIndex(x, z);

        LevelChunk levelChunk = storage.chunks.get(i);
        ChunkPos chunkPos = new ChunkPos(x, z);

        if (!isValidChunk(levelChunk, x, z)) {
            levelChunk = new LevelChunk(this.level, chunkPos);
            levelChunk.replaceWithPacketData(buffer, tag, consumer);
            storage.replace(i, levelChunk);
        } else {
            levelChunk.replaceWithPacketData(buffer, tag, consumer);
        }

        this.level.onChunkLoaded(chunkPos);
        Helper.log("load chunk at {" + x + ", " + z + "};");
        return levelChunk;

    }

    @Override
    public @Nullable LevelChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
        var storage = getStorageAt(chunkX, chunkZ);
        if (storage == null) {
            return load ? this.emptyChunk : null;
        }

        var chunk = storage.getChunk(storage.getIndex(chunkX, chunkZ));
        if (!isValidChunk(chunk, chunkX, chunkZ) && load) {
            chunk = this.emptyChunk;
        }

        return chunk;
    }


    @Override
    public int getLoadedChunksCount() {
        var counts = 0;

        for (var storage : storages.values()) {
            counts += storage.chunkCount;
        }

        return counts;
    }

    @Override
    public void updateViewRadius(int viewDistance) {
        var mainCamera = RenderingManager.getCurrentContext().camera;
        onCameraViewDistanceChanged(mainCamera);
    }

    @Override
    public @NotNull String gatherStats() {
        int var10000 = 0;
        for (var storage : storages.values()) {
            var10000 += storage.chunks.length();
        }
        return var10000 + ", " + this.getLoadedChunksCount();
    }

    @Override
    public void updateViewCenter(int x, int z) {

    }

    @Override
    public void drop(int x, int z) {

    }
}

