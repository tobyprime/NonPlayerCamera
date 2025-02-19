package top.tobyprime.nonplayercamera.client.mixin_bridge;

import net.minecraft.client.particle.ParticleEngine;

public interface BridgeClientLevel {
    ParticleEngine getParticleEngine();
    void setParticleEngine(ParticleEngine particleEngine);
}
