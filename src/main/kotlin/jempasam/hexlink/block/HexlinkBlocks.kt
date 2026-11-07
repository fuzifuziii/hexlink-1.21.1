package jempasam.hexlink.block

import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.entity.HexlinkEntities
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes

object HexlinkBlocks {

    class BlockEntry(val id: String, val block: Block)
    private val blocks= mutableListOf<BlockEntry>()

    private fun <T: Block>create(id: String, block: T): T{
        blocks.add(BlockEntry(id,block))
        return block
    }

    /** Called from the NeoForge `RegisterEvent` for [Registries.BLOCK]. */
    fun registerBlocks(registry: Registry<Block>){
        for(entry in blocks)
            Registry.register(registry, ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, entry.id), entry.block)
    }
}
