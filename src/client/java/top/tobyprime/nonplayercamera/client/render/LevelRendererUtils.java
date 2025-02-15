package top.tobyprime.nonplayercamera.client.render;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer.RenderChunkInfo;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeRenderChunk;

import java.util.ArrayDeque;

public class LevelRendererUtils {
    public static ArrayDeque<ChunkRenderDispatcher.RenderChunk> searchQueueCache = new ArrayDeque<>();

    public static void calcVisibleSections(
            ClientLevel world,
            ViewArea viewArea,
            Camera camera,
            int viewDistance,
            Frustum frustum,
            ObjectArrayList<RenderChunkInfo> resultHolder
    ) {
        resultHolder.clear();
        searchQueueCache.clear();
        long renderTime = System.nanoTime();

        Vec3 cameraPos = camera.getPosition();
        frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
        var cameraSectionPos = SectionPos.of(new BlockPos(cameraPos));

        chunkSearchStep(renderTime, cameraSectionPos, cameraSectionPos, viewDistance, viewArea, frustum, searchQueueCache, resultHolder);

        while (!searchQueueCache.isEmpty()) {
            ChunkRenderDispatcher.RenderChunk curr = searchQueueCache.poll();
            int cx = SectionPos.blockToSectionCoord(curr.getOrigin().getX());
            int cy = SectionPos.blockToSectionCoord(curr.getOrigin().getY());
            int cz = SectionPos.blockToSectionCoord(curr.getOrigin().getZ());

            chunkSearchStep(renderTime, SectionPos.of(cx + 1, cy, cz), cameraSectionPos, viewDistance, viewArea, frustum, searchQueueCache, resultHolder);
            chunkSearchStep(renderTime, SectionPos.of(cx - 1, cy, cz), cameraSectionPos, viewDistance, viewArea, frustum, searchQueueCache, resultHolder);
            chunkSearchStep(renderTime, SectionPos.of(cx, cy + 1, cz), cameraSectionPos, viewDistance, viewArea, frustum, searchQueueCache, resultHolder);
            chunkSearchStep(renderTime, SectionPos.of(cx, cy - 1, cz), cameraSectionPos, viewDistance, viewArea, frustum, searchQueueCache, resultHolder);
            chunkSearchStep(renderTime, SectionPos.of(cx, cy, cz + 1), cameraSectionPos, viewDistance, viewArea, frustum, searchQueueCache, resultHolder);
            chunkSearchStep(renderTime, SectionPos.of(cx, cy, cz - 1), cameraSectionPos, viewDistance, viewArea, frustum, searchQueueCache, resultHolder);
        }
        return;
    }


    private static void chunkSearchStep(long renderTime, SectionPos pos, SectionPos cameraSectionPos, int viewDistance, ViewArea viewArea, Frustum frustum, ArrayDeque<ChunkRenderDispatcher.RenderChunk> searchQueue, ObjectArrayList<RenderChunkInfo> resultHolder) {
        var chunk = getSectionIfInViewDistance(renderTime, pos, cameraSectionPos, viewDistance, viewArea, frustum);
        if (chunk != null) {
            searchQueue.push(chunk);

            resultHolder.push(new RenderChunkInfo(
                    chunk,
                    null, 0
            ));
        }
    }

    private static ChunkRenderDispatcher.RenderChunk getSectionIfInViewDistance(long renderTime, SectionPos sectionPos, SectionPos cameraSectionPos, int viewDistance, ViewArea viewArea, Frustum frustum) {

        if (!sectionPos.closerThan(cameraSectionPos, viewDistance)) {
            return null;
        }
        var pos = sectionPos.center();

        var chunk = viewArea.getRenderChunkAt(pos);

        if (chunk == null) {
            return null;
        }

        if (((BridgeRenderChunk) chunk).getPreRenderTime() == renderTime) {
            return null;
        }
        ((BridgeRenderChunk) chunk).setPreRenderTime(renderTime);

        if (frustum == null) {
            return chunk;
        }
        if (frustum.isVisible(chunk.getBoundingBox())) {
            return chunk;
        }
        return null;
    }
}
