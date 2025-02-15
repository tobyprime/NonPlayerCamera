package top.tobyprime.nonplayercamera.mixin_bridge;

import top.tobyprime.nonplayercamera.common.ServerCamera;

import java.util.Set;

public interface BridgeChunkMap {
    Set<ServerCamera> getActivateCameras();
}
