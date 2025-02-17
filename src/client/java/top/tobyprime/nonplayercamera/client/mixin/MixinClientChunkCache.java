package top.tobyprime.nonplayercamera.client.mixin;


import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientChunkCache.Storage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import top.tobyprime.nonplayercamera.client.common.LevelManager;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeChunkCacheStorage;
import top.tobyprime.nonplayercamera.utils.Helper;

import java.util.Map;

import java.util.function.Consumer;
import java.util.logging.Level;

@Mixin(ClientChunkCache.class)
public abstract class MixinClientChunkCache {


    @Shadow
    public volatile ClientChunkCache.Storage storage;
    
    public Map<Camera,Storage> cameraChunkStorages;


    @Shadow @Final
    ClientLevel level;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void injectInit(ClientLevel Level, int loadDistance, CallbackInfo ci) {
        ((BridgeChunkCacheStorage) (Object) this.storage).setDimension(level.dimension());
    }

    @Inject(method = "updateViewRadius", at = @At("HEAD"), cancellable = true)
    public void injectUpdateViewRadius(int loadDistance, CallbackInfo ci) {
        ci.cancel();
    }

    public Storage getClosestChunkStorage(int x,int z){
        Storage clstStorage = null;
        float minDistance = Float.MAX_VALUE;
        
        for (var storage : cameraChunkStorages.values()) {
            var dstSqr =  getChunkStorageDstSqrTo(storage, x, z);
            
            if (dstSqr<minDistance) {
                minDistance = dstSqr;
                clstStorage = storage;
            }
        }
        return clstStorage;
    }

    public float getChunkStorageDstSqrTo(Storage storage,int x,int z){
        return storage.viewCenterX * storage.viewCenterX
        + storage.viewCenterZ * storage.viewCenterZ;
    }

    public LevelChunk getChunkData(int x,int z){
        var chunk = getChunkInStorage(storage, x, z);
        if (chunk != null){
            return chunk;
        }
        return getChunkInStorage(getClosestChunkStorage(x,z),x, z);

    }

    public LevelChunk getChunkInStorage(Storage storage,int x,int z){
        if (storage == null) {
            return null;
        }

        int i = storage.getIndex(x, z);
        LevelChunk levelChunk = storage.chunks.get(i);

        if (ClientChunkCache.isValidChunk(levelChunk, x, z)) {
            return levelChunk;
        }
        return null;
    }

    @Inject(method = "replaceWithPacketData", at = @At("HEAD"), cancellable = true)
    public void replaceWithPacketData(int x, int z, FriendlyByteBuf buffer, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> cir) {
        Helper.dbg("replacing non player chunk");
        if (storage.inRange(x,z)) {
            return;
        }
        var storage = getClosestChunkStorage(x, z);

        int i = storage.getIndex(x, z);
        LevelChunk levelChunk = storage.chunks.get(i);
        ChunkPos chunkPos = new ChunkPos(x, z);
        
        if (!ClientChunkCache.isValidChunk(levelChunk, x, z)) {
            levelChunk = new LevelChunk(this.level, chunkPos);
            levelChunk.replaceWithPacketData(buffer, tag, consumer);
            storage.replace(i, levelChunk);
        } else {
            levelChunk.replaceWithPacketData(buffer, tag, consumer);
        }

        level.onChunkLoaded(chunkPos);
        cir.setReturnValue(levelChunk);
    }


}
