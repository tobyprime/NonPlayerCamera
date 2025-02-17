package top.tobyprime.nonplayercamera.client.common;

import java.util.Map;
import java.util.Set;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.dimension.DimensionType;


public class LevelManager {
    // for init chunk cache;
    public static ResourceKey<Level> onCreatingDimension = null;
    public static Map<ResourceKey<Level>, ClientLevel> levelMap;

    public static ClientLevel get(ResourceKey<Level> dimension){
        return levelMap.get(dimension);
    }

    public static Level getOrCreateLevel(ResourceKey<Level> dimention){
        if (!levelMap.containsKey(dimention)) {
            levelMap.put(dimention, createLevel(dimention));
        }
        return levelMap.get(dimention);
    }



    private static Set<ResourceKey<Level>> getAvailableLevelKeys() {
        Set<ResourceKey<Level>> keys = Minecraft.getInstance().getConnection().levels();
        return keys;
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
        onCreatingDimension = null;
    
        client.getProfiler().pop();
    
        return newLevel;
    }

    public static void tickAll(){
        for (var level : levelMap.values()) {
            level.tick(()->true);
        }
    }

}
