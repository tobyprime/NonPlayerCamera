package top.tobyprime.nonplayercamera.client.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import top.tobyprime.nonplayercamera.client.mixin_bridge.BridgeClientLevel;
import top.tobyprime.nonplayercamera.utils.Helper;


public class LevelManager {
    // for init chunk cache;
    public static ResourceKey<Level> onCreatingDimension = null;
    public static Map<ResourceKey<Level>, ClientLevel> levelMap = new HashMap<>();
    public static Map<ResourceKey<Level>, Set<SuperCamera>> cameraSetPerLevel = new HashMap<>();

    public static @Nullable ClientLevel get(ResourceKey<Level> dimension){
        return levelMap.get(dimension);
    }


    public static void onCameraLevelUpdated(SuperCamera camera,ResourceKey<Level> preDimension){
        var cameraSet = LevelManager.cameraSetPerLevel.get(preDimension);
        if (cameraSet != null) {
            cameraSet.remove(camera);
        }

        cameraSet = cameraSetPerLevel.computeIfAbsent(camera.getDimension(), k -> new HashSet<>());
        cameraSet.add(camera);
    }

    public static void close(){
        for(ClientLevel level : levelMap.values()){
            try {
                level.close();
            } catch (IOException e) {
                Helper.err("Failed to close level " + level);
            }
        }
        for (var cameras : cameraSetPerLevel.values()){
            for (var camera : cameras){
                camera.renderer.close();
            }
        }
        cameraSetPerLevel = new HashMap<>();
        levelMap = new HashMap<>();
    }

    public static Set<SuperCamera> getCamerasInDimension(ResourceKey<Level> dimension){
        var cameras = cameraSetPerLevel.get(dimension);
        if(cameras == null){
            return new HashSet<>();
        }
        return cameras;
    }

    public static Level getOrCreateLevel(ResourceKey<Level> dimension){
        if (!levelMap.containsKey(dimension)) {
            levelMap.put(dimension, createLevel(dimension));
        }
        return levelMap.get(dimension);
    }



    private static Set<ResourceKey<Level>> getAvailableLevelKeys() {
        return DimensionTypeManager.getAvailableDimensions();
    }

    private static ClientLevel createLevel(ResourceKey<Level> dimension) {
        var client = Minecraft.getInstance();
    

        if (!getAvailableLevelKeys().contains(dimension)) {
            throw new RuntimeException("Cannot create invalid dimension " + dimension.location());
        }
    
        client.getProfiler().push("create_Level");
        
        var levelRenderer = new LevelRenderer(client, client.renderBuffers);
        int chunkLoadDistance = 3;
    
        ClientLevel newLevel;
        onCreatingDimension = dimension;
        try {
            ClientPacketListener mainNetHandler = client.player.connection;
    
            ResourceKey<DimensionType> dimensionTypeKey = DimensionTypeManager.getDimensionTypeKey(dimension);
            var levelData = client.level.getLevelData();
    
            RegistryAccess registryManager = mainNetHandler.registryAccess();
            int simulationDistance = client.level.getServerSimulationDistance();
    
            Holder<DimensionType> dimensionType = registryManager.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getHolderOrThrow(dimensionTypeKey);
    
            //todo: 参数 flatLevel
            ClientLevel.ClientLevelData properties = new ClientLevel.ClientLevelData(
                    levelData.getDifficulty(),
                    levelData.isHardcore(),
                    false
            );
    
            newLevel = new ClientLevel(
                    mainNetHandler,
                    properties,
                    dimension,
                    dimensionType,
                    chunkLoadDistance,
                    simulationDistance,// seems that client Level does not use this
                    client::getProfiler,
                    levelRenderer,
                    client.level.isDebug(),
                    client.level.getBiomeManager().biomeZoomSeed
            );
        }
        catch (Exception e) {
            throw new IllegalStateException(
                    "Creating Client Level " + dimension ,
                    e
            );
        }
        ((BridgeClientLevel)newLevel).setParticleEngine(new ParticleEngine(newLevel, Minecraft.getInstance().getTextureManager()));
        onCreatingDimension = null;
    
        client.getProfiler().pop();
        Minecraft.getInstance().resourceManager.registerReloadListener(levelRenderer);

        return newLevel;
    }

    public static void tickAll(){
        for (var level : levelMap.values()) {
            level.tick(()->true);
        }
    }

}
