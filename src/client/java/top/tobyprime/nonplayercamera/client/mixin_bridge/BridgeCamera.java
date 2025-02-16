package top.tobyprime.nonplayercamera.client.mixin_bridge;

import top.tobyprime.nonplayercamera.client.common.CameraRenderingData;

public interface BridgeCamera {
    CameraRenderingData getRenderingData();
    void setRenderingData(CameraRenderingData data);
}