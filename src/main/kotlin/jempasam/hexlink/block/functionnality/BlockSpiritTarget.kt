package jempasam.hexlink.block.functionnality

import jempasam.hexlink.spirit.inout.SpiritTarget
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos

interface BlockSpiritTarget {
    fun getSpiritTarget(world: ServerLevel, pos: BlockPos): SpiritTarget
}