package top.tobyprime.nonplayercamera.client.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.tobyprime.nonplayercamera.client.common.LevelManager;
import top.tobyprime.nonplayercamera.client.utils.ChunkHelper;
import top.tobyprime.nonplayercamera.mixin_bridge.BridgeClientboundLevelChunkWithLightPacket;
import top.tobyprime.nonplayercamera.utils.Helper;

import java.util.BitSet;
import java.util.Iterator;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handleLevelChunkWithLight", at = @At("HEAD"), cancellable = true)
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ClientGamePacketListener) (Object)this, this.minecraft);

        var chunkLevel = ((BridgeClientboundLevelChunkWithLightPacket) packet).getLevelKey();

        if (Minecraft.getInstance().level==null || chunkLevel == Minecraft.getInstance().level.dimension()) {
            Helper.dbg("handle player chunk data");
            return;
        }

        var nonPlayerLevel = LevelManager.levelMap.get(chunkLevel);
        if (nonPlayerLevel == null || nonPlayerLevel.equals(Minecraft.getInstance().level)) {
            Helper.dbg("handle player chunk data");
            return;
        }
        Helper.dbg("handle nonplayer chunk data");

        var chunkData = packet.getChunkData();
        int x = packet.getX();
        int z = packet.getZ();
        var level = nonPlayerLevel;
        level.getChunkSource().replaceWithPacketData(packet.getX(), packet.getZ(), chunkData.getReadBuffer(), chunkData.getHeightmaps(), chunkData.getBlockEntitiesTagsConsumer(x, z));


        var lightData = packet.getLightData();
        level.queueLightUpdate(() -> {
            ChunkHelper.readLightDataForLevel(level, x, z, lightData);
            LevelChunk chunk = level.getChunkSource().getChunk(x, z, false);

            if (chunk != null) {
                LevelLightEngine lightingProvider = level.getChunkSource().getLightEngine();
                LevelChunkSection[] chunkSections = chunk.getSections();
                ChunkPos chunkPos = chunk.getPos();
                lightingProvider.enableLightSources(chunkPos, true);

                for (int i = 0; i < chunkSections.length; ++i) {
                    LevelChunkSection chunkSection = chunkSections[i];
                    int j = level.getSectionYFromSectionIndex(i);
                    lightingProvider.updateSectionStatus(SectionPos.of(chunkPos, j), chunkSection.hasOnlyAir());
                    level.setSectionDirtyWithNeighbors(x, j, z);
                }

                level.setLightReady(x, z);
            }

        });
        ci.cancel();
    }


}

