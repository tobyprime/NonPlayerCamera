package top.tobyprime.nonplayercamera.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;

import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.tobyprime.nonplayercamera.common.ServerCamera;
import top.tobyprime.nonplayercamera.mixin_bridge.BridgeChunkMap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Mixin(ChunkMap.class)
public abstract class MixinChunkMap implements BridgeChunkMap {
    @Shadow
    protected static boolean isChunkOnRangeBorder(int i, int j, int k, int l, int m) {
        return false;
    }

    @Shadow
    public static boolean isChunkInRange(int i, int j, int k, int l, int m) {
        return false;
    }

    @Shadow @Final private PlayerMap playerMap;
    @Shadow private int viewDistance;
    @Unique
    private final Set<ServerCamera> activateCameras = new HashSet<>();



    @Inject(method = "getPlayers", at = @At("HEAD"), cancellable = true)
    public void getPlayers(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge, CallbackInfoReturnable<List<ServerPlayer>> cir) {
        Set<ServerPlayer> playerInChunk = new HashSet<>();
        for (ServerCamera camera : activateCameras) {
            SectionPos chunkSectionPos = SectionPos.of(camera.pos);
            if (onlyOnWatchDistanceEdge && isChunkOnRangeBorder(chunkPos.x, chunkPos.z, chunkSectionPos.getX(), chunkSectionPos.getZ(), camera.viewDistance)
                    || !onlyOnWatchDistanceEdge && isChunkInRange(chunkPos.x, chunkPos.z, chunkSectionPos.getX(), chunkSectionPos.getZ(), camera.viewDistance)) {
                playerInChunk.add(camera.player);
            }
        }

        Set<ServerPlayer> players = this.playerMap.getPlayers(chunkPos.toLong());

        for(ServerPlayer serverPlayerEntity : players) {
            SectionPos chunkSectionPos = serverPlayerEntity.getLastSectionPos();
            if (onlyOnWatchDistanceEdge && isChunkOnRangeBorder(chunkPos.x, chunkPos.z, chunkSectionPos.getX(), chunkSectionPos.getZ(), this.viewDistance) || !onlyOnWatchDistanceEdge && isChunkInRange(chunkPos.x, chunkPos.z, chunkSectionPos.getX(), chunkSectionPos.getZ(), this.viewDistance)) {
                playerInChunk.add(serverPlayerEntity);
            }
        }

        cir.setReturnValue(playerInChunk.stream().toList());
    }

    @Unique
    public Set<ServerCamera> getActivateCameras() {return this.activateCameras;}
}
