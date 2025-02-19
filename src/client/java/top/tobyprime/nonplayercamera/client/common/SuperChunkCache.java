package top.tobyprime.nonplayercamera.client.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.bridge.Bridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache.Storage;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.tobyprime.nonplayercamera.utils.Helper;

/**
 * 原版仅存储玩家周围的区块，修改为对每个相机都存储其周围的区块。{@link top.tobyprime.nonplayercamera.client.mixin.MixinClientLevel#injectInit} 中将原版ClientChunkCache替换为此类
 */
public class SuperChunkCache extends ClientChunkCache {
    public Map<Camera, Storage> storages = new HashMap<>();
    public ClientLevel level;

    public SuperChunkCache(ClientLevel level) {
        super(level, 0);
        this.level = level;
        this.storage = null;
    }

    public void onCameraUpdated(Camera camera) {
        var storage = storages.get(camera);

        var viewDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();

        if (camera instanceof SuperCamera) {
            boolean enabled = ((SuperCamera) camera).isEnabled();
            boolean inLevel = ((SuperCamera) camera).getDimension() == this.level.dimension();

            if (!(enabled || inLevel) && storage != null) {
                Helper.log("on camera leave");
                storages.remove(camera);
                return;
            }
            viewDistance = ((SuperCamera) camera).getViewDistance();
        }

        var newChunkPos = new ChunkPos(camera.getBlockPosition());
        if (storage != null) {
            int x = storage.viewCenterX - newChunkPos.x;
            int z = storage.viewCenterZ - newChunkPos.z;

            if (x * x + z * z < viewDistance * viewDistance) {
                return;
            }
        }


        Storage newStorage = new Storage(viewDistance * 2);
        if (storage == null) {
            Helper.log("on camera in");
            storages.put(camera, newStorage);
            return;
        }

        newStorage.viewCenterX = newChunkPos.x;
        newStorage.viewCenterZ = newChunkPos.z;


        Helper.log("on camera storage move");

        storages.replace(camera, newStorage);


        for (int k = 0; k < storage.chunks.length(); ++k) {
            LevelChunk levelChunk = storage.chunks.get(k);
            if (levelChunk != null) {
                ChunkPos chunkPos = levelChunk.getPos();
                var targetStorage = getStorageAt(chunkPos.x, chunkPos.z);
                if (targetStorage != null) {
                    targetStorage.replace(newStorage.getIndex(chunkPos.x, chunkPos.z), levelChunk);
                }
            }
        }

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
    public LevelChunk replaceWithPacketData(int x, int z, FriendlyByteBuf buffer, CompoundTag tag,
                                            Consumer<BlockEntityTagOutput> consumer) {
        var storage = getStorageAt(x, z);
        if (storage == null) {
            Helper.warn("fail load chunk at {" + x + ", " + z + "}: no storage found");

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
        // todo: 目前无法更新视距
        return;
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
        return;
    }

    @Override
    public void drop(int x, int z) {

    }
}

