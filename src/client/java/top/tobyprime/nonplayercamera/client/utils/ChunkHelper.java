package top.tobyprime.nonplayercamera.client.utils;


import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;

import java.util.BitSet;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class ChunkHelper {
    public static void updateLightingForLevel(ClientLevel level, int chunkX, int chunkZ, LevelLightEngine provider, LightLayer type, BitSet inited, BitSet uninited, Iterator<byte[]> nibbles, boolean nonEdge) {
        for (int i = 0; i < provider.getLightSectionCount(); ++i) {
            int j = provider.getMinLightSection() + i;
            boolean bl = inited.get(i);
            boolean bl2 = uninited.get(i);
            if (bl || bl2) {
                provider.queueSectionData(type, SectionPos.of(chunkX, j, chunkZ), bl ? new DataLayer((byte[]) ((byte[]) nibbles.next()).clone()) : new DataLayer(), nonEdge);
                level.setSectionDirtyWithNeighbors(chunkX, j, chunkZ);
            }
        }

    }

    public static void readLightDataForLevel(ClientLevel Level, int x, int z, ClientboundLightUpdatePacketData data) {
        LevelLightEngine lightingProvider = Level.getChunkSource().getLightEngine();

        BitSet bitSet = data.getSkyYMask();
        BitSet bitSet2 = data.getEmptySkyYMask();
        Iterator<byte[]> iterator = data.getSkyUpdates().iterator();
        updateLightingForLevel(Level, x, z, lightingProvider, LightLayer.SKY, bitSet, bitSet2, iterator, data.getTrustEdges());
        BitSet bitSet3 = data.getBlockYMask();
        BitSet bitSet4 = data.getEmptyBlockYMask();
        Iterator<byte[]> iterator2 = data.getBlockUpdates().iterator();
        updateLightingForLevel(Level, x, z, lightingProvider, LightLayer.BLOCK, bitSet3, bitSet4, iterator2, data.getTrustEdges());
        Level.setLightReady(x, z);

    }
}
