package top.tobyprime.nonplayercamera.client.mixin_bridge;

import top.tobyprime.nonplayercamera.client.common.SuperCamera;

public interface BridgeLevelRenderer {
    void setCamera(SuperCamera camera);
    SuperCamera getCamera();
}
