package top.tobyprime.nonplayercamera.client.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
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

import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public abstract class MixinClientChunkCache {


    @Shadow
    public volatile ClientChunkCache.Storage storage;

    @Shadow @Final
    ClientLevel level;

    @Unique
    public boolean isNonPlayerLevel() {
        if (Minecraft.getInstance().level == null) {
            return false;
        }
        if (Minecraft.getInstance().level.equals(level)) {
            return false;
        }
         return true;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(ClientLevel Level, int loadDistance, CallbackInfo ci) {
        if (!isNonPlayerLevel()) {
            return;
        }
        ((BridgeChunkCacheStorage) (Object) this.storage).setDimension(LevelManager.onCreatingDimension);
    }

    @Inject(method = "updateViewRadius", at = @At("HEAD"), cancellable = true)
    public void updateViewRadius(int loadDistance, CallbackInfo ci) {
        if (isNonPlayerLevel()) {
            ci.cancel();
        }
    }

    @Inject(method = "replaceWithPacketData", at = @At("HEAD"), cancellable = true)
    public void replaceWithPacketData(int x, int z, FriendlyByteBuf buffer, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> cir) {
//        Helper.dbg("ChunkCache");
//
//        if (!isNonPlayerLevel()) {
//            Helper.dbg("replacing player chunk");
//            return;
//        }
        Helper.dbg("replacing non player chunk");

        int i = this.storage.getIndex(x, z);
        LevelChunk levelChunk = this.storage.chunks.get(i);
        ChunkPos chunkPos = new ChunkPos(x, z);
        if (!((ClientChunkCache)((Object)this)).isValidChunk(levelChunk, x, z)) {
            levelChunk = new LevelChunk(this.level, chunkPos);
            levelChunk.replaceWithPacketData(buffer, tag, consumer);
            this.storage.replace(i, levelChunk);
        } else {
            levelChunk.replaceWithPacketData(buffer, tag, consumer);
        }

        this.level.onChunkLoaded(chunkPos);
        cir.setReturnValue(levelChunk);
    }


}
