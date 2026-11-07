package jempasam.hexlink.entity

import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.block.HexlinkBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

object HexlinkEntities {

    private val BLOCK_ENTITIES = mutableMapOf<ResourceLocation, BlockEntityType<*>>()

    private fun <T : BlockEntity> register(id: String, type: BlockEntityType<T>): BlockEntityType<T> {
        BLOCK_ENTITIES[ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, id)] = type
        return type
    }

    /** Called from the NeoForge `RegisterEvent` for [Registries.BLOCK_ENTITY_TYPE]. */
    fun registerBlockEntities(registry: Registry<BlockEntityType<*>>) {
        for ((id, type) in BLOCK_ENTITIES) {
            Registry.register(registry, id, type)
        }
    }

    private fun <T : BlockEntity> create(
        factory: (BlockPos, BlockState) -> T,
        vararg blocks: net.minecraft.world.level.block.Block
    ): BlockEntityType<T> = BlockEntityType.Builder.of(factory::invoke, *blocks).build(null)
}
