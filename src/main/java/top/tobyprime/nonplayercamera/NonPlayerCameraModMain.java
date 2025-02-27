package top.tobyprime.nonplayercamera;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import top.tobyprime.nonplayercamera.common.ServerCameraManager;

public class NonPlayerCameraModMain implements ModInitializer {
    public static final String MOD_ID = "nonplayercamera";

    public static final TestBlock TEST_BLOCK = new TestBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));
    public static final BlockEntityType<TestBlockEntity> TEST_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation("test", "test_block"), FabricBlockEntityTypeBuilder.create(TestBlockEntity::new, TEST_BLOCK).build());

    @Override
    public void onInitialize() {

        Registry.register(Registry.BLOCK, new ResourceLocation("test", "test_block"), TEST_BLOCK);
        Registry.register(Registry.ITEM, new ResourceLocation("test", "test_block"), new BlockItem(TEST_BLOCK, new Item.Properties()));

        ServerTickEvents.END_SERVER_TICK.register(server -> ServerCameraManager.tick());
    }

    public static class TestBlock extends BaseEntityBlock {
        protected TestBlock(BlockBehaviour.Properties settings) {
            super(settings);
        }
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new TestBlockEntity(pos, state);
        }

    }

    public static class TestBlockEntity extends BlockEntity {
        public TestBlockEntity(BlockPos pos, BlockState state) {
            super(NonPlayerCameraModMain.TEST_BLOCK_ENTITY, pos, state);
        }

    }

}
