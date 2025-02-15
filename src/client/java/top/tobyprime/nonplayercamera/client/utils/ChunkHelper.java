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


    public class BlockTraverse {

        public static interface IntFunc<T> {
            T eval(int i);
        }

        public static interface BiIntFunc<T> {
            T eval(int x, int z);
        }

        public static interface TriIntFunc<T> {
            T eval(int x, int y, int z);
        }

        // the from and to are inclusive!
        public static <T> T searchFromTo(int from, int to, IntFunc<T> func) {
            if (from > to) {
                for (int i = from; i >= to; i--) {
                    T obj = func.eval(i);
                    if (obj != null) {
                        return obj;
                    }
                }
            } else {
                for (int i = from; i <= to; i++) {
                    T obj = func.eval(i);
                    if (obj != null) {
                        return obj;
                    }
                }
            }
            return null;
        }

        public static <T> T searchOnPlane(
                int centerX, int centerZ, int range,
                BiIntFunc<T> func
        ) {
            T centerResult = func.eval(centerX, centerZ);
            if (centerResult != null) {
                return centerResult;
            }

            for (int layer = 1; layer < range; layer++) {
                for (int w = 0; w < layer * 2; w++) {
                    T obj = func.eval(layer + centerX, w + 1 - layer + centerZ);
                    if (obj != null) {
                        return obj;
                    }
                }

                for (int w = 0; w < layer * 2; w++) {
                    T obj = func.eval(-w + layer - 1 + centerX, layer + centerZ);
                    if (obj != null) {
                        return obj;
                    }
                }

                for (int w = 0; w < layer * 2; w++) {
                    T obj = func.eval(-layer + centerX, -w + layer - 1 + centerZ);
                    if (obj != null) {
                        return obj;
                    }
                }

                for (int w = 0; w < layer * 2; w++) {
                    T obj = func.eval(w + 1 - layer + centerX, -layer + centerZ);
                    if (obj != null) {
                        return obj;
                    }
                }
            }

            return null;
        }

        public static <T> T searchColumnedRaw(
                int centerX, int centerZ, int range,
                int startY, int endY,
                TriIntFunc<T> func
        ) {
            return searchOnPlane(
                    centerX, centerZ, range,
                    (x, z) -> searchFromTo(startY, endY, y -> func.eval(x, y, z))
            );
        }

        // NOTE Mutable block pos
        // NOTE the startY and endY are inclusive
        public static <T> T searchColumned(
                int centerX, int centerZ, int range,
                int startY, int endY,
                Function<BlockPos, T> func
        ) {
            BlockPos.MutableBlockPos temp = new BlockPos.MutableBlockPos();
            return searchColumnedRaw(
                    centerX, centerZ, range, startY, endY,
                    (x, y, z) -> {
                        temp.set(x, y, z);
                        return func.apply(temp);
                    }
            );
        }

        public static <T> T searchInBox(AABB box, TriIntFunc<T> func) {
            var minX = (int)Math.floor(box.minX);
            var maxX = (int)Math.ceil(box.maxX);
            var minY = (int)Math.floor(box.minY);
            var maxY = (int)Math.ceil(box.maxY);
            var minZ = (int)Math.floor(box.minZ);
            var maxZ = (int)Math.ceil(box.maxZ);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        final T obj = func.eval(x, y, z);
                        if (obj != null) {
                            return obj;
                        }
                    }
                }
            }
            return null;
        }

        // NOTE Mutable block pos
        public static <T> T searchInBox(AABB box, Function<BlockPos, T> func) {
            BlockPos.MutableBlockPos temp = new BlockPos.MutableBlockPos();
            return searchInBox(box,
                    (x, y, z) -> {
                        temp.set(x, y, z);
                        return func.apply(temp);
                    }
            );
        }

        // NOTE Mutable block pos
        public static boolean boxAllMatch(AABB box, Predicate<BlockPos> predicate) {
            Boolean result = searchInBox(box, mutable -> {
                if (predicate.test(mutable)) {
                    return Boolean.valueOf(true);
                }
                return null;
            });
            return result != null;
        }
    }
}
