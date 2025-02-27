package top.tobyprime.nonplayercamera.client.common;

import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class DimensionTypeManager {
    public static Map<ResourceKey<Level>, ResourceKey<DimensionType>> clientTypeMap;

    public static Set<ResourceKey<Level>> getAvailableDimensions() {
        return Minecraft.getInstance().getConnection().levels();
    }

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
