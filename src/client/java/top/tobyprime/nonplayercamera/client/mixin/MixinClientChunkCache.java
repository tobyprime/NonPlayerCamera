package top.tobyprime.nonplayercamera.client.mixin;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.tobyprime.nonplayercamera.utils.Helper;

@Mixin(ClientChunkCache.Storage.class)
abstract class ClientChunkCacheStorage {
//    @Shadow @Final private ClientChunkCache field_16254;
//    @Inject(method = "<init>", at=@At("CTOR_HEAD"))
//    private void onInit(CallbackInfo ci) {
//        Helper.log("AAAAAAAAAAAAAAAAAAAAAAAAAA"+ field_16254.getLevel().toString());
////    }
//    @Inject(method = "dumpChunks", at=@At("HEAD"))
//    void dumpChunks(String filePath, CallbackInfo ci) {
//        ci.cancel();
//    }
}
