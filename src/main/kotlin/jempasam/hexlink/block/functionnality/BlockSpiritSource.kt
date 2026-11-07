package jempasam.hexlink.block.functionnality

import jempasam.hexlink.spirit.inout.SpiritSource
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos

interface BlockSpiritSource {
    fun getSpiritSource(world: ServerLevel, pos: BlockPos): SpiritSource
}