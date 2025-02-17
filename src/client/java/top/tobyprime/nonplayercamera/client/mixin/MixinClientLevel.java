package top.tobyprime.nonplayercamera.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import top.tobyprime.nonplayercamera.client.common.SuperChunkCache;

@Mixin(ClientLevel.class)
public class MixinClientLevel {
@Shadow
ClientChunkCache chunkSource;

    @Inject(method ="<init>", at = @At("TAIL"))
    public void injectInit(){
        this.chunkSource = new SuperChunkCache((ClientLevel)(Object)this);
    }
}
