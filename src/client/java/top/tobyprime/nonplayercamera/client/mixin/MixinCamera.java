package top.tobyprime.nonplayercamera.client.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.tobyprime.nonplayercamera.client.common.RenderingManager;
import top.tobyprime.nonplayercamera.client.common.SuperCamera;
import top.tobyprime.nonplayercamera.client.common.SuperChunkCache;

@Mixin(Camera.class)
public class MixinCamera {
    private boolean isSuperCamera(){
        return (Object)this instanceof SuperCamera;
    }

    @Inject(method = "<init>", at= @At("TAIL"))
    public void injectInit(CallbackInfo ci){
        if (isSuperCamera()){
            return;
        }
        if (RenderingManager.isEnvModified()){
            ((SuperChunkCache) RenderingManager.mainRenderingContext.level.getChunkSource()).onCameraUpdated((Camera) (Object)this);
            return;
        }
        if (Minecraft.getInstance().level==null) return;

        ((SuperChunkCache)Minecraft.getInstance().level.getChunkSource()).onCameraUpdated((Camera) (Object)this);
    }

    @Inject(method = "setPosition(Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
    public void injectSetPosition(Vec3 pos, CallbackInfo ci){
        if (isSuperCamera()){
            return;
        }
        if (RenderingManager.isEnvModified()){
            ((SuperChunkCache) RenderingManager.mainRenderingContext.level.getChunkSource()).onCameraUpdated((Camera) (Object)this);
            return;
        }
        ((SuperChunkCache)Minecraft.getInstance().level.getChunkSource()).onCameraUpdated((Camera) (Object)this);
    }
}
