package top.tobyprime.nonplayercamera.client.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.mojang.bridge.Bridge;

import net.minecraft.client.multiplayer.ClientChunkCache.Storage;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class SuperChunkCache extends ClientChunkCache {
    public Map<SuperCamera, Storage> storages;
    public ClientLevel level;

    public SuperChunkCache(ClientLevel level) {
        super(level, 0);
        this.level = level;
        this.storage = null;
    }

    @Override
    public LevelChunk replaceWithPacketData(int x, int z, FriendlyByteBuf buffer, CompoundTag tag,
            Consumer<BlockEntityTagOutput> consumer) {
        var storage = getStorageAt(x, z);
        if (storage == null) {
            return null;
        }

        int i = this.storage.getIndex(x, z);
        
        LevelChunk levelChunk = (LevelChunk) storage.chunks.get(i);
        ChunkPos chunkPos = new ChunkPos(x, z);

        if (!isValidChunk(levelChunk, x, z)) {
            levelChunk = new LevelChunk(this.level, chunkPos);
            levelChunk.replaceWithPacketData(buffer, tag, consumer);
            storage.replace(i, levelChunk);
        } else {
            levelChunk.replaceWithPacketData(buffer, tag, consumer);
        }

        this.level.onChunkLoaded(chunkPos);
        return levelChunk;

    }

    @Override
    public LevelChunk getChunk(int chunkX, int chunkZ, boolean load) {
        var storage = getStorageAt(chunkX, chunkZ);
        if (storage == null) {
            return null;
        }
        
        var chunk = storage.getChunk(storage.getIndex(chunkX, chunkZ));
        if (chunk == null) {
            return load ? this.emptyChunk : null;
        }

        return chunk;
    }

    @Override
    public int getLoadedChunksCount() {
        var counts = 0;

        for (var storage : storages.values()) {
            counts+=storage.chunkCount;
        }

        return counts;
    }



    public void onCameraUpdated(SuperCamera camera){
        var storage = storages.get(camera);
        
        var newChunkPos = new ChunkPos(camera.getBlockPosition());
    
        var viewDistance = camera.getViewDistance();
        if (!camera.isEnabled() && storage != null) {
            storages.remove(camera);
        }

        int x= storage.viewCenterX - newChunkPos.x;
        int z = storage.viewCenterZ - newChunkPos.z;
        if (x*x+z*z < viewDistance) {
            return;
        }

        Storage newStorage = new Storage(this, (int)viewDistance);

        newStorage.viewCenterX = newChunkPos.x;
        newStorage.viewCenterZ = newChunkPos.z;
        
        if (storage == null) {
            storages.put(camera, newStorage);
            return;
        }
        
        storages.replace(camera, newStorage);


        for(int k = 0; k < storage.chunks.length(); ++k) {
            LevelChunk levelChunk = (LevelChunk) storage.chunks.get(k);
            if (levelChunk != null) {
               ChunkPos chunkPos = levelChunk.getPos();
               var targetStorage = getStorageAt(x, z);
               if (targetStorage == null) {
                continue;
               }
               targetStorage.replace(newStorage.getIndex(chunkPos.x, chunkPos.z), levelChunk);
            }
        }        

    }
    public Storage getStorageAt(int x, int z) {
        return getClosestChunkStorage(x, z, storages.values());
    }

    public Storage getClosestChunkStorage(int x, int z, Collection<Storage> storages) {
        Storage clstStorage = null;
        float minDistance = Float.MAX_VALUE;

        for (var storage : storages) {
            var dstSqr = storage.viewCenterX * storage.viewCenterX + storage.viewCenterZ * storage.viewCenterZ;

            if (storage.inRange(x, z) && dstSqr < minDistance) {
                minDistance = dstSqr;
                clstStorage = storage;
            }
        }
        return clstStorage;
    }

    @Override
    public void updateViewRadius(int viewDistance) {
        return;
    }

    @Override
    public void updateViewCenter(int x, int z) {
        return;
    }

    @Override
    public void drop(int x, int z) {
        return;
    }

}

record Bound(int xMin, int zMin, int xMax, int zMax) {
    boolean contain(int x, int z) {
        return (xMin <= x && x <= xMax) && (zMin <= z && z <= zMax);
    }
    boolean cross(Bound another){
        boolean b1 = another.xMax < xMin;
        boolean b2 = xMax < another.xMin;
        boolean b3 = another.zMax < zMin;
        boolean b4 = zMax < another.zMin;
        return !(b1|b2|b3|b4);
    }
}
