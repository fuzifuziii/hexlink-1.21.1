package jempasam.hexlink.block.functionnality

import jempasam.hexlink.spirit.Spirit
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

interface BlockSpiritContainer {
    fun getSpiritContent(slot: Int, world: Level, pos: BlockPos): Sequence<Spirit>
    fun getSlotCount(): Int
}