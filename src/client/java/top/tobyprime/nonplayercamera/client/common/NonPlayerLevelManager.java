package top.tobyprime.nonplayercamera.client.common;


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
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NonPlayerLevelManager {
    public ClientLevel playerLevel = null;
    public static Map<ResourceKey<Level>, ClientLevel> clientLevelMap = new HashMap<>();
    public static Set<ResourceKey<Level>> getAvailableLevelKeys() {
        Set<ResourceKey<Level>> keys = Minecraft.getInstance().getConnection().levels();
        return keys;
    }

    public static ClientLevel getLevel(ResourceKey<Level> key) {
        return clientLevelMap.get(key);
    }
    public static ClientLevel getOrCreateLevel(ResourceKey<Level> key,@Nullable LevelRenderer renderer) {
        if (clientLevelMap.containsKey(key)) {
            return clientLevelMap.get(key);
        }
        if (key == Minecraft.getInstance().level.dimension()) {
            clientLevelMap.put(key, Minecraft.getInstance().level);
            return clientLevelMap.get(key);
        }

        Validate.notNull(renderer);
        var client = Minecraft.getInstance();

        Set<ResourceKey<Level>> availableLevelKeys = getAvailableLevelKeys();

        if (!availableLevelKeys.contains(key)) {
            throw new RuntimeException("Cannot create invalid dimension " + key.location());
        }

        client.getProfiler().push("create_Level");

        int chunkLoadDistance = 3;// my own chunk manager doesn't need it

        ClientLevel newLevel;

        try {
            ClientPacketListener mainNetHandler = client.player.connection;

            ResourceKey<DimensionType> dimensionTypeKey = DimensionTypeManager.getDimensionTypeKey(key);
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
                    key,
                    dimensionType,
                    chunkLoadDistance,
                    simulationDistance,// seems that client Level does not use this
                    client::getProfiler,
                    renderer,
                    client.level.isDebug(),
                    client.level.getBiomeManager().biomeZoomSeed
            );
        }
        catch (Exception e) {
            throw new IllegalStateException(
                    "Creating Client Level " + key + " " + clientLevelMap.keySet(),
                    e
            );
        }
        clientLevelMap.put(key, newLevel);

        client.getProfiler().pop();

        return newLevel;


    }

    public static class DimensionTypeManager{
        public static Map<ResourceKey<Level>, ResourceKey<DimensionType>> clientTypeMap;

        public static ResourceKey<DimensionType> getDimensionTypeKey(ResourceKey<Level> LevelKey) {
            if (LevelKey == Level.OVERWORLD) {
                return DimensionType.OVERWORLD_LOCATION;
            }

            if (LevelKey == Level.NETHER) {
                return DimensionType.NETHER_LOCATION;
            }

            if (LevelKey == Level.END) {
                return DimensionType.END_LOCATION;
            }

            ResourceKey<DimensionType> obj = clientTypeMap.get(LevelKey);

            if (obj == null) {
                return DimensionType.OVERWORLD_LOCATION;
            }

            return obj;
        }
    }
}

